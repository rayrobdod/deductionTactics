name := "Deduction Tactics Meta"

organization := "com.rayrobdod"

sbtPlugin := true

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "a.5.0-SNAPSHOT"

target := new File("C:/Users/Raymond/AppData/Local/Temp/build/DeductionTacticsMeta/")

libraryDependencies += ("com.rayrobdod" %% "json" % "1.0.0-SNAPSHOT")

libraryDependencies += ("com.rayrobdod" %% "csv" % "1.0.0-SNAPSHOT")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "1.0.0-SNAPSHOT")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "1.0.0-SNAPSHOT")

scalaSource := new File("C:/Users/Raymond/Documents/Programming/Java/Games/DeductionTactics/src/meta")



javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-unchecked", "-deprecation" )

scalacOptions <++= scalaVersion.map{(sv:String) =>
  if (sv.take(3) == "2.1") {Seq("-feature")} else {Nil}
}

