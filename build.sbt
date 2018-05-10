import sbt.Keys._
import sbt._
import scala.io.Source

import spray.json._, DefaultJsonProtocol._

import complete.DefaultParsers._

import scala.language.postfixOps
import gov.nasa.jpl.imce.sbt._
import gov.nasa.jpl.imce.sbt.ProjectHelper._
import java.io.File

//import gov.nasa.jpl.imce.profileGenerator.batch.tests.RunProfileGenerator

updateOptions := updateOptions.value.withCachedResolution(true)

resolvers ++= {
  if (git.gitUncommittedChanges.value)
    Seq[Resolver](Resolver.mavenLocal)
  else
    Seq.empty[Resolver]
}

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

lazy val mdInstallDirectory = SettingKey[File]("md-install-directory", "MagicDraw Installation Directory")

mdInstallDirectory in Global :=
  baseDirectory.value / "target" / "md.package"

lazy val testsInputsDir = SettingKey[File]("tests-inputs-dir", "Directory to scan for input *.json tests")

lazy val testsResultDir = SettingKey[File]("tests-result-dir", "Directory for the tests results to archive as the test resource artifact")

lazy val testsResultsSetupTask = taskKey[Unit]("Create the tests results directory")

lazy val produceProfile = inputKey[Unit]("Produce a MD SysML profile from a given digest")

lazy val mdJVMFlags = SettingKey[Seq[String]]("md-jvm-flags", "Extra JVM flags for running MD (e.g., debugging)")

// @see https://github.com/jrudolph/sbt-dependency-graph/issues/113
def zipFileSelector
( a: Artifact, f: File)
: Boolean
= a.`type` == "zip" || a.extension == "zip"

// @see https://github.com/jrudolph/sbt-dependency-graph/issues/113
def fromConfigurationReport
(report: ConfigurationReport,
 rootInfo: sbt.ModuleID,
 selector: (Artifact, File) => Boolean)
: net.virtualvoid.sbt.graph.ModuleGraph = {
  implicit def id(sbtId: sbt.ModuleID): net.virtualvoid.sbt.graph.ModuleId
  = net.virtualvoid.sbt.graph.ModuleId(sbtId.organization, sbtId.name, sbtId.revision)

  def moduleEdges(orgArt: OrganizationArtifactReport)
  : Seq[(net.virtualvoid.sbt.graph.Module, Seq[net.virtualvoid.sbt.graph.Edge])]
  = {
    val chosenVersion = orgArt.modules.find(!_.evicted).map(_.module.revision)
    orgArt.modules.map(moduleEdge(chosenVersion))
  }

  def moduleEdge(chosenVersion: Option[String])(report: ModuleReport)
  : (net.virtualvoid.sbt.graph.Module, Seq[net.virtualvoid.sbt.graph.Edge]) = {
    val evictedByVersion = if (report.evicted) chosenVersion else None

    val jarFile = report.artifacts.find(selector.tupled).map(_._2)
    (net.virtualvoid.sbt.graph.Module(
      id = report.module,
      license = report.licenses.headOption.map(_._1),
      evictedByVersion = evictedByVersion,
      jarFile = jarFile,
      error = report.problem),
      report.callers.map(caller ⇒ net.virtualvoid.sbt.graph.Edge(caller.caller, report.module)))
  }

  val (nodes, edges) = report.details.flatMap(moduleEdges).unzip
  val root = net.virtualvoid.sbt.graph.Module(rootInfo)

  net.virtualvoid.sbt.graph.ModuleGraph(root +: nodes, edges.flatten)
}

