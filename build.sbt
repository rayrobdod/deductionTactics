name := "Deduction Tactics"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "a.6.0-SNAPSHOT"

scalaVersion := "2.10.5"

crossScalaVersions := Seq("2.10.5", "2.11.7")

// heavy resource use, including ResourceBundles
fork := true

mainClass := Some("com.rayrobdod.deductionTactics.main.Main")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "20140518")

libraryDependencies += ("com.rayrobdod" %% "json" % "2.0-RC4")

libraryDependencies += ("com.opencsv" % "opencsv" % "3.4")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "3.0-RC1")



packageOptions in (Compile, packageBin) <+= (scalaVersion, sourceDirectory).map{(scalaVersion:String, srcDir:File) =>
	val manifest = new java.util.jar.Manifest(new java.io.FileInputStream(srcDir + "/main/MANIFEST.MF"))
	//
	manifest.getAttributes("scala/").putValue("Implementation-Version", scalaVersion)
	//
	Package.JarManifest( manifest )
}



javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", 
			"-language:implicitConversions"
)

excludeFilter in unmanagedSources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/GangUpAI.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/NetworkClient.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/WithNetworkServer.scala") ||
			//
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/ConsoleInterface_CFN.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/ai/WithConsoleViewport.scala") ||
			(abPath endsWith "com/rayrobdod/deductionTactics/consoleView/CommandParser.scala") ||
			(abPath contains "com/rayrobdod/deductionTactics/swingView/ChooserFrame.scala") ||
			(abPath contains "com/rayrobdod/deductionTactics/main/SimpleStart") ||
			false
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
excludeFilter in unmanagedResources in Compile := {
	(excludeFilter in unmanagedResources in Compile).value || (includeFilter in compileTokens).value
}


// scalaTest
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % "test"

testOptions in Test += Tests.Argument("-oS")

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

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


