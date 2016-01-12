sbtPlugin := true

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

(unmanagedSourceDirectories in Compile) := (unmanagedSourceDirectories in Compile).value :+
		(baseDirectory.value / ".." / "src" / "main" / "scala").getCanonicalFile()

(unmanagedClasspath in Compile) := (unmanagedClasspath in Compile).value :+
		Attributed.blank((baseDirectory.value / ".." / "lib" / "svgSalamander.jar").getCanonicalFile())

resolvers += ("rayrobdod" at "http://ivy.rayrobdod.name/")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "20160112")

libraryDependencies += ("com.rayrobdod" %% "json" % "2.0-RC6")

libraryDependencies += ("com.opencsv" % "opencsv" % "3.4")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "3.0.0-RC2")


excludeFilter in unmanagedSources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath contains "com/rayrobdod/deductionTactics/consoleView") ||
			(abPath contains "com/rayrobdod/deductionTactics/swingView") ||
			(abPath contains "com/rayrobdod/deductionTactics/main") ||
			((abPath contains "com/rayrobdod/deductionTactics/ai/") && !(abPath contains "/package.scala")) ||
			false
		)
	}
}

