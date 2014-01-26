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
import com.rayrobdod.deductionTactics.{CannonicalTokenClass,
		CannonicalTokenClassDecoder, Weaponkinds}
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
	
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.{
		JSONParser, JSONDecoder, JSONParseListener}
import com.rayrobdod.javaScriptObjectNotation.JSONString
import com.rayrobdod.binaryJSON.BSONWriter
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.nio.file.{Path, Files}

import com.rayrobdod.deductionTactics.CannonicalTokenClassFromBinary.{nameLength, imageLocLength}

/**
 * Reads a set of JSON-encoded token class files and writes out a composit, minified
 * propietary token class file
 * 
 * @author Raymond Dodge
 * @since a.5.0
 */
object CompileTokenClassesToBinary // extends scala.App
{
	def compile(sources:Seq[Path], outPath:Path) = {
		
		val classes:Seq[CannonicalTokenClass] = sources.map{(jsonPath:Path) => 
			val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
			
			val l = new ToScalaCollection(CannonicalTokenClassDecoder)
			JSONParser.parse(l, jsonReader)
			jsonReader.close()
			l.resultSeq
		}.flatten
		
		val imageMap = new TokenClassNameToImageLocation(sources)
		
		
		val os = Files.newOutputStream(outPath)
		val dos = new DataOutputStream(os);
		
		dos.writeShort(classes.length)
		
		classes.map{(tclass:CannonicalTokenClass) =>
			val name:Array[Byte] = (tclass.name.getBytes(UTF_8) ++: Seq.fill(nameLength)(0.byteValue)).toArray
			dos.write(name, 0, nameLength);
			dos.writeByte(tclass.atkElement.get.id.byteValue)
			dos.writeByte(tclass.atkWeapon.get.id.byteValue)
			dos.writeByte(tclass.atkStatus.get.id.byteValue)
			dos.writeByte(tclass.body.get.id.byteValue)
			dos.writeByte(tclass.range.get.byteValue)
			dos.writeByte(tclass.speed.get.byteValue)
			dos.writeByte(tclass.weakStatus.get.id.byteValue)
			dos.writeByte(tclass.weakDirection.get.id.byteValue)
			Weaponkinds.values.foreach{(x:Weaponkind) =>
				dos.writeFloat(tclass.weakWeapon(x).get.floatValue)
			}
			val imageLoc = (imageMap.map.getOrElse(tclass.name, "").getBytes(UTF_8) ++: Seq.fill(imageLocLength)(0.byteValue)).toArray;
			dos.write(imageLoc, 0, imageLocLength);
			
			// dos.write('\n');
		}
		dos.close();
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