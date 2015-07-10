addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.2")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.1.0")

// only works with scala 2.11
// addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "0.94.6")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

//addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")
