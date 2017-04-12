import scoverage.ScoverageKeys
import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}

val btVersion: String = {
  Try(ConfigFactory.load.getString("version")) match {
    case Success(ver) => ver
    case Failure(_) => "0.1.0"
  }
}

name := """session-store"""
version := btVersion
scalaVersion := "2.11.10"
organization := "com.cjww-dev.apps"

lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;views.*;models.*;config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(playSettings ++ scoverageSettings : _*)

PlayKeys.devSettings := Seq("play.server.http.port" -> "8400")

val cjwwDep: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" % "data-security_2.11" % "0.6.0",
  "com.cjww-dev.libs" % "logging_2.11" % "0.2.0",
  "com.cjww-dev.libs" % "reactive-mongo_2.11" % "0.7.0",
  "com.cjww-dev.libs" % "bootstrapper_2.11" % "0.6.0",
  "com.cjww-dev.libs" % "backend-auth_2.11" % "0.4.0"
)

val testDep: Seq[ModuleID] = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.mockito" % "mockito-core" % "2.7.22" % Test
)

libraryDependencies ++= cjwwDep
libraryDependencies ++= testDep

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "cjww-dev" at "http://dl.bintray.com/cjww-development/releases"

herokuAppName in Compile := "cjww-session-store"