lazy val core =
  Project("imce-profileGenerator-batch", file("."))
    .enablePlugins(IMCEGitPlugin)
    .enablePlugins(IMCEReleasePlugin)
    .settings(dynamicScriptsResourceSettings("gov.nasa.jpl.imce.profileGenerator.batch"))
    .settings(IMCEPlugin.strictScalacFatalWarningsSettings)
    .settings(IMCEReleasePlugin.packageReleaseProcessSettings)
    .settings(
      releaseProcess := Seq(
        IMCEReleasePlugin.clearSentinel,
        sbtrelease.ReleaseStateTransformations.checkSnapshotDependencies,
        sbtrelease.ReleaseStateTransformations.inquireVersions,
        IMCEReleasePlugin.extractStep,
        IMCEReleasePlugin.setReleaseVersion,
        IMCEReleasePlugin.runCompile,
        sbtrelease.ReleaseStateTransformations.tagRelease,
        sbtrelease.ReleaseStateTransformations.publishArtifacts,
        sbtrelease.ReleaseStateTransformations.pushChanges,
        IMCEReleasePlugin.successSentinel
      ),

      IMCEKeys.licenseYearOrRange := "2016",
      IMCEKeys.organizationInfo := IMCEPlugin.Organizations.omf,
      IMCEKeys.targetJDK := IMCEKeys.jdk18.value,

      buildInfoPackage := "gov.nasa.jpl.imce.profileGenerator.batch",
      buildInfoKeys ++= Seq[BuildInfoKey](BuildInfoKey.action("buildDateUTC") { buildUTCDate.value }),

      scalacOptions in (Compile,doc) ++= Seq(
        "-diagrams",
        "-doc-title", name.value,
        "-doc-root-content", baseDirectory.value + "/rootdoc.txt"),

      projectID := {
        val previous = projectID.value
        previous.extra(
          "build.date.utc" -> buildUTCDate.value,
          "artifact.kind" -> "magicdraw.library")
      },

      IMCEKeys.targetJDK := IMCEKeys.jdk18.value,
      git.baseVersion := Versions.version,

      publishArtifact in Test := true,

      logLevel in Test := Level.Debug,

      unmanagedClasspath in Compile ++= (unmanagedJars in Compile).value,

      resolvers += Resolver.bintrayRepo("jpl-imce", "gov.nasa.jpl.imce"),
      resolvers += Resolver.bintrayRepo("tiwg", "org.omg.tiwg"),

      resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases",
      scalacOptions in (Compile, compile) += s"-P:artima-supersafe:config-file:${baseDirectory.value}/project/supersafe.cfg",
      scalacOptions in (Test, compile) += s"-P:artima-supersafe:config-file:${baseDirectory.value}/project/supersafe.cfg",
      scalacOptions in (Compile, doc) += "-Xplugin-disable:artima-supersafe",
      scalacOptions in (Test, doc) += "-Xplugin-disable:artima-supersafe",

      mdJVMFlags := Seq("-Xmx8G"), //
      // for debugging: Seq("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"),

      testsInputsDir := baseDirectory.value / "resources" / "tests",

      testsResultDir := baseDirectory.value / "target" / "md.testResults",

      scalaSource in Test := baseDirectory.value / "src" / "test" / "scala",

      testsResultsSetupTask := {

        val s = streams.value

        // Wipe any existing tests results directory and create a fresh one
        val resultsDir = testsResultDir.value
        if (resultsDir.exists) {
          s.log.warn(s"# Deleting existing results directory: $resultsDir")
          IO.delete(resultsDir)
        }
        s.log.warn(s"# Creating results directory: $resultsDir")
        IO.createDirectory(resultsDir)
        require(
          resultsDir.exists && resultsDir.canWrite,
          s"The created results directory should exist and be writeable: $resultsDir")

      },

      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test,compile",

      libraryDependencies ~= {
        _ map {
          case m => m.exclude("imce.third_party.other_scala", "scalacheck")
            .exclude("org.scalacheck", "scalacheck")
        }
      },

      // Profile generator app with dynamic scripts (needs to be extracted over MD installation)
      libraryDependencies += "gov.nasa.jpl.imce"
        %% "gov.nasa.jpl.imce.profileGenerator.application"
        % "2.5.4"
        artifacts
        Artifact("gov.nasa.jpl.imce.profileGenerator.application", "zip", "zip", "resource"),

      // TODO These are dependencies of the profile generator: (currently a manual dependency?)
      // https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple
      libraryDependencies +=
        "com.googlecode.json-simple"
          % "json-simple"
          % "1.1.1",

      libraryDependencies +=
        "org.omg.tiwg.vendor.nomagic"
          % "com.nomagic.magicdraw.sysml.plugin"
          % "18.0-sp6.2"
          artifacts
          Artifact("com.nomagic.magicdraw.sysml.plugin", "pom", "pom", None, Seq(), None, Map()),

      libraryDependencies += "gov.nasa.jpl.imce"
        % "gov.nasa.jpl.imce.metrology.isoiec80000.magicdraw.library"
        % "18.0.7"
        artifacts
        Artifact("gov.nasa.jpl.imce.metrology.isoiec80000.magicdraw.library", "zip", "zip", "resource"),

     /* produceProfile := {
        // get the result of parsing
        val args: Seq[String] = spaceDelimited("<arg>").parsed

        args foreach(_ match {
          case knownString if knownString.startsWith("-digest=") => {
                val digest = knownString.replace("-digest=", "")

                (new RunProfileGenerator).execute(configMap = Map("digest" -> digest))
              }
          case unknownString => None
        })
      },*/

      test in Test := (test in Test).dependsOn(testsResultsSetupTask).value,

      testOptions in testOnly += Tests.Argument("-digest", "../../project-bundle.json"),

      parallelExecution in Test := false,

      fork in Test := true,

      testGrouping in Test := {
        val original = (testGrouping in Test).value
        val tests_dir = testsInputsDir.value
        val md_install_dir = mdInstallDirectory.value
        val tests_results_dir = testsResultDir.value
        val pas = (packageBin in Universal).value
        val jvmFlags = mdJVMFlags.value
        val jHome = javaHome.value
        val cInput = connectInput.value
        val jOpts = javaOptions.value
        val env = envVars.value
        val s = streams.value

        val testOutputFile = tests_results_dir.toPath.resolve("output.log").toFile

        val xlogger = new xsbti.Logger {

          def debug(msg: xsbti.F0[String]): Unit = append(msg())
          def error(msg: xsbti.F0[String]): Unit = append(msg())
          def info(msg: xsbti.F0[String]): Unit = append(msg())
          def warn(msg: xsbti.F0[String]): Unit = append(msg())
          def trace(exception: xsbti.F0[Throwable]): Unit = {
            val t = exception()
            append(t.getMessage)
            append(t.getStackTraceString)
          }

          def append(msg: String): Unit = {
            val pw = new java.io.PrintWriter(new java.io.FileWriter(testOutputFile, true))
            pw.println(msg)
            pw.flush()
            pw.close()
          }

        }

        val logger = new FullLogger(xlogger)

        //val ds_dir = md_install_dir / "dynamicScripts"

        //val files = IO.unzip(pas, ds_dir)
        //s.log.warn(
        //  s"=> Installed ${files.size} " +
        //    s"files extracted from zip: $pas")

        val mdProperties = new java.util.Properties()
        IO.load(mdProperties, md_install_dir / "bin" / "magicdraw.properties")

        val mdBoot =
          mdProperties
            .getProperty("BOOT_CLASSPATH")
            .split(":")
            .map(md_install_dir / _)
            .toSeq
        s.log.warn(s"# MD BOOT CLASSPATH: ${mdBoot.mkString("\n", "\n", "\n")}")

        val mdClasspath =
          mdProperties
            .getProperty("CLASSPATH")
            .split(":")
            .map(md_install_dir / _)
            .toSeq
        s.log.warn(s"# MD CLASSPATH: ${mdClasspath.mkString("\n", "\n", "\n")}")

        val imceSetupProperties = IO.readLines(md_install_dir / "bin" / "magicdraw.imce.setup.sh")

        val imceBoot =
          imceSetupProperties
            .find(_.startsWith("IMCE_BOOT_CLASSPATH_PREFIX"))
            .getOrElse("")
            .stripPrefix("IMCE_BOOT_CLASSPATH_PREFIX=\"")
            .stripSuffix("\"")
            .split("\\\\+:")
            .map(md_install_dir / _)
            .toSeq
        s.log.warn(s"# IMCE BOOT: ${imceBoot.mkString("\n", "\n", "\n")}")

        val imcePrefix =
          imceSetupProperties
            .find(_.startsWith("IMCE_CLASSPATH_PREFIX"))
            .getOrElse("")
            .stripPrefix("IMCE_CLASSPATH_PREFIX=\"")
            .stripSuffix("\"")
            .split("\\\\+:")
            .map(md_install_dir / _)
            .toSeq
        s.log.warn(s"# IMCE CLASSPATH Prefix: ${imcePrefix.mkString("\n", "\n", "\n")}")

        original.map { group =>

          s.log.warn(s"# ${env.size} env properties")
          env.keySet.toList.sorted.foreach { k =>
            s.log.warn(s"env[$k]=${env.get(k)}")
          }
          s.log.warn(s"# ------")

          s.log.warn(s"# ${jOpts.size} java options")
          s.log.warn(jOpts.mkString("\n"))
          s.log.warn(s"# ------")

          s.log.warn(s"# ${jvmFlags.size} jvm flags")
          s.log.warn(jvmFlags.mkString("\n"))
          s.log.warn(s"# ------")

          val testPropertiesFile =
            md_install_dir.toPath.resolve("data/imce.properties").toFile

          val out = new java.io.PrintWriter(new java.io.FileWriter(testPropertiesFile))
          val in = Source.fromFile(md_install_dir.toPath.resolve("data/test.properties").toFile)
          for (line <- in.getLines) {
            if (line.startsWith("log4j.appender.R.File="))
              out.println(s"log4j.appender.R.File=$tests_results_dir/tests.log")
            else if (line.startsWith("log4j.appender.SO=")) {
              out.println(s"log4j.appender.SO=org.apache.log4j.RollingFileAppender")
              out.println(s"log4j.appender.SO.File=$tests_results_dir/console.log")
            }
            else
              out.println(line)
          }
          out.close()

          val forkOptions = ForkOptions(
            bootJars = imceBoot ++ mdBoot,
            javaHome = jHome,
            connectInput = cInput,
            outputStrategy = Some(LoggedOutput(logger)),
            runJVMOptions = jOpts ++ Seq(
              "-DLOCALCONFIG=false",
              "-DWINCONFIG=false",
              "-DHOME=" + md_install_dir.getAbsolutePath,
              s"-Ddebug.properties=$testPropertiesFile",
              "-Ddebug.properties.file=imce.properties",
              "-DFL_FORCE_USAGE=true",
              "-DFL_SERVER_ADDRESS=cae-lic04.jpl.nasa.gov",
              "-DFL_SERVER_PORT=1101",
              "-DFL_EDITION=enterprise",
              "-classpath", (imcePrefix ++ mdClasspath).mkString(File.pathSeparator)
            ) ++ jvmFlags,
            workingDirectory = Some(md_install_dir),
            envVars = env +
              ("debug.dir" -> md_install_dir.getAbsolutePath) +
              ("FL_FORCE_USAGE" -> "true") +
              ("FL_SERVER_ADDRESS" -> "cae-lic04.jpl.nasa.gov") +
              ("FL_SERVER_PORT" -> "1101") +
              ("FL_EDITION" -> "enterprise") +
              ("DYNAMIC_SCRIPTS_TESTS_DIR" -> tests_dir.getAbsolutePath) +
              ("DYNAMIC_SCRIPTS_RESULTS_DIR" -> tests_results_dir.getAbsolutePath)
          )

          s.log.warn(s"# working directory: $md_install_dir")

          group.copy(runPolicy = Tests.SubProcess(forkOptions))
        }
      },

      extractArchives := {
        val base = baseDirectory.value
        val up = update.value
        val s = streams.value
        val showDownloadProgress = true // does not compile: logLevel.value <= Level.Debug

        val mdInstallDir = (mdInstallDirectory in ThisBuild).value
        if (!mdInstallDir.exists) {

          IO.createDirectory(mdInstallDir)

          MagicDrawDownloader.fetchMagicDraw(
            s.log, showDownloadProgress,
            up,
            credentials.value,
            mdInstallDir, base / "target" / "no_install.zip"
          )

          MagicDrawDownloader.fetchSysMLPlugin(
            s.log, showDownloadProgress,
            up,
            credentials.value,
            mdInstallDir, base / "target" / "sysml_plugin.zip"
          )

          val pfilter: DependencyFilter = new DependencyFilter {
            def apply(c: String, m: ModuleID, a: Artifact): Boolean =
              (a.`type` == "zip" || a.`type` == "resource") &&
                a.extension == "zip" &&
                (m.organization.startsWith("gov.nasa.jpl") || m.organization.startsWith("com.nomagic")) &&
                (m.name.startsWith("cae_md") ||
                  m.name.startsWith("gov.nasa.jpl.magicdraw.projectUsageIntegrityChecker") ||
                  m.name.startsWith("imce.dynamic_scripts.magicdraw.plugin") ||
                  m.name.startsWith("com.nomagic.magicdraw.package") ||
                  m.name.startsWith("gov.nasa.jpl.imce.metrology.isoiec80000.magicdraw.library"))
          }
          val ps: Seq[File] = up.matching(pfilter)
          ps.foreach { zip =>
            // Use unzipURL to download & extract
            val files = IO.unzip(zip, mdInstallDir)
            s.log.info(
              s"=> created md.install.dir=$mdInstallDir with ${files.size} " +
                s"files extracted from zip: ${zip.getName}")
          }

          val mdDynamicScriptsDir = mdInstallDir / "dynamicScripts"
          IO.createDirectory(mdDynamicScriptsDir)

          val zfilter: DependencyFilter = new DependencyFilter {
            def apply(c: String, m: ModuleID, a: Artifact): Boolean =
              (a.`type` == "zip" || a.`type` == "resource" || true) &&
                a.extension == "zip" &&
                (m.organization.startsWith("gov.nasa.jpl") || m.organization.startsWith("org.omg.tiwg")) &&
                !(m.name.startsWith("cae_md") ||
                  m.name.startsWith("gov.nasa.jpl.magicdraw.projectUsageIntegrityChecker") ||
                  m.name.startsWith("imce.dynamic_scripts.magicdraw.plugin") ||
                  m.name.startsWith("imce.third_party") ||
                  m.name.startsWith("gov.nasa.jpl.imce.metrology.isoiec80000.magicdraw.library"))
          }
          val zs: Seq[File] = up.matching(zfilter)
          zs.foreach { zip =>
            val files = IO.unzip(zip, mdDynamicScriptsDir)
            s.log.info(
              s"=> extracted ${files.size} DynamicScripts files from zip: ${zip.getName}")
          }

          val imceSetup = mdInstallDir / "bin" / "magicdraw.imce.setup.sh"
          if (imceSetup.exists()) {
            val setup = sbt.Process(command = "/bin/bash", arguments = Seq[String](imceSetup.getAbsolutePath)).!
            require(0 == setup, s"IMCE MD Setup error! ($setup)")
            s.log.info(s"*** Executed bin/magicdraw.imce.setup.sh script")
          } else {
            s.log.info(s"*** No bin/magicdraw.imce.setup.sh script found!")
          }
        } else
          s.log.info(
            s"=> use existing md.install.dir=$mdInstallDir")
      },

      unmanagedJars in Compile := {
        val prev = (unmanagedJars in Compile).value
        val base = baseDirectory.value
        val s = streams.value
        val _ = extractArchives.value

        val mdInstallDir = base / "target" / "md.package"

        //val depJars = ((base / "lib") ** "*").filter{f => f.isDirectory && ((f) * "*.jar").get.nonEmpty}.get.map(Attributed.blank)
        val depJars = ((base / "lib") ** "*.jar").get.map(Attributed.blank)

        //val libJars = (mdInstallDir ** "*").filter{f => f.isDirectory && ((f) * "*.jar").get.nonEmpty}.get.map(Attributed.blank)
        val mdLibJars = ((mdInstallDir / "lib") ** "*.jar").get.map(Attributed.blank)
        val mdPluginLibJars = ((mdInstallDir / "plugins") ** "*.jar").get.map(Attributed.blank)
        val pGApp = ((mdInstallDir / "dynamicScripts" / "gov.nasa.jpl.imce.profileGenerator.application") ** "*.jar").get.map(Attributed.blank)
	val pGModelBundle = ((mdInstallDir / "dynamicScripts" / "gov.nasa.jpl.imce.profileGenerator.model.bundle") ** "*.jar").get.map(Attributed.blank)
	val pGModelProfile = ((mdInstallDir / "dynamicScripts" / "gov.nasa.jpl.imce.profileGenerator.model.profile") ** "*.jar").get.map(Attributed.blank)
	//val pGLibs = (file("target/profileGenerator") ** "*.jar").get.map(Attributed.blank)

        val allJars = mdLibJars ++ mdPluginLibJars ++ pGApp ++ pGModelBundle ++ pGModelProfile ++ depJars ++ prev

        s.log.info(s"=> Adding ${allJars.size} unmanaged jars")

        allJars
      },

      unmanagedJars in Test := (unmanagedJars in Compile).value,

      unmanagedClasspath in Test := (unmanagedJars in Test).value,

      compile in Compile := (compile in Compile).dependsOn(extractArchives).value,

      resolvers += Resolver.bintrayRepo("jpl-imce", "gov.nasa.jpl.imce"),
      resolvers += Resolver.bintrayRepo("tiwg", "org.omg.tiwg"),

      compile in Test := {
        val _ = extractArchives.value
        (compile in Test).value
      },

      unmanagedClasspath in Compile ++= (unmanagedJars in Compile).value

      //unmanagedClasspath in Test += baseDirectory.value / "target" / "extracted" / "gov.nasa.jpl.imce.ontologies"
      // for local development, use this instead:
      //unmanagedClasspath in Test += baseDirectory.value / ".." / "gov.nasa.jpl.imce.ontologies" / "gov.nasa.jpl.imce.ontologies/"
    )
    .dependsOnSourceProjectOrLibraryArtifacts(
      "imce-magicdraw-dynamicscripts-batch",
      "imce.magicdraw.dynamicscripts.batch",
      Seq(
        "org.omg.tiwg"
          %% "imce.magicdraw.dynamicscripts.batch"
          % "3.15.1"
          artifacts
          Artifact("imce.magicdraw.dynamicscripts.batch", "zip", "zip", Some("resource"), Seq(), None, Map())
      )
    )

