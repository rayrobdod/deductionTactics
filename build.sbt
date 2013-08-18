name := "Deduction Tactics"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "a.5.0-SNAPSHOT"

scalaVersion := "2.9.3"

crossScalaVersions ++= Seq("2.9.1", "2.9.2", "2.10.2", "2.11.0-M4")

exportJars := true

mainClass := Some("com.rayrobdod.deductionTactics.main.Main")

target := new File("C:/Users/Raymond/AppData/Local/Temp/build/DeductionTactics/")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "1.0.0")

libraryDependencies += ("com.rayrobdod" %% "json" % "1.0.0-SNAPSHOT")

libraryDependencies += ("com.rayrobdod" %% "csv" % "1.0.0")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "1.0.0-SNAPSHOT")



packageOptions in (Compile, packageBin) <+= (scalaVersion, sourceDirectory).map{(scalaVersion:String, srcDir:File) =>
	val manifest = new java.util.jar.Manifest(new java.io.FileInputStream(srcDir + "/main/MANIFEST.MF"))
	//
	manifest.getAttributes("scala/").putValue("Implementation-Version", scalaVersion)
	//
	Package.JarManifest( manifest )
}



javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-unchecked", "-deprecation" )

scalacOptions <++= scalaVersion.map{(sv:String) =>
	if (sv.take(3) == "2.1") {Seq("-feature")} else {Nil}
}

excludeFilter in unmanagedSources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath endsWith "com/rayrobdod/deductionTactics/consoleView/ansiEscape/SpacePrinter.scala") ||
			(abPath contains "com/rayrobdod/deductionTactics/consoleView/") ||
			(abPath endsWith "com/rayrobdod/testing/ParserTest.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/WithNetworkServer.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/NetworkClient.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/ConsoleInterface_CFN.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/ConsoleInterface.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/FieldPotentialAI.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/WithConsoleViewport.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/CommandParser.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/TokenEventPrinter.scala")
		)
	}
}

excludeFilter in unmanagedResources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			((abPath contains "/Hits/") && !((abPath endsWith "/Hits/Hit.wav") || (abPath endsWith "/Hits/license.txt"))) ||
			(abPath endsWith "deductionTacticsCombined.svg")
		)
	}
}

// Token compiling
excludeFilter in unmanagedResources in Compile <<= (excludeFilter in unmanagedResources in Compile, compileTokensInput) apply {(previous, tokenSrc) =>
	previous || new FileFilter{
		def accept(n:File) = tokenSrc.contains(n)
	}
}

mappings in (Compile, packageSrc) <++= (compileTokensInput) map { (tokenSrc) =>
	// Not resilient to change
	tokenSrc.map{x => ((x, "com/rayrobdod/deductionTactics/tokenClasses/" + x.getName )) }
}

resourceGenerators in Compile <+= compileTokens.task


// proguard
proguardSettings

proguardType := "mini" 

ProguardKeys.options in Proguard <+= (baseDirectory in Compile, proguardType).map{"-include '"+_+"/"+_+".proguard'"}

ProguardKeys.inputFilter in Proguard := { file =>
	if (file.name.startsWith("deduction-tactics")) {
		None
	} else {
		Some("**.class")
	}
}
