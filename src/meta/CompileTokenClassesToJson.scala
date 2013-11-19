/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
 * @since a.5.0
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
		
		classes.zipWithIndex.foreach({(tclass:CannonicalTokenClass, index:Int) =>
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
	
