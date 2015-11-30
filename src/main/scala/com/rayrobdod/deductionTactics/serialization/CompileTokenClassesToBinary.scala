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
package com.rayrobdod.deductionTactics.serialization

import java.nio.file.FileSystems.{getDefault => defaultFileSystem, newFileSystem}
import scala.collection.immutable.{Seq, Map}
import scala.collection.JavaConversions.{iterableAsScalaIterable, mapAsJavaMap}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.json.builder.SeqBuilder
import com.rayrobdod.deductionTactics.{TokenClass, Weaponkinds, TokenClassBuilder, TokenClassTemplate}
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.nio.file.{Path, Files}

import com.rayrobdod.deductionTactics.TokenClassFromBinary.{nameLength, imageLocLength}

/**
 * Reads a set of JSON-encoded token class files and writes out a composit, minified
 * propietary token class file
 * 
 * @author Raymond Dodge
 * @since a.5.0
 * @version a.6.0
 */
object CompileTokenClassesToBinary
{
	def compile(sources:Seq[Path], outPath:Path) = {
		
		val classes:Seq[TokenClass] = sources.map{(jsonPath:Path) =>
			var jsonReader:java.io.Reader = new java.io.StringReader("[]")
			try {
				val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
				
				new JsonParser(new SeqBuilder(new TokenClassBuilder)).parse(jsonReader).map{_.asInstanceOf[TokenClassTemplate].build}
			} finally {
				jsonReader.close()
			}
		}.flatten.flatten
		
		val imageMap = new TokenClassNameToImageLocation(sources)
		
		
		val os = Files.newOutputStream(outPath)
		val dos = new DataOutputStream(os);
		
		dos.writeShort(classes.length)
		
		classes.map{(tclass:TokenClass) =>
			val name:Array[Byte] = (tclass.name.getBytes(UTF_8) ++: Seq.fill(nameLength)(0.byteValue)).toArray
			dos.write(name, 0, nameLength);
			dos.writeByte(tclass.atkElement.id.byteValue)
			dos.writeByte(tclass.atkWeapon.id.byteValue)
			dos.writeByte(tclass.atkStatus.id.byteValue)
			dos.writeByte(tclass.body.id.byteValue)
			dos.writeByte(tclass.range.byteValue)
			dos.writeByte(tclass.speed.byteValue)
			dos.writeByte(tclass.weakStatus.id.byteValue)
			dos.writeByte(tclass.weakDirection.id.byteValue)
			Weaponkinds.values.foreach{(x:Weaponkind) =>
				dos.writeFloat(tclass.weakWeapon(x).floatValue)
			}
			val imageLoc = (imageMap.map.getOrElse(tclass.name, "").getBytes(UTF_8) ++: Seq.fill(imageLocLength)(0.byteValue)).toArray;
			dos.write(imageLoc, 0, imageLocLength);
			
			// dos.write('\n');
		}
		dos.close();
	}
}
