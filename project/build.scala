import sbt._
import Keys._
import java.util.zip.{ZipInputStream, ZipOutputStream, ZipEntry}
import com.rayrobdod.deductionTactics.meta.CompileTokenClassesToBinary
import com.github.retronym.SbtOneJar

object DeductionTacticsBuild extends Build {
	
	val proguardType = SettingKey[String]("proguard-type", "The strength of proguard compression")
	val proguardTypeSetting = proguardType := "mini"
//	val proguardTypeSetting = proguardType := "micro"
	
	
	
	
	// configuration points, like the built in `version`, `libraryDependencies`, or `compile`
	// by implementing Plugin, these are automatically imported in a user's `build.sbt`
	val compileTokens = TaskKey[Seq[File]]("token-compile")
	val compileTokensInput = SettingKey[Seq[File]]("token-in")
	val compileTokensOutput = SettingKey[File]("token-outdir")
	
	val tokensPackage = "com/rayrobdod/deductionTactics/tokenClasses/"
	
	// a group of settings ready to be added to a Project
	// to automatically add them, do
	val compileTokensSettings = Seq(
		compileTokensInput <<= (resourceDirectory in Compile) apply { x =>
			new File(x, tokensPackage).listFiles(new FileFilter{
				def accept(x:File) = x.toString.endsWith(".json")
			})
		},
		compileTokensOutput <<= (managedResourceDirectories in Compile) apply { x =>
			new File(x.head, tokensPackage + "baseSet.rrd-dt-tokenClass")
		},
		compileTokens <<= (compileTokensInput, compileTokensOutput) map { (in, out) =>
			val in2 = in.map{_.toPath}
			val out2 = out.toPath
			
			java.nio.file.Files.createDirectories(out2.getParent())
			CompileTokenClassesToBinary.compile(in2, out2)
			
			Seq(out2.toFile)
		}
	)
	
	
	
	lazy val root = Project(
			id = "deductionTactics",
			base = file("."),
			settings = Defaults.defaultSettings ++
					Seq(proguardTypeSetting) ++
					compileTokensSettings ++
					SbtOneJar.oneJarSettings
	)
	lazy val meta = Project(
			id = "deductionTactics-meta",
			base = file("src/meta"),
			settings = Defaults.defaultSettings
	) dependsOn(root)
}