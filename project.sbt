
sbtPlugin := false

name := "gov.nasa.jpl.imce.profileGenerator.application"

description := "Generator from Ontological Modeling Framework (OMF) vocabularies to profiles extending UML/SysML"

moduleName := name.value

organization := "gov.nasa.jpl.imce"

organizationName := "JPL-IMCE"

organizationHomepage := Some(url(s"https://github.jpl.nasa.gov/imce/${organizationName.value}"))

homepage := Some(url(s"https://jpl-imce.github.io/${moduleName.value}"))

git.remoteRepo := s"git@github.jpl.nasa.gov/imce/${moduleName.value}.git"

scmInfo := Some(ScmInfo(
  browseUrl = url(s"https://github.jpl.nasa.gov/imce/${moduleName.value}"),
  connection = "scm:"+git.remoteRepo.value))

developers := List(
  Developer(
    id="sherzig",
    name="Sebastian J. Herzig",
    email="sebastian.j.herzig@jpl.nasa.gov",
    url=url("https://gateway.jpl.nasa.gov/personal/sherzig/default.aspx")),
  Developer(
    id="NicolasRouquette",
    name="Nicolas F. Rouquette",
    email="nicolas.f.rouquette@jpl.nasa.gov",
    url=url("https://github.com/NicolasRouquette")))

