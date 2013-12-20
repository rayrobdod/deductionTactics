name := "Deduction Tactics"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "a.5.3-SNAPSHOT"

scalaVersion := "2.9.3"

crossScalaVersions ++= Seq("2.9.1", "2.9.2", "2.9.3", "2.10.2", "2.11.0-M4")

exportJars := true

mainClass := Some("com.rayrobdod.deductionTactics.main.Main")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "SNAPSHOT")

libraryDependencies += ("com.rayrobdod" %% "json" % "1.0.0")

libraryDependencies += ("net.sf.opencsv" % "opencsv" % "2.3")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "2.1.0-SNAPSHOT")



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
	if (sv.take(3) == "2.1") {Seq("-feature", "-language:implicitConversions")} else {Nil}
}

excludeFilter in unmanagedSources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/ConsoleInterface_CFN.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/WithConsoleViewport.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/consoleView/CommandParser.scala")
		)
	}
}

excludeFilter in unmanagedResources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			((abPath contains "/Hits/") && !((abPath endsWith "/Hits/Hit.wav") || (abPath endsWith "/Hits/license.txt"))) ||
			(abPath endsWith "deductionTacticsCombined.svg") ||
			(abPath endsWith "tokenClasses/basic.json.php") ||
			(abPath contains "tokenClasses/sportsmen/") ||
			(abPath contains "tilemaps/Field Chess") ||
			((abPath contains "deductionTactics/maps/") && (abPath endsWith ".png"))
		)
	}
}


// license nonsense
licenses += (("GPLv3 or later", new java.net.URL("http://www.gnu.org/licenses/") ))

mappings in (Compile, packageSrc) <+= baseDirectory.map{(b) => (new File(b, "LICENSE.txt"), "LICENSE.txt" )}

mappings in (Compile, packageBin) <+= baseDirectory.map{(b) => (new File(b, "LICENSE.txt"), "LICENSE.txt" )}


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

// if some part of the circular dependency breaks down, remove this line
resourceGenerators in Compile <+= compileTokens.task

resourceGenerators in Compile <+= genBasicTokens.task

// proguard
proguardSettings

proguardType := "mini" 

ProguardKeys.options in Proguard <+= (baseDirectory in Compile, proguardType).map{"-include '"+_+"/"+_+".proguard'"}

ProguardKeys.inputFilter in Proguard := { file =>
	if (file.name.startsWith("deduction-tactics")) {
		None
	} else if (file.name.startsWith("rt")) {
		Some("**.class;java.**;javax.**")
	} else {
		Some("**.class")
	}
}

artifactPath in Proguard <<= (artifactPath in Proguard, proguardType, version).apply{(orig:File, level:String, version:String) =>
	orig.getParentFile() / ("deductionTactics-" + version + "-full-" + level + ".jar")
}

javaOptions in (Proguard, ProguardKeys.proguard) += "-Xmx2G"

// anon-fun-reduce
autoCompilerPlugins := true

addCompilerPlugin("com.rayrobdod" %% "anon-fun-reduce" % "1.0.0")

libraryDependencies += ("com.rayrobdod" %% "anon-fun-reduce" % "1.0.0")