def dynamicScriptsResourceSettings(projectName: String): Seq[Setting[_]] = {

  import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._

  def addIfExists(f: File, name: String): Seq[(File, String)] =
    if (!f.exists) Seq()
    else Seq((f, name))

  val QUALIFIED_NAME = "^[a-zA-Z][\\w_]*(\\.[a-zA-Z][\\w_]*)*$".r

  Seq(
    // the '*-resource.zip' archive will start from: 'dynamicScripts'
    com.typesafe.sbt.packager.Keys.topLevelDirectory in Universal := None,

    // name the '*-resource.zip' in the same way as other artifacts
    com.typesafe.sbt.packager.Keys.packageName in Universal :=
      normalizedName.value + "_" + scalaBinaryVersion.value + "-" + version.value + "-resource",

    // contents of the '*-resource.zip' to be produced by 'universal:packageBin'
    mappings in Universal ++= {
      val dir = baseDirectory.value
      val bin = (packageBin in Compile).value
      val src = (packageSrc in Compile).value
      val doc = (packageDoc in Compile).value
      val binT = (packageBin in Test).value
      val srcT = (packageSrc in Test).value
      val docT = (packageDoc in Test).value

      (dir * ".classpath").pair(rebase(dir, projectName)) ++
        (dir * "*.md").pair(rebase(dir, projectName)) ++
        (dir / "resources" ***).pair(rebase(dir, projectName)) ++
        addIfExists(bin, projectName + "/lib/" + bin.name) ++
        addIfExists(binT, projectName + "/lib/" + binT.name) ++
        addIfExists(src, projectName + "/lib.sources/" + src.name) ++
        addIfExists(srcT, projectName + "/lib.sources/" + srcT.name) ++
        addIfExists(doc, projectName + "/lib.javadoc/" + doc.name) ++
        addIfExists(docT, projectName + "/lib.javadoc/" + docT.name)
    },

    artifacts += {
      val n = (name in Universal).value
      Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map())
    },
    packagedArtifacts += {
      val p = (packageBin in Universal).value
      val n = (name in Universal).value
      Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) -> p
    }
  )
}
