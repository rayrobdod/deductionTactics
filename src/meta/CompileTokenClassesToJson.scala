package com.rayrobdod.deductionTactics.meta

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
		
		classes.map{(tclass:CannonicalTokenClass) =>
			writer.write("{\"name\":\"");
			writer.write(tclass.name);
			
			writer.write("\",\"element\":\"");
			writer.write(tclass.atkElement.get.name);
			
			writer.write("\",\"atkWeapon\":\"");
			writer.write(tclass.atkWeapon.get.name.dropRight(4));
			
			writer.write("\",\"atkStatus\":\"");
			writer.write(tclass.atkStatus.get.name)
			
			writer.write("\",\"body\":\"");
			// writer.write(tclass.body.get.name)
			writer.write("Human");
			
			writer.write("\",\"range\":");
			writer.write(tclass.range.get.toString)
			
			writer.write(",\"speed\":");
			writer.write(tclass.speed.get.toString)
			
			writer.write(",\"weakStatus\":\"");
			writer.write(tclass.weakStatus.get.name)
			
			writer.write("\",\"weakDirection\":\"");
			writer.write(tclass.weakDirection.get.name)
			
			writer.write("\",\"weakWeapon\":{");
			val weakWeapon = tclass.weakWeapon.foldLeft(new java.lang.StringBuilder){(a,b) => 
				a.append(",\"")
				a.append(b._1.name.dropRight(4))
				a.append("\":")
				a.append(b._2.get)
			}.toString.tail
			writer.write(weakWeapon)
			
			writer.write("},\n");
		}
		
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
	
