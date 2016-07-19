import java.io.File

import sbt.Keys._
import sbt._
import spray.json._
import DefaultJsonProtocol._
import com.typesafe.sbt.pgp.PgpKeys
import gov.nasa.jpl.imce.sbt._
import gov.nasa.jpl.imce.sbt.ProjectHelper._

useGpg := true

fork in run := true

developers := List(
  Developer(
    id="sherzig",
    name="Sebastian J. Herzig",
    email="sebastian.j.herzig@jpl.nasa.gov",
    url=url("https://gateway.jpl.nasa.gov/personal/sherzig/default.aspx")),
  Developer(
    id="rouquett",
    name="Nicolas F. Rouquette",
    email="nicolas.f.rouquette@jpl.nasa.gov",
    url=url("https://gateway.jpl.nasa.gov/personal/rouquett/default.aspx")))

lazy val mdInstallDirectory = SettingKey[File]("md-install-directory", "MagicDraw Installation Directory")

mdInstallDirectory in Global :=
  baseDirectory.value / "target" / "md.package"

import scala.io.Source
import scala.util.control.Exception._

def docSettings(diagrams:Boolean): Seq[Setting[_]] =
  Seq(
    sources in (Compile,doc) <<= (git.gitUncommittedChanges, sources in (Compile,compile)) map {
      (uncommitted, compileSources) =>
        if (uncommitted)
          Seq.empty
        else
          compileSources
    },

    sources in (Test,doc) <<= (git.gitUncommittedChanges, sources in (Test,compile)) map {
      (uncommitted, testSources) =>
        if (uncommitted)
          Seq.empty
        else
          testSources
    },

    scalacOptions in (Compile,doc) ++=
      (if (diagrams)
        Seq("-diagrams", "-diagrams-dot-path", "/usr/bin/dot", "-verbose", "-diagrams-debug")
      else
        Seq()
        ) ++
        Seq(
          "-doc-title", name.value,
          "-doc-root-content", baseDirectory.value + "/rootdoc.txt"
        ),
    autoAPIMappings := ! git.gitUncommittedChanges.value
    //    apiMappings <++=
    //      ( git.gitUncommittedChanges,
    //        dependencyClasspath in Compile in doc,
    //        IMCEKeys.nexusJavadocRepositoryRestAPIURL2RepositoryName,
    //        IMCEKeys.pomRepositoryPathRegex,
    //        streams ) map { (uncommitted, deps, repoURL2Name, repoPathRegex, s) =>
    //        if (uncommitted)
    //          Map[File, URL]()
    //        else
    //          (for {
    //            jar <- deps
    //            url <- jar.metadata.get(AttributeKey[ModuleID]("moduleId")).flatMap { moduleID =>
    //              val urls = for {
    //                (repoURL, repoName) <- repoURL2Name
    //                (query, match2publishF) = IMCEPlugin.nexusJavadocPOMResolveQueryURLAndPublishURL(
    //                  repoURL, repoName, moduleID)
    //                url <- nonFatalCatch[Option[URL]]
    //                  .withApply { (_: java.lang.Throwable) => None }
    //                  .apply({
    //                    val conn = query.openConnection.asInstanceOf[java.net.HttpURLConnection]
    //                    conn.setRequestMethod("GET")
    //                    conn.setDoOutput(true)
    //                    repoPathRegex
    //                      .findFirstMatchIn(Source.fromInputStream(conn.getInputStream).getLines.mkString)
    //                      .map { m =>
    //                        val javadocURL = match2publishF(m)
    //                        s.log.info(s"Javadoc for: $moduleID")
    //                        s.log.info(s"= mapped to: $javadocURL")
    //                        javadocURL
    //                      }
    //                  })
    //              } yield url
    //              urls.headOption
    //            }
    //          } yield jar.data -> url).toMap
    //      }
  )

resolvers := {
  val previous = resolvers.value
  if (git.gitUncommittedChanges.value)
    Seq[Resolver](Resolver.mavenLocal) ++ previous
  else
    previous
}

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

lazy val mdRoot = SettingKey[File]("md-root", "MagicDraw Installation Directory")

lazy val specsRoot = SettingKey[File]("specs-root", "MagicDraw DynamicScripts Test Specification Directory")

lazy val runMDTests = taskKey[Unit]("Run MagicDraw DynamicScripts Unit Tests")

