name := "Deduction Tactics"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "SNAPSHOT"

scalaVersion := "2.10.7"

crossScalaVersions := Seq("2.10.7", "2.11.12")

// heavy resource use, including ResourceBundles
fork := true

// proguard doesn't see the META-INF without this
exportJars := true

mainClass in Compile := Some("com.rayrobdod.deductionTactics.main.Main")

resolvers += ("rayrobdod" at "http://ivy.rayrobdod.name/")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "20160112")

libraryDependencies += ("com.opencsv" % "opencsv" % "3.4")

libraryDependencies += ("com.rayrobdod" %% "tile-view-swing" % "4.0-SNAPSHOT")



packageOptions in (Compile, packageBin) += {
	val manifest = new java.util.jar.Manifest()
	manifest.getEntries().put("scala/", {
		val attrs = new java.util.jar.Attributes()
		attrs.putValue("Implementation-Title", "Scala")
		attrs.putValue("Implementation-URL", "http://www.scala-lang.org/")
		attrs.putValue("Implementation-Version", scalaVersion.value)
		attrs
	})
	manifest.getEntries().put("com/kitfox/svg/", {
		val attrs = new java.util.jar.Attributes()
		attrs.putValue("Implementation-Title", "SvgSalamander")
		attrs.putValue("Implementation-URL", "http://svgsalamander.java.net/")
		attrs.putValue("Implementation-Date", "January 11 2013")
		attrs
	})
	manifest.getMainAttributes().putValue("Implementation-URL", "http://rayrobdod.name/programming/java/programs/deductionTactics/")
	Package.JarManifest( manifest )
}


javacOptions in Compile ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

scalacOptions ++= (scalaBinaryVersion.value match {
	case "2.10" | "2.11" => Seq("-target:jvm-1.7")
	case _ => Seq("-target:jvm-1.8")
})

scalacOptions ++= (scalaBinaryVersion.value match {
	case "2.10" => Seq()
	case _ => Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint:_", "-Xlint:-adapted-args", "-Xfuture", "-Xcheckinit")
})

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

mappings in (Compile, packageSrc) += (new File(baseDirectory.value, "LICENSE.txt"), "LICENSE.txt" )

mappings in (Compile, packageBin) += (new File(baseDirectory.value, "LICENSE.txt"), "LICENSE.txt" )



// scalaTest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

testOptions in Test += Tests.Argument("-oS",
  // to allow appveyor to show tests in friendly view
  "-u", s"${crossTarget.value}/test-results-junit"
)

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

// proguard
enablePlugins(SbtProguard)

val proguardType = settingKey[String]("level of proguard compression")

proguardType := "mini" // "micro"

proguardVersion in Proguard := "6.0"

proguardOptions in Proguard += "-include " + ((baseDirectory in Compile).value / (proguardType.value + ".proguard"))

proguardInputFilter in Proguard := { file =>
	if (file.name.startsWith("deduction-tactics")) {
		None
	} else if (file.name.startsWith("rt")) {
		Some("**.class;java.**;javax.**")
	} else {
		Some("**.class")
	}
}

proguardOptions in Proguard := (proguardOptions in Proguard).value.map{line =>
	if (line contains "scala") {line.replaceAll("-libraryjars (.+)", "-injars $1(**.class)")} else {line}
}

artifactPath in Proguard := {
	(artifactPath in Proguard).value.getParentFile() / (s"deductionTactics-fatjar-${proguardType.value}.jar")
}

javaOptions in (Proguard, proguard) += "-Xmx2G"
