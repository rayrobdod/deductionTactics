name := "Deduction Tactics Meta"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "a.5.2"

scalaVersion := "2.9.3"

crossScalaVersions ++= Seq("2.9.1", "2.9.2", "2.9.3", "2.10.2", "2.11.0-M4")

target <<= (target) apply { x => new File(x, "../../../target/meta") }

libraryDependencies += ("com.rayrobdod" %% "json" % "1.0.0")

libraryDependencies += ("com.rayrobdod" %% "csv" % "1.0.0")

libraryDependencies += ("com.rayrobdod" %% "utilities" % "20130908")

libraryDependencies += ("com.rayrobdod" %% "board-game-generic" % "2.0.0-SNAPSHOT")

scalaSource := new File("C:/Users/Raymond/Documents/Programming/Java/Games/DeductionTactics/src/meta")

licenses += (("GPLv3 or later", new java.net.URL("http://www.gnu.org/licenses/") ))



javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-unchecked", "-deprecation" )

scalacOptions <++= scalaVersion.map{(sv:String) =>
  if (sv.take(3) == "2.1") {Seq("-feature")} else {Nil}
}

