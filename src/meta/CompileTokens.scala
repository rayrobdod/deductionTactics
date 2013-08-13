import sbt._
import Keys.{resourceManaged, resourceDirectory}
import com.rayrobdod.deductionTactics.meta.CompileTokenClassesToBinary

object CompileTokensPlugin extends Plugin
{
    // configuration points, like the built in `version`, `libraryDependencies`, or `compile`
    // by implementing Plugin, these are automatically imported in a user's `build.sbt`
    val compileTokens = TaskKey[Unit]("token-compile")
    val compileTokensInput = SettingKey[Seq[File]]("token-in")
    val compileTokensOutput = SettingKey[File]("token-outdir")
    
    val tokensPackage = "com/rayrobdod/deductionTactics/tokenClasses/"

    // a group of settings ready to be added to a Project
    // to automatically add them, do
    val compileTokensSettings = Seq(
        compileTokensInput <<= resourceDirectory apply { x =>
        	new File(x, tokensPackage).listFiles(new FileFilter{
        		def accept(x:File) = x.toString.endsWith(".json")
        	})
        },
        compileTokensOutput <<= resourceManaged apply { x =>
        	new File(x, tokensPackage + "baseSet.rrd-dt-tokenClass")
        },
        compileTokens <<= (compileTokensInput, compileTokensOutput) map { (in, out) =>
        	val in2 = in.map{_.toPath}
        	val out2 = out.toPath
        	
        	CompileTokenClassesToBinary.compile(in2, out2)
        }
    )
}

