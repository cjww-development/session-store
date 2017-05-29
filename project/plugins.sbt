// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.15")

// Test plugins
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0-RC1")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

// Deploy plugin
addSbtPlugin("com.heroku" % "sbt-heroku" % "1.0.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0-M8")
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")