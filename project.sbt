
sbtPlugin := false

name := "gov.nasa.jpl.imce.profileGenerator.batch"

description := "Batch execution of profile generator"

moduleName := name.value

organization := "gov.nasa.jpl.imce"

organizationName := "JPL-IMCE"

homepage := Some(url(s"https://github.com/${organizationName.value}/${moduleName.value}"))

organizationHomepage := Some(url(s"https://github.com/${organizationName.value}"))

git.remoteRepo := "git@github.com/JPL-IMCE/gov.nasa.jpl.imce.profileGenerator.batch.git"

// publish to bintray.com via: `sbt publish`
publishTo := Some(
  "JPL-IMCE" at
    s"https://api.bintray.com/content/jpl-imce/${organization.value}/${moduleName.value}/${version.value}")

scmInfo := Some(ScmInfo(
  browseUrl = url(s"https://github.jpl.nasa.gov/imce/gov.nasa.jpl.imce.ontologies.workflow"),
  connection = "scm:"+git.remoteRepo.value))

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

