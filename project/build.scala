import sbt._
import Keys._
import java.util.zip.{ZipInputStream, ZipOutputStream, ZipEntry}
import scala.collection.immutable.{Seq => ISeq}
import com.rayrobdod.deductionTactics.TokenClass
import com.rayrobdod.deductionTactics.serialization.CompileTokenClassesToBinary
import com.rayrobdod.deductionTactics.serialization.GenerateBasicTokens
import com.rayrobdod.deductionTactics.serialization.TokenClassParser
import net.tixxit.sbt.benchmark.BenchmarkPlugin
import java.nio.file.{Path, Files}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.json.builder.{Builder, MapBuilder, MinifiedJsonObjectBuilder, MinifiedJsonArrayBuilder}
import com.rayrobdod.json.parser.{SeqParser}
//import com.github.retronym.SbtOneJar

object DeductionTacticsBuild extends Build {
	
	val proguardType = SettingKey[String]("proguard-type", "The strength of proguard compression")
	val proguardTypeSetting = proguardType := "mini"
//	val proguardTypeSetting = proguardType := "micro"
	
	
	
	
	// configuration points, like the built in `version`, `libraryDependencies`, or `compile`
	// by implementing Plugin, these are automatically imported in a user's `build.sbt`
	val compileTokens = TaskKey[Seq[File]]("token-compile")
	val genBasicTokens = TaskKey[Seq[File]]("token-basicGen")
	
	val tokensPackage = "com/rayrobdod/deductionTactics/tokenClasses/"
	
	
	val compileTokensSettings = Seq(
		includeFilter in compileTokens := new FileFilter{
			def accept(n:File) = {
				n.toString.replace('\\', '/').contains(tokensPackage) && n.toString.endsWith(".json")
			}
		},
		sourceDirectory in compileTokens in Compile <<= (sourceDirectory in Compile),
		
		sources in compileTokens in Compile := {
			(sourceDirectory in compileTokens in Compile).value **
					((includeFilter in compileTokens in Compile).value --
					(excludeFilter in compileTokens in Compile).value)
		}.get,
		sources in compileTokens in Compile := ((genBasicTokens in Compile).value ++: (sources in compileTokens in Compile).value),
		target in compileTokens in Compile <<= (managedResourceDirectories in Compile) apply { x =>
			new File(x.head, tokensPackage + "baseSet.rrd-dt-tokenClass")
		},
		compileTokens in Compile := {
			val in2 = (sources in compileTokens in Compile).value.map{_.toPath}
			val out2 = (target in compileTokens in Compile).value.toPath
			
			java.nio.file.Files.createDirectories(out2.getParent())
			CompileTokenClassesToBinary.compile(in2.to[ISeq], out2)
			
			Seq(out2.toFile)
		},
		resourceGenerators in Compile <+= (compileTokens in Compile)
	)
	val generateBasicTokens = Seq(
		(target in genBasicTokens in Compile) := {
			(resourceManaged in Compile).value / tokensPackage / "basic.json"
		},
		genBasicTokens in Compile := {
			val out2 = (target in genBasicTokens in Compile).value.toPath
			val resDir = (resourceDirectory in Compile).value.toPath
			
			java.nio.file.Files.createDirectories(out2.getParent());
			{
				val writer = Files.newBufferedWriter(out2, UTF_8)
				val transformer:PartialFunction[Any,Any] = {case x:TokenClass => new TokenClassParser(new MapBuilder).parse(x, GenerateBasicTokens.nameToIcon(resDir)(x.name))}
				val seqParser = new SeqParser(new MinifiedJsonArrayBuilder(transformer = transformer))
				
				val json = seqParser.parse(GenerateBasicTokens.classes)
				
				writer.write(json)
				writer.close();
			}
			
			Seq(out2.toFile)
		}
	)
	
	
	
	lazy val root = Project(
			id = "deductionTactics",
			base = file("."),
			configurations = Configurations.default ++
					BenchmarkPlugin.projectConfigurations,
			settings = Defaults.coreDefaultSettings ++
					BenchmarkPlugin.projectSettings ++
					Seq(proguardTypeSetting) ++
					compileTokensSettings ++
					generateBasicTokens
	)
}
