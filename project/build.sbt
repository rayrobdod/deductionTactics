sbtPlugin := true

(unmanagedSourceDirectories in Compile) := (unmanagedSourceDirectories in Compile).value :+
		(baseDirectory.value / ".." / "src" / "main" / "scala").getCanonicalFile()

(unmanagedClasspath in Compile) := (unmanagedClasspath in Compile).value :+
		Attributed.blank((baseDirectory.value / ".." / "lib" / "svgSalamander.jar").getCanonicalFile())

libraryDependencies += ("com.rayrobdod" %% "utilities" % "20140518")

libraryDependencies += ("com.rayrobdod" %% "json" % "2.0-RC2")

libraryDependencies += ("net.sf.opencsv" % "opencsv" % "2.3")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "3.0.0-SNAPSHOT-20150616")

excludeFilter in unmanagedSources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath contains "com/rayrobdod/deductionTactics/consoleView") ||
			(abPath contains "com/rayrobdod/deductionTactics/swingView") ||
			(abPath contains "com/rayrobdod/deductionTactics/main") ||
			(abPath contains "com/rayrobdod/deductionTactics/ai") ||
			false
		)
	}
}

