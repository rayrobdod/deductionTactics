package com.rayrobdod.deductionTactics.meta

import com.rayrobdod.deductionTactics.tokenClassToJSON

import java.nio.file.FileSystems.{getDefault => defaultFileSystem, newFileSystem}
import scala.collection.JavaConversions.{iterableAsScalaIterable, mapAsJavaMap}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.deductionTactics.CannonicalTokenClass
import com.rayrobdod.deductionTactics.CannonicalTokenClassDecoder
	
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.binaryJSON.BSONWriter
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.nio.file.{Path, Files}

/**
 * Reads a set of JSON-encoded token class files and writes out a composit, minified
 * JSON-encoded token class file
 * 
 * @author Raymond Dodge
 * @version 2013 Aug 05
 */
object CompileTokenClassesToJson // extends scala.App
{
	def compile(sources:Seq[Path], outPath:Path) = {
		
		val classes:Seq[CannonicalTokenClass] = sources.map{(jsonPath:Path) => 
			val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
			
			val l = new ToScalaCollection(CannonicalTokenClassDecoder)
			JSONParser.parse(l, jsonReader)
			jsonReader.close()
			l.resultSeq
		}.flatten
		
		
		val writer = Files.newBufferedWriter(outPath, UTF_8);
		writer.write('[')
		
		classes.zipWithIndex.foreach({(tclass:TokenClass, index:Int) =>
			if (index != 0) writer.write(',')
			writer.write( tokenClassToJSON(tclass) )
		}.tupled)
		
		writer.write(']');
		writer.close();
	}
	
	def main(args:Array[String]) {
		val (sources, outDir) = {
			var sources:Seq[Path] = Nil
			var outDir:Option[Path] = None
		
			var i = 0
			while (i < args.length) {
				args(i) match {
					case "-d" => {
						outDir = Some(defaultFileSystem getPath args(i+1))
						i = i + 1;
					}
					case _ => {
						sources = sources :+ (defaultFileSystem getPath args(i))
					}
				}
				i = i + 1;
			}
			
			(sources, outDir.get)
		}
		
		this.compile(sources, outDir)
	}
}
	
