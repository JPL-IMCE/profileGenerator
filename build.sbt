import java.io.File

import sbt.Keys._
import sbt._

import spray.json._, DefaultJsonProtocol._

import gov.nasa.jpl.imce.sbt._
import gov.nasa.jpl.imce.sbt.ProjectHelper._

useGpg := true

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

import scala.io.Source
import scala.util.control.Exception._

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

/*
 * For now, we can't compile in strict mode because the Scala macros used for generating the JSon adapters
 * results in a compilation warning:
 *
 * Warning:(1, 0) Unused import
 * / *
 * ^
 *
 */
lazy val core = Project("gov-nasa-jpl-imce-profileGenerator-model-bundle", file("."))
  .enablePlugins(IMCEGitPlugin)
  .enablePlugins(IMCEReleasePlugin)
  .settings(dynamicScriptsResourceSettings(Some("gov.nasa.jpl.imce.profileGenerator.model.bundle")))
  .settings(IMCEPlugin.strictScalacFatalWarningsSettings)
  .settings(IMCEReleasePlugin.packageReleaseProcessSettings)
  .settings(
    IMCEKeys.licenseYearOrRange := "2014-2016",
    IMCEKeys.organizationInfo := IMCEPlugin.Organizations.oti,
    IMCEKeys.targetJDK := IMCEKeys.jdk18.value,

    organization := "gov.nasa.jpl.imce",
    organizationHomepage :=
      Some(url("https://github.jpl.nasa.gov/imce/gov.nasa.jpl.imce.team")),

    buildInfoPackage := "gov.nasa.jpl.imce.profileGenerator.model.bundle",
    buildInfoKeys ++= Seq[BuildInfoKey](BuildInfoKey.action("buildDateUTC") { buildUTCDate.value }),

    projectID := {
      val previous = projectID.value
      previous.extra(
        "build.date.utc" -> buildUTCDate.value,
        "artifact.kind" -> "generic.library")
    },

    git.baseVersion := Versions.version,

    scalaSource in Compile :=
      baseDirectory.value / "src",

    unmanagedSourceDirectories in Compile +=
      baseDirectory.value / "src-gen",

    resourceDirectory in Compile :=
      baseDirectory.value / "resources",

    scalaSource in Test :=
      baseDirectory.value / "test",

    resourceDirectory in Test :=
      baseDirectory.value / "resources",

    // disable publishing the jar produced by `test:package`
    publishArtifact in(Test, packageBin) := false,

    // disable publishing the test API jar
    publishArtifact in(Test, packageDoc) := false,

    // disable publishing the test sources jar
    publishArtifact in(Test, packageSrc) := false,

    unmanagedClasspath in Compile <++= unmanagedJars in Compile,

    resolvers += Resolver.bintrayRepo("jpl-imce", "gov.nasa.jpl.imce"),
    resolvers += Resolver.bintrayRepo("tiwg", "org.omg.tiwg"),

    libraryDependencies +=
      "gov.nasa.jpl.imce" %% "imce.third_party.other_scala_libraries"
        % Versions_other_scala_libraries.version artifacts
        Artifact("imce.third_party.other_scala_libraries", "zip", "zip", Some("resource"), Seq(), None, Map()),

    extractArchives := {}
  )

def dynamicScriptsResourceSettings(dynamicScriptsProjectName: Option[String] = None): Seq[Setting[_]] = {

  import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._

  def addIfExists(f: File, name: String): Seq[(File, String)] =
    if (!f.exists) Seq()
    else Seq((f, name))

  val QUALIFIED_NAME = "^[a-zA-Z][\\w_]*(\\.[a-zA-Z][\\w_]*)*$".r

  Seq(
    // the '*-resource.zip' archive will start from: 'dynamicScripts/<dynamicScriptsProjectName>'
    com.typesafe.sbt.packager.Keys.topLevelDirectory in Universal := {
      val projectName = dynamicScriptsProjectName.getOrElse(baseDirectory.value.getName)
      require(
        QUALIFIED_NAME.pattern.matcher(projectName).matches,
        s"The project name, '$projectName` is not a valid Java qualified name")
      Some(projectName)
    },

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
      packageDoc in Test,
      streams) map {
      (base, bin, src, doc, binT, srcT, docT, s) =>
        val file2name =
          addIfExists(bin, "lib/" + bin.name) ++
            addIfExists(binT, "lib/" + binT.name) ++
            addIfExists(src, "lib.sources/" + src.name) ++
            addIfExists(srcT, "lib.sources/" + srcT.name) ++
            addIfExists(doc, "lib.javadoc/" + doc.name) ++
            addIfExists(docT, "lib.javadoc/" + docT.name)

        s.log.info(s"file2name entries: ${file2name.size}")
        s.log.info(file2name.mkString("\n"))

        file2name
    },

    artifacts <+= (name in Universal) { n => Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) },
    packagedArtifacts <+= (packageBin in Universal, name in Universal) map { (p, n) =>
      Artifact(n, "zip", "zip", Some("resource"), Seq(), None, Map()) -> p
    }
  )
}
