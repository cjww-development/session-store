import scoverage.ScoverageKeys
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_) => "0.1.0"
}

name := """session-store"""
version := btVersion
scalaVersion := "2.11.11"
organization := "com.cjww-dev.backends"

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;/.data/..*;views.*;models.*;config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(scoverageSettings : _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
    parallelExecution in IntegrationTest := false)

PlayKeys.devSettings := Seq("play.server.http.port" -> "8400")

val cjwwDep: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" % "data-security_2.11"      % "0.10.0",
  "com.cjww-dev.libs" % "logging_2.11"            % "0.5.0",
  "com.cjww-dev.libs" % "reactive-mongo_2.11"     % "1.12.0",
  "com.cjww-dev.libs" % "bootstrapper_2.11"       % "1.4.2",
  "com.cjww-dev.libs" % "backend-auth_2.11"       % "0.14.0"
)

val testDep: Seq[ModuleID] = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.mockito" % "mockito-core" % "2.7.22" % Test
)

libraryDependencies ++= cjwwDep
libraryDependencies ++= testDep

resolvers += "cjww-dev" at "http://dl.bintray.com/cjww-development/releases"

herokuAppName in Compile := "cjww-session-store"

bintrayOrganization := Some("cjww-development")
bintrayReleaseOnPublish in ThisBuild := true
bintrayRepository := "releases"
bintrayOmitLicense := true
