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

import java.nio.file.FileSystems.{getDefault => defaultFileSystem, newFileSystem}
import scala.collection.JavaConversions.{iterableAsScalaIterable, mapAsJavaMap}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.deductionTactics.TokenClass
import com.rayrobdod.deductionTactics.CannonicalTokenClassDecoder
	
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.binaryJSON.BSONWriter
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.nio.file.{Path, Files}

/**
 * Reads a set of JSON-encoded token class files and writes out a composit, minified
 * BSON-encoded token class file
 * 
 * @author Raymond Dodge
 * @since a.5.0
 */
object CompileTokenClassesToBson // extends scala.App
{
	def compile(sources:Seq[Path], outPath:Path) = {
		
		val classes:Seq[TokenClass] = sources.map{(jsonPath:Path) => 
			val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
			
			val l = new ToScalaCollection(CannonicalTokenClassDecoder)
			JSONParser.parse(l, jsonReader)
			jsonReader.close()
			l.resultSeq
		}.flatten
		
		
		val os = new ByteArrayOutputStream();
		val dos = new DataOutputStream(os);
		val os2 = new ByteArrayOutputStream();
		val dos2 = new DataOutputStream(os2);
		val os3 = Files.newOutputStream(outPath)
		val dos3 = new DataOutputStream(os3);
		
		classes.map{(tclass:TokenClass) =>
			dos.write(Array(0x02, 'n','a','m','e', 0).map{_.byteValue})
			BSONWriter.writeValue(0x02, dos, tclass.name)
			
			dos.write(Array(0x10, 'e','l','e','m','e','n','t', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.atkElement.id)
			
			dos.write(Array(0x10, 'a','t','k','W','e','a','p','o','n', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.atkWeapon.id)
			
			dos.write(Array(0x10, 'a','t','k','S','t','a','t','u','s', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.atkStatus.id)
			
			dos.write(Array(0x10, 'b','o','d','y', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.body.id)
			
			dos.write(Array(0x10, 'r','a','n','g','e', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.range)
			
			dos.write(Array(0x10, 's','p','e','e','d', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.speed)
			
			dos.write(Array(0x10, 'w','e','a','k','S','t','a','t','u','s', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.weakStatus.id)
			
			dos.write(Array(0x10, 'w','e','a','k','D','i','r','e','c','t','i','o','n', 0).map{_.byteValue})
			BSONWriter.writeValue(0x10, dos, tclass.weakDirection.id)
			
			dos.write(Array(0x03, 'w','e','a','k','W','e','a','p','o','n', 0).map{_.byteValue})
			BSONWriter.writeValue(0x03, dos, mapAsJavaMap(tclass.weakWeapon.map{a => ((a._1.id.toString, a._2))}))
			
			dos.write(0);
			dos.flush();
			val bytes = os.toByteArray
			
			dos2.writeInt(java.lang.Integer.reverseBytes(bytes.length + 4));
			dos2.write(bytes)
			os.reset();
		}
		
		dos2.write(0);
		dos2.flush();
		val bytes = os2.toByteArray
		dos3.writeInt(java.lang.Integer.reverseBytes(bytes.length + 4));
		dos3.write(bytes);
		
		dos.close();
		dos2.close();
		dos3.close();
		
		
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
	
