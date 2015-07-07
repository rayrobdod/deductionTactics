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
import com.rayrobdod.deductionTactics.{TokenClass,
		Weaponkinds}
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.json.builder.{Builder, SeqBuilder, MapBuilder}
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.nio.file.{Path, Files}

/**
 * Reads a set of JSON-encoded token class files and creates a map from
 * class names to image resources
 * 
 * @author Raymond Dodge
 * @since a.5.0
 * @version a.6.0
 */
class TokenClassNameToImageLocation(sources:Seq[Path]) {
	
	
	val map:Map[String, String] = {
		val a:Seq[Seq[(String, Option[String])]] = sources.map{(jsonPath:Path) =>
			var jsonReader:java.io.Reader = new java.io.StringReader("[]")
			try {
				val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
				
				new JsonParser(new SeqBuilder(new NameToIconBuilder)).parse(jsonReader).map{_.asInstanceOf[Tuple2[String, Option[String]]]}
			} finally {
				jsonReader.close()
			}
		}
		val b:Seq[(String, Option[String])] = a.flatten
		val c:Seq[(String, String)] = b.filter{_._2.isDefined}.map{(a) => (( a._1, a._2.get ))}
		val d:Map[String, String] = c.toMap
		d
	}
	
	
	
	
	final class NameToIconBuilder extends Builder[(String, Option[String])] {
		override val init:(String, Option[String]) = ("", None)
		override def apply(folding:Tuple2[String, Option[String]], key:String, value:Any):Tuple2[String, Option[String]] = key match {
			case "name" => folding.copy(_1 = value.toString)
			case "icon" => folding.copy(_2 = Option(value.toString))
			case _ => folding
		}
		override def childBuilder(key:String):Builder[_] = new MapBuilder
		override val resultType:Class[Tuple2[String, Option[String]]] = classOf[Tuple2[String, Option[String]]]
	}
}
