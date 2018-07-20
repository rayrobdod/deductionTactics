import sbt._
import Keys._
import scala.collection.immutable.{Seq => ISeq}
import com.rayrobdod.deductionTactics.TokenClass
import com.rayrobdod.deductionTactics.serialization.CompileTokenClassesToBinary
import com.rayrobdod.deductionTactics.serialization.GenerateBasicTokens
import com.rayrobdod.deductionTactics.serialization.TokenClassParser
import java.nio.file.{Path, Files}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.json.builder.{Builder, MapBuilder, MinifiedJsonObjectBuilder, MinifiedJsonArrayBuilder}
import com.rayrobdod.json.parser.{SeqParser}

object TokenCompilePlugin extends AutoPlugin {
	object autoImport {
		val tokensCompile = taskKey[Seq[File]]("compile tokens into a binary form")
		val tokensGenerateBasic = taskKey[Seq[File]]("Generate the array of ordinary tokens")
	}
	import autoImport._

	private[this] val tokensPackage = "com/rayrobdod/deductionTactics/tokenClasses/"
	
	
	private[this] val tokensCompileSettings = Seq(
		includeFilter in tokensCompile := new FileFilter{
			def accept(n:File) = {
				n.toString.replace('\\', '/').contains(tokensPackage) && n.toString.endsWith(".json")
			}
		},
		sourceDirectory in tokensCompile in Compile := (sourceDirectory in Compile).value,
		
		sources in tokensCompile in Compile := {
			(sourceDirectory in tokensCompile in Compile).value **
					((includeFilter in tokensCompile in Compile).value --
					(excludeFilter in tokensCompile in Compile).value)
		}.get,
		sources in tokensCompile in Compile := ((tokensGenerateBasic in Compile).value ++: (sources in tokensCompile in Compile).value),
		target in tokensCompile in Compile := {
			new File((managedResourceDirectories in Compile).value.head, tokensPackage + "baseSet.rrd-dt-tokenClass")
		},
		tokensCompile in Compile := {
			val in2 = (sources in tokensCompile in Compile).value.map{_.toPath}
			val out2 = (target in tokensCompile in Compile).value.toPath
			
			java.nio.file.Files.createDirectories(out2.getParent())
			CompileTokenClassesToBinary.compile(in2.to[ISeq], out2)
			
			Seq(out2.toFile)
		},
		resourceGenerators in Compile += (tokensCompile in Compile).taskValue
	)
	private[this] val generateBasicTokens = Seq(
		(target in tokensGenerateBasic in Compile) := {
			(resourceManaged in Compile).value / tokensPackage / "basic.json"
		},
		tokensGenerateBasic in Compile := {
			val out2 = (target in tokensGenerateBasic in Compile).value.toPath
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
	
	override lazy val projectSettings = tokensCompileSettings ++ generateBasicTokens
	
	override def requires = plugins.JvmPlugin
	override def trigger = allRequirements
}
