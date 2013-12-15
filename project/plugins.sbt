addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.1")

libraryDependencies += ("com.rayrobdod" %% "deduction-tactics-meta" % "a.5.2")

libraryDependencies += ("com.rayrobdod" %% "deduction-tactics" % "a.5.2")

resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")
