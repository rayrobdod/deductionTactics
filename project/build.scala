import sbt._
import Keys._
import java.util.zip.{ZipInputStream, ZipOutputStream, ZipEntry}
import com.rayrobdod.deductionTactics.meta.CompileTokenClassesToBinary

object DeductionTacticsBuild extends Build {
	
	val websitePath = SettingKey[File]("website-path", "A staging area for setting up website publish things")
	val websitePathSetting = websitePath <<= crossTarget(new File(_, "website"))

	val proguardType = SettingKey[String]("proguard-type", "The strength of proguard compression")
	val proguardTypeSetting = proguardType := "mini"
//	val proguardTypeSetting = proguardType := "micro"

	
	
	
	val transformToGZip = {(inFile:File) =>
		val identityFile = new File(inFile.toString + ".identity")
		val gzipFile = new File(identityFile.toString + ".gz")
		//
		def entryWithSameName(x:ZipEntry) = {
			if (x == null) null
			else new ZipEntry(x.getName)
		}
		//
		//if (inFile.lastModified() > identityFile.lastModified()) {
			val inStream = new ZipInputStream(new java.io.FileInputStream(inFile)) 
			val identityStream = new ZipOutputStream(new java.io.FileOutputStream(identityFile))
			identityStream.setLevel(0)
		//	identityStream.setMethod(ZipOutputStream.STORED)
	
			var nextEntry = entryWithSameName(inStream.getNextEntry())
			while (null != nextEntry) {
				identityStream.putNextEntry(nextEntry)
				while (inStream.available() != 0) {
					identityStream.write(inStream.read())
				}
				nextEntry = entryWithSameName(inStream.getNextEntry())
			}
			inStream.close()
			identityStream.close()
		//}
		if (identityFile.lastModified() > gzipFile.lastModified()) {
			IO.gzip(identityFile, gzipFile)
		}
		Seq(identityFile, gzipFile)
	}
	
	val transformToPack200 = {(inFile:File) =>
		import java.util.jar.Pack200.Packer._
		//
		val inJar = new java.util.jar.JarFile(inFile)
		val packer = java.util.jar.Pack200.newPacker()
		val outFile = new File(inFile.toString + ".pack")
		val outGzipFile = new File(outFile.toString + ".gz")
		val repackedFile = new File(inFile.toString + ".repack")
		val shouldStripStr = if (true) {STRIP} else {PASS}
		//
		packer.properties.put(UNKNOWN_ATTRIBUTE, shouldStripStr);
		packer.properties.put(EFFORT, "9");
		packer.properties.put(MODIFICATION_TIME, LATEST);
		packer.properties.put(CODE_ATTRIBUTE_PFX+"LineNumberTable",    shouldStripStr);
		packer.properties.put(CODE_ATTRIBUTE_PFX+"LocalVariableTable", shouldStripStr);
		packer.properties.put(CLASS_ATTRIBUTE_PFX+"SourceFile",        shouldStripStr);
		packer.properties.put(CLASS_ATTRIBUTE_PFX+"ScalaSig",          "BBB");
		//
		// only bother doing the action if stuff changed recently
		if (inFile.lastModified() > outFile.lastModified()) {
			val outStream = new java.io.FileOutputStream(outFile)
			packer.pack(inJar, outStream)
			outStream.close()
		}
		if (outFile.lastModified() > outGzipFile.lastModified()) {
			IO.gzip(outFile, outGzipFile)
		}
		if (outFile.lastModified() > repackedFile.lastModified()) {
			val repackedStream = new java.util.jar.JarOutputStream(new java.io.FileOutputStream(repackedFile))
			java.util.jar.Pack200.newUnpacker().unpack(outFile, repackedStream)
			repackedStream.close()
		}
		Seq(outFile, outGzipFile, repackedFile)
	}
	
	def varFileContents(name:String) = {
"""URI: """+name+""".jar

URI: """+name+""".jar.identity
Content-type: application/java-archive

URI: """+name+""".jar.gzip
Content-type: application/java-archive
Content-Encoding: gzip; q=0.6

URI: """+name+""".jar.pack.gzip
Content-type: application/java-archive
Content-Encoding: pack200-gzip; q=1"""
	}
	
	
	
	
	import com.typesafe.sbt.SbtProguard.ProguardKeys
	import com.typesafe.sbt.SbtProguard.Proguard
	