lazy val artifactZipFile = taskKey[File]("Location of the zip artifact file")

lazy val zipInstall = TaskKey[File]("zip-install", "Zip the resources")

/*
 * For now, we can't compile in strict mode because the Scala macros used for generating the JSon adapters
 * results in a compilation warning:
 *
 * Warning:(1, 0) Unused import
 * / *
 * ^
 *
 */
lazy val core = Project("gov-nasa-jpl-imce-profileGenerator", file("."))
  .enablePlugins(IMCEGitPlugin)
  .enablePlugins(IMCEReleasePlugin)
  .settings(dynamicScriptsResourceSettings("gov.nasa.jpl.imce.profileGenerator"))
  //.settings(IMCEPlugin.strictScalacFatalWarningsSettings)
  .settings(IMCEReleasePlugin.packageReleaseProcessSettings)
  .settings(addArtifact(Artifact("imce_md18_0_sp5_profiles_libraries_resource", "zip", "zip"), artifactZipFile).settings: _*)
  .dependsOnSourceProjectOrLibraryArtifacts(
    "gov-nasa-jpl-imce-profileGenerator-model-bundle",
    "gov.nasa.jpl.imce.profileGenerator.model.bundle",
    Seq(
      //      //  extra("artifact.kind" -> "generic.library")
      "gov.nasa.jpl.imce" %% "profileGenerator-model-bundle"
        % Versions_profileGenerator_model_bundle.version %
        "compile" withSources() withJavadoc() artifacts
        Artifact("profileGenerator-model-bundle", "zip", "zip", Some("resource"), Seq(), None, Map())
    )
  )
  .dependsOnSourceProjectOrLibraryArtifacts(
    "gov-nasa-jpl-imce-profileGenerator-model-profile",
    "gov.nasa.jpl.imce.profileGenerator.model.profile",
    Seq(
      //      //  extra("artifact.kind" -> "generic.library")
      "gov.nasa.jpl.imce" %% "profileGenerator-model-profile"
        % Versions_profileGenerator_model_profile.version %
        "compile" withSources() withJavadoc() artifacts
        Artifact("profileGenerator-model-profile", "zip", "zip", Some("resource"), Seq(), None, Map())
    )
  )
  .dependsOnSourceProjectOrLibraryArtifacts(
    "gov-nasa-jpl-imce-magicdraw-plugins-cae_md18_0_sp5_puic",
    "gov.nasa.jpl.imce.magicdraw.plugins.cae_md18_0_sp5_puic",
    Seq(
      //      //  extra("artifact.kind" -> "generic.library")
      "gov.nasa.jpl.imce.magicdraw.plugins" % "cae_md18_0_sp5_puic"
        % Versions_projectUsageIntegrityChecker.version %
        "compile" withSources() withJavadoc() artifacts
        Artifact("cae_md18_0_sp5_puic", "zip", "zip", Some("resource"), Seq(), None, Map())
    )
  )
  .dependsOnSourceProjectOrLibraryArtifacts(
    "oti-uml-magicdraw-adapter",
    "org.omg.oti.uml.magicdraw.adapter",
    Seq(
      "org.omg.tiwg" %% "oti-uml-magicdraw-adapter"
        % Versions_oti_uml_magicdraw_adapter.version % "compile"
        withSources() withJavadoc() artifacts
        Artifact("oti-uml-magicdraw-adapter", "zip", "zip", Some("resource"), Seq(), None, Map())
    )
  )
  .settings(
    IMCEKeys.licenseYearOrRange := "2016",
    IMCEKeys.organizationInfo := IMCEPlugin.Organizations.omf,
    IMCEKeys.targetJDK := IMCEKeys.jdk18.value,
    git.baseVersion := Versions.version,

    organization := "gov.nasa.jpl.imce.magicdraw.resources",
    name := "imce_md18_0_sp5_profiles_libraries_resource",
    organizationHomepage :=
      Some(url("https://github.jpl.nasa.gov/imce/gov.nasa.jpl.imce.team")),

    buildInfoPackage := "gov.nasa.jpl.imce.profileGenerator",
    buildInfoKeys ++= Seq[BuildInfoKey](BuildInfoKey.action("buildDateUTC") { buildUTCDate.value }),

    projectID := {
      val previous = projectID.value
      previous.extra(
        "build.date.utc" -> buildUTCDate.value,
        "artifact.kind" -> "generic.library")
    },

    mappings in (Compile, packageSrc) ++= {
      import Path.{flat, relativeTo}
      val base = (sourceManaged in Compile).value
      val srcs = (managedSources in Compile).value
      srcs x (relativeTo(base) | flat)
    },

    // disable using the Scala version in output paths and artifacts
    crossPaths := false,

    artifactZipFile := {
      baseDirectory.value / "target" / s"imce_md18_0_sp5_profiles_libraries-${version.value}-resource.zip"
    },

    addArtifact(Artifact("imce_md18_0_sp5_profiles_libraries_resource", "zip", "zip", Some("resource"), Seq(), None, Map()),
      artifactZipFile),

    sources in doc in Compile := List(),

    publish <<= publish dependsOn zipInstall,
    PgpKeys.publishSigned <<= PgpKeys.publishSigned dependsOn zipInstall,

    publishLocal <<= publishLocal dependsOn zipInstall,
    PgpKeys.publishLocalSigned <<= PgpKeys.publishLocalSigned dependsOn zipInstall,

    libraryDependencies += "org.jgrapht" % "jgrapht-ext" % "0.9.0",

    // TODO Needs modification:
    //   1. Currently, output expected in products / imce.profiles_libraries directory
    //   2. XML descriptor needs to be generated
    zipInstall <<=
      (baseDirectory, update, streams,
        mdInstallDirectory in ThisBuild,
        artifactZipFile,
        makePom, buildUTCDate
        ) map {
        (base, up, s, mdInstallDir, zip, pom, d) =>

          import com.typesafe.sbt.packager.universal._

          val root = base / "target" / "imce_md18_0_sp5_profiles_libraries"
          s.log.info(s"\n*** top: $root")
          s.log.info(s"\n*** zip: ${zip}")
          s.log.info(s"\n*** zip: ${zip.getCanonicalFile} (canonical)")

          // This is likely where the profiles are stored that are produced by the profile generator
          // Looks like this expects: modelLibraries/... and profiles/... to be populated already
          IO.copyDirectory(
            base / "products" / "imce.profiles_libraries",
            root, overwrite=true, preserveLastModified=true)

          // Create other necessary plugin configs
          val resourceManager = root / "data" / "resourcemanager"
          IO.createDirectory(resourceManager)
          val resourceDescriptorFile = resourceManager / "MDR_IMCE_ProfilesLibraries_74997_descriptor.xml"

          // TODO This needs to be generated... or does it?
          val resourceDescriptorInfo =
            <resourceDescriptor critical="false" date={d}
                                description="IMCE Profiles &amp; Libraries"
                                group="IMCE Resource"
                                homePage="https://github.jpl.nasa.gov/imce/imce.qvto.profileGenerator"
                                id="74997"
                                mdVersionMax="higher"
                                mdVersionMin="18.0"
                                name="IMCEProfiles"
                                product="IMCE Profiles And Libraries"
                                restartMagicdraw="false" type="Profile">
              <version human={Versions.version} internal={Versions.version} resource={Versions.version + "0"}/>
              <provider email="sebastian.j.herzig@jpl.nasa.gov"
                        homePage="https://github.jpl.nasa.gov/imce"
                        name="IMCE"/>
              <edition>Reader</edition>
              <edition>Community</edition>
              <edition>Standard</edition>
              <edition>Professional Java</edition>
              <edition>Professional C++</edition>
              <edition>Professional C#</edition>
              <edition>Professional ArcStyler</edition>
              <edition>Professional EFFS ArcStyler</edition>
              <edition>OptimalJ</edition>
              <edition>Professional</edition>
              <edition>Architect</edition>
              <edition>Enterprise</edition>
              <installation>
                <file from="modelLibraries/IMCE/IMCE.DC.mdzip"
                      to="modelLibraries/IMCE/IMCE.DC.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.DI.mdzip"
                      to="modelLibraries/IMCE/IMCE.DI.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.IEC80000-13 Information Science and Technology.mdzip"
                      to="modelLibraries/IMCE/IMCE.IEC80000-13 Information Science and Technology.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.IEC80000-6 Electromagnetism.mdzip"
                      to="modelLibraries/IMCE/IMCE.IEC80000-6 Electromagnetism.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO-80000-All.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO-80000-All.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO80000-1 General.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO80000-1 General.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO80000-10 Atomic and Nuclear Physics.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO80000-10 Atomic and Nuclear Physics.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO80000-3 Space and Time.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO80000-3 Space and Time.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO80000-4 Mechanics.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO80000-4 Mechanics.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO80000-5 Thermodynamics.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO80000-5 Thermodynamics.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO80000-7 Light.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO80000-7 Light.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.ISO80000-9 Physical Chemistry and Molecular Physics.mdzip"
                      to="modelLibraries/IMCE/IMCE.ISO80000-9 Physical Chemistry and Molecular Physics.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.SysMLDI.mdzip"
                      to="modelLibraries/IMCE/IMCE.SysMLDI.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.UML2.5.mdzip"
                      to="modelLibraries/IMCE/IMCE.UML2.5.mdzip"/>
                <file from="modelLibraries/IMCE/IMCE.UMLDI.mdzip"
                      to="modelLibraries/IMCE/IMCE.UMLDI.mdzip"/>
                <file from="modelLibraries/IMM/XMLSchema Metamodel.mdzip"
                      to="modelLibraries/IMM/XMLSchema Metamodel.mdzip"/>
                <file from="profiles/IMCE/IMCE.BlockSpecificTypeModelingAndAnalysis.mdzip"
                      to="profiles/IMCE/IMCE.BlockSpecificTypeModelingAndAnalysis.mdzip"/>
                <file from="profiles/IMCE/IMCE.owl2-mof2.mdzip"
                      to="profiles/IMCE/IMCE.owl2-mof2.mdzip"/>
                <file from="profiles/IMCE/QVTOValidation.mdzip"
                      to="profiles/IMCE/QVTOValidation.mdzip"/>
                <file from="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/foundation/project/project-bundle.mdzip"
                      to="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/foundation/project/project-bundle.mdzip"/>
                <file from="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/www.omg.org/spec/SysML/20140311/Metrology-bundle.mdzip"
                      to="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/www.omg.org/spec/SysML/20140311/Metrology-bundle.mdzip"/>
              </installation>
            </resourceDescriptor>


          xml.XML.save(
            filename=resourceDescriptorFile.getAbsolutePath,
            node=resourceDescriptorInfo,
            enc="UTF-8")

          val fileMappings = (root.*** --- root) pair relativeTo(root)
          ZipHelper.zipNIO(fileMappings, zip.getCanonicalFile)

          s.log.info(s"\n*** Created the zip: $zip")
          zip
      },

    scalaSource in Compile :=
      baseDirectory.value / "src" / "main" / "scala",

    unmanagedSourceDirectories in Compile +=
      baseDirectory.value / "src-gen",

    resourceDirectory in Compile :=
      baseDirectory.value / "resources",

    scalaSource in Test :=
      baseDirectory.value / "src" / "test" / "scala",

    resourceDirectory in Test :=
      baseDirectory.value / "resources",

    // disable publishing the jar produced by `test:package`
    publishArtifact in(Test, packageBin) := false,

    // disable publishing the test API jar
    publishArtifact in(Test, packageDoc) := false,

    // disable publishing the test sources jar
    publishArtifact in(Test, packageSrc) := false,

    unmanagedClasspath in Compile <++= unmanagedJars in Compile,

    libraryDependencies += "org.yaml" % "snakeyaml" % Versions_snakeyaml.version,

    libraryDependencies +=
      "gov.nasa.jpl.imce.thirdParty" %% "other-scala-libraries" % Versions_other_scala_libraries.version artifacts
        Artifact("other-scala-libraries", "zip", "zip", Some("resource"), Seq(), None, Map()),

    libraryDependencies +=
      "gov.nasa.jpl.imce.magicdraw.plugins" % "cae_md18_0_sp5_puic" % Versions_projectUsageIntegrityChecker.version artifacts
    Artifact("cae_md18_0_sp5_puic", "zip", "zip", Some("resource"), Seq(), None, Map()),

    extractArchives <<= (baseDirectory, update, streams) map {
      (base, up, s) =>

        val mdInstallDir = base / "target" / "md.package"
        if (!mdInstallDir.exists) {

          IO.createDirectory(mdInstallDir)

          val pfilter: DependencyFilter = new DependencyFilter {
            def apply(c: String, m: ModuleID, a: Artifact): Boolean =
              (a.`type` == "zip" || a.`type` == "resource") &&
                a.extension == "zip" &&
                (m.organization == "gov.nasa.jpl.cae.magicdraw.packages" ||
                  m.organization == "gov.nasa.jpl.imce.magicdraw.plugins")
          }
          val ps: Seq[File] = up.matching(pfilter)
          ps.foreach { zip =>
            val files = IO.unzip(zip, mdInstallDir)
            s.log.info(
              s"=> created md.install.dir=$mdInstallDir with ${files.size} " +
                s"files extracted from zip: ${zip.getName}")
          }

          // Also copy IMCE libraries & profiles (this bootstraps some manually created dependencies)
          val imceLibsProfiles: File = base / "res" / "imce_md18_0_sp5_profiles_libraries_resource_2.11-1.11.zip"
          IO.unzip(imceLibsProfiles, mdInstallDir)
          s.log.info(
            s"=> installed IMCE libraries and profiles into $mdInstallDir")

          //          val mdDynamicScriptsDir = mdInstallDir / "dynamicScripts"
          //          IO.createDirectory(mdDynamicScriptsDir)
          //
          //          val zfilter: DependencyFilter = new DependencyFilter {
          //            def apply(c: String, m: ModuleID, a: Artifact): Boolean =
          //              (a.`type` == "zip" || a.`type` == "resource") &&
          //                a.extension == "zip" &&
          //                m.organization == "org.omg.tiwg"
          //          }
          //          val zs: Seq[File] = up.matching(zfilter)
          //          zs.foreach { zip =>
          //            val files = IO.unzip(zip, mdDynamicScriptsDir)
          //            s.log.info(
          //              s"=> extracted ${files.size} DynamicScripts files from zip: ${zip.getName}")
          //          }

        } else
          s.log.info(
            s"=> use existing md.install.dir=$mdInstallDir")
    },

    unmanagedJars in Compile <++= (baseDirectory, update, streams, extractArchives) map {
      (base, up, s, _) =>

        val mdInstallDir = base / "target" / "md.package"

        val libJars = ((mdInstallDir / "lib") ** "*.jar").get
        s.log.info(s"jar libraries: ${libJars.size}")

        //        val dsJars = ((mdInstallDir / "dynamicScripts") * "*" / "lib" ** "*.jar").get
        //        s.log.info(s"jar dynamic script: ${dsJars.size}")
        //
        //        val mdJars = (libJars ++ dsJars).map { jar => Attributed.blank(jar) }
        val mdJars = libJars.map { jar => Attributed.blank(jar) }

        mdJars
    },

    compile <<= (compile in Compile) dependsOn extractArchives,

    IMCEKeys.nexusJavadocRepositoryRestAPIURL2RepositoryName := Map(
      "https://oss.sonatype.org/service/local" -> "releases",
      "https://cae-nexuspro.jpl.nasa.gov/nexus/service/local" -> "JPL",
      "https://cae-nexuspro.jpl.nasa.gov/nexus/content/groups/jpl.beta.group" -> "JPL Beta Group",
      "https://cae-nexuspro.jpl.nasa.gov/nexus/content/groups/jpl.public.group" -> "JPL Public Group"),
    IMCEKeys.pomRepositoryPathRegex := """\<repositoryPath\>\s*([^\"]*)\s*\<\/repositoryPath\>""".r

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
    mappings in Universal <++= (
      baseDirectory,
      packageBin in Compile,
      packageSrc in Compile,
      packageDoc in Compile,
      packageBin in Test,
      packageSrc in Test,
      packageDoc in Test) map {
      (dir, bin, src, doc, binT, srcT, docT) =>
        (dir ** "*.md").pair(rebase(dir, projectName)) ++
          (dir / "resources" ***).pair(rebase(dir, projectName)) ++
          addIfExists(bin, projectName + "/lib/" + bin.name) ++
          addIfExists(binT, projectName + "/lib/" + binT.name) ++
          addIfExists(src, projectName + "/lib.sources/" + src.name) ++
          addIfExists(srcT, projectName + "/lib.sources/" + srcT.name) ++
          addIfExists(doc, projectName + "/lib.javadoc/" + doc.name) ++
          addIfExists(docT, projectName + "/lib.javadoc/" + docT.name)
    },

    artifacts <+= (name in Universal) { n => Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) },
    packagedArtifacts <+= (packageBin in Universal, name in Universal) map { (p, n) =>
      Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) -> p
    }
  )
}