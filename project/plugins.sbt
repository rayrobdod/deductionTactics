addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.2")

libraryDependencies += ("com.rayrobdod" %% "deduction-tactics" % "a.6.0-SNAPSHOT")

resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

//addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")