	val packageBinPack = TaskKey[Seq[File]]("package-bin-pack",
			"perform a pack200 compression on the packaged file")
	val packageBinPackTask = packageBinPack <<=
			(packageBin in Compile).map(transformToPack200)
	val packageBinGzip = TaskKey[Seq[File]]("package-bin-gz",
			"perform a gzip compression on the packaged file")
	val packageBinGzipTask = packageBinGzip <<=
			(packageBinPack).map(_.last).map(transformToGZip)
	val proguardPack = TaskKey[Seq[File]]("proguard-pack",
			"perform a pack200 compression on the proguarded file")
	val proguardPackTask = proguardPack <<=
			(ProguardKeys.proguard in Proguard).map(_.head).map(transformToPack200)
	val proguardGzip = TaskKey[Seq[File]]("proguard-gz",
			"perform a gzip compression on the packaged file")
	val proguardGzipTask = proguardGzip <<=
			(proguardPack).map(_.last).map(transformToGZip)
	val sourcesGzip = TaskKey[Seq[File]]("package-src-gz",
			"perform a gzip compression on the packaged sources")
	val sourcesGzipTask = sourcesGzip <<=
			(packageSrc in Compile).map(transformToGZip)

	
	val prepForWebsite = TaskKey[Seq[File]]("prep-for-website")
	val prepForWebsiteTask = prepForWebsite <<= 
			(version, websitePath, proguardType, packageSrc in Compile,
			packageBinPack, packageBinGzip, proguardPack, proguardGzip, sourcesGzip).map{
				(version:String, base:File, proguardType:String, srcFrom:File, binPack:Seq[File],
				binGzip:Seq[File], proPack:Seq[File], proGzip:Seq[File], srcFromGzip:Seq[File]) =>
		
		val srcJar = new File(base, "deductionTactics-"+version+"-orig-src.jar.identity")
		IO.copyFile(srcFrom, srcJar)
		val srcGzip = new File(base, "deductionTactics-"+version+"-orig-src.jar.gzip")
		IO.copyFile(srcFromGzip(1), srcGzip)
		val origFullJar = new File(base, "deductionTactics-"+version+"-orig-full.jar.identity")
		IO.copyFile(binPack(2), origFullJar)
		val origFullGzip = new File(base, "deductionTactics-"+version+"-orig-full.jar.gzip")
		IO.copyFile(binGzip(1), origFullGzip)
		val origFullPack = new File(base, "deductionTactics-"+version+"-orig-full.jar.pack.gzip")
		IO.copyFile(binPack(1), origFullPack)
		val fullMiniJar = new File(base, "deductionTactics-"+version+"-full-"+proguardType+".jar.identity")
		IO.copyFile(proPack(2), fullMiniJar)
		val fullMiniGzip = new File(base, "deductionTactics-"+version+"-full-"+proguardType+".jar.gzip")
		IO.copyFile(proGzip(1), fullMiniGzip)
		val fullMiniPack = new File(base, "deductionTactics-"+version+"-full-"+proguardType+".jar.pack.gzip")
		IO.copyFile(proPack(1), fullMiniPack)
		
		val origFullVar = new File(base, "deductionTactics-"+version+"-orig-full.jar.var")
		val origFullVarWriter = new java.io.FileWriter(origFullVar)
		origFullVarWriter.write(varFileContents("orig-full"))
		origFullVarWriter.close()
		
		val fullMiniVar = new File(base, "deductionTactics-"+version+"-full-"+proguardType+".jar.var")
		val fullMiniVarWriter = new java.io.FileWriter(fullMiniVar)
		fullMiniVarWriter.write(varFileContents("full-"+proguardType))
		fullMiniVarWriter.close()
		
		
		Seq(srcJar, srcGzip, origFullJar, origFullGzip, origFullPack, fullMiniJar, fullMiniGzip, fullMiniPack)
	}
	
	
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
			settings = Defaults.defaultSettings ++ Seq(websitePathSetting, 
					proguardTypeSetting, packageBinPackTask, packageBinGzipTask,
					proguardPackTask, proguardGzipTask, prepForWebsiteTask,
					sourcesGzipTask) ++ compileTokensSettings
	)
	lazy val meta = Project(
			id = "deductionTactics-meta",
			base = file("src/meta"),
			settings = Defaults.defaultSettings
	) dependsOn(root)
}