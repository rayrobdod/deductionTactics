name := "Deduction Tactics"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "a.6.1-SNAPSHOT"

scalaVersion := "2.10.6"

crossScalaVersions := Seq("2.10.6", "2.11.7")

// heavy resource use, including ResourceBundles
fork := true

// proguard doesn't see the META-INF without this
exportJars := true

mainClass in Compile := Some("com.rayrobdod.deductionTactics.main.Main")

resolvers += ("rayrobdod" at "http://ivy.rayrobdod.name/")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "20160112")

libraryDependencies += ("com.rayrobdod" %% "json" % "2.0-RC6")

libraryDependencies += ("com.opencsv" % "opencsv" % "3.4")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "3.0.0-RC2")



packageOptions in (Compile, packageBin) <+= (scalaVersion, sourceDirectory).map{(scalaVersion:String, srcDir:File) =>
	val manifest = new java.util.jar.Manifest(new java.io.FileInputStream(srcDir + "/main/MANIFEST.MF"))
	//
	manifest.getAttributes("scala/").putValue("Implementation-Version", scalaVersion)
	//
	Package.JarManifest( manifest )
}



javacOptions in Compile ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.7")

scalacOptions in doc in Compile ++= Seq(
		"-doc-title", name.value,
		"-doc-version", version.value,
		"-doc-root-content", ((scalaSource in Compile).value / "rootdoc.txt").toString,
		"-diagrams",
		"-sourcepath", baseDirectory.value.toString,
		"-doc-source-url", "https://github.com/rayrobdod/deductionTactics/tree/" + version.value + "€{FILE_PATH}.scala"
)

autoAPIMappings in doc in Compile := true

excludeFilter in unmanagedSources in Compile := new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
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
			(abPath contains "tokenClasses/sportsmen/") ||
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

ProguardKeys.proguardVersion in Proguard := "5.2.1"

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
	orig.getParentFile() / ("deductionTactics-" + version + "-withdebug-" + level + ".jar")
}

javaOptions in (Proguard, ProguardKeys.proguard) += "-Xmx2G"
