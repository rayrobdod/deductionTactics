name := "Deduction Tactics"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "a.5.0"

scalaVersion := "2.9.3"

crossScalaVersions ++= Seq("2.9.1", "2.10.0", "2.11.0-M4")

exportJars := true

mainClass := Some("com.rayrobdod.deductionTactics.main.Main")

target := new File("C:/Users/Raymond/AppData/Local/Temp/build/DeductionTactics/")

//scalaSource in Compile := new File("C:/Users/Raymond/Documents/Programming/Java/Games/DeductionTactics/")

javaSource in Compile := new File("C:/Users/Raymond/Documents/Programming/Java/Games/DeductionTactics/")

resourceDirectory in Compile := new File("C:/Users/Raymond/Documents/Programming/Java/Games/DeductionTactics/")

libraryDependencies += ("com.rayrobdod" %% "json" % "1.0.0")

unmanagedSourceDirectories in Compile ++= Seq(
		new File("C:/Users/Raymond/Documents/Programming/Java/Utilities/"),
		new File("C:/Users/Raymond/Documents/Programming/Java/Games/BoardGameGeneric/src/main/scala"),
		new File("C:/Users/Raymond/Documents/Programming/Java/Games/DeductionTactics"),
		new File("C:/Users/Raymond/Documents/Programming/Java/File Formats/CSV"),
		new File("C:/Users/Raymond/Documents/Programming/Java/File Formats/CFN")
)

includeFilter in Compile in unmanagedResources ~= (_ || new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath endsWith ".txt") ||
			(abPath endsWith ".png") ||
			(abPath endsWith ".json") ||
			(abPath endsWith ".svg") ||
			//(abPath endsWith ".properties") ||
			(abPath endsWith "Hit.wav") ||
			(abPath endsWith ".csv") ||
			(abPath endsWith "META-INF") || (abPath contains "META-INF/services")
		)
	}
})

includeFilter in Compile in unmanagedResources ~= (_ || new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath endsWith "build.sbt") ||
			(abPath endsWith "build.scala") ||
			(abPath endsWith "plugins.sbt") ||
			(abPath endsWith ".proguard") ||
			(abPath endsWith "deductionTactics.gdf") ||
			(abPath endsWith "deductionTactics.rc")
		)
	}
})

unmanagedJars in Compile ++= Seq(
		Attributed.blank(new File("C:/Users/Raymond/Documents/Programming/Java/Imported JAR Files/svgSalamander/0.1.12/svgSalamander.jar"))
)

packageOptions in (Compile, packageBin) <+= (scalaVersion).map{(scalaVersion:String) =>
    val manifest = new java.util.jar.Manifest(new java.io.FileInputStream("C:/Users/Raymond/Documents/Programming/Java/Games/DeductionTactics/META-INF/MANIFEST.MF"))
    //
    manifest.getAttributes("scala/").putValue("Implementation-Version", scalaVersion)
    //
    Package.JarManifest( manifest )
}



includeFilter in Compile ~= (_ || new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath contains "com/rayrobdod/animation/") ||
			(abPath contains "com/rayrobdod/util/services/") ||
			(abPath endsWith "com/rayrobdod/commonFunctionNotation/Parser.scala") ||
			(abPath endsWith "com/rayrobdod/swing/layouts/MoveToLayout.scala") ||
			(abPath endsWith "com/rayrobdod/swing/NameAndIcon.scala") ||
			(abPath endsWith "com/rayrobdod/swing/ScalaSeqListModel.scala") ||
			(abPath endsWith "com/rayrobdod/swing/NullReplaceListCellRenderer.scala") ||
			(abPath endsWith "com/rayrobdod/swing/GridBagConstraintsFactory.scala") ||
			(abPath endsWith "com/rayrobdod/util/BlitzAnimImage.java") ||
			(abPath endsWith "com/rayrobdod/util/CloneNotSupportedError.java") ||
			(abPath endsWith "com/rayrobdod/util/services/*.java") ||
			(abPath endsWith "com/rayrobdod/swing/SolidColorIcon.java") ||
			(abPath endsWith "com/rayrobdod/swing/ExitMenuItem.java") ||
			(abPath endsWith "com/rayrobdod/swing/layouts/LayeredLayout.java") ||
			(abPath endsWith "com/rayrobdod/tilemaps/Field Chess/tiles.scala") ||
			((abPath contains "commaSeparatedValues") && ((abPath endsWith ".java") || (abPath endsWith ".scala"))) ||
			((abPath contains "boardGame") && ((abPath endsWith ".java") || (abPath endsWith ".scala"))) ||
			((abPath contains "deductionTactics") && ((abPath endsWith ".java") || (abPath endsWith ".scala")))
		)
	}
})

excludeFilter in Compile ~= (_ || new FileFilter{
	def accept(n:File) = {
		val abPath = n.getAbsolutePath().replace('\\', '/')
		(
			(abPath contains "com/rayrobdod/animation/DieAnimation.scala") ||
			(abPath endsWith "com/rayrobdod/commaSeparatedValues/CSVTable.java") ||
			(abPath endsWith "com/rayrobdod/commaSeparatedValues/parser/ToArrayListTableCSVParseListener.java") ||
			((abPath contains "ansiEscape")) ||
			((abPath contains "junit")) ||
			((abPath contains "test"))
		)
	}
})

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-unchecked", "-deprecation" )

scalacOptions <++= scalaVersion.map{(sv:String) =>
  if (sv.take(3) == "2.1") {Seq("-feature")} else {Nil}
}



// proguard
proguardSettings

proguardType := "mini" 

ProguardKeys.options in Proguard <+= (resourceDirectory in Compile, proguardType).map{"-include '"+_+"/"+_+".proguard'"}

ProguardKeys.inputFilter in Proguard := { file =>
	file.name match {
		case "scala-library.jar" => Some("!META-INF/**,!library.properties,!scala/swing/test/**")
		case "scala-swing-2.9.3.jar" => Some("!META-INF/**,!scala/swing/test/**")
		case "scala-swing-2.10.2.jar" => Some("!META-INF/**,!scala/swing/test/**")
		case "anon-fun-reduce_2.9.3.jar" => Some("!**")
		case "anon-fun-reduce_2.9.1.jar" => Some("!**")
		case "anon-fun-reduce_2.10.jar" => Some("!**")
		case "scala-compiler.jar" => Some("!**")
		case "svgSalamander.jar" => Some("**.class")
		case _                   => None
	}
}
