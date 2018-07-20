/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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
package com.rayrobdod.deductionTactics.swingView

import javax.swing.Icon
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.deductionTactics.{Weaponkinds, Elements}

import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.json.builder.{Builder, SeqBuilder, MapBuilder}
import java.net.URL
import scala.collection.immutable.{Seq, Map}

/**
 * Reads a set of JSON-encoded token class files and creates a map from
 * class names to `javax.swing.Icon`s
 * 
 * @author Raymond Dodge
 * @since a.6.0
 */
class TokenClassNameToIconFromJson(sources:Seq[URL]) {
	import TokenClassNameToIconFromJson._
	
	val map:Map[String, Icon] = {
		val a:Seq[Seq[(String, Icon)]] = sources.map{(jsonPath:URL) =>
			val jsonReader = new java.io.InputStreamReader(jsonPath.openStream(), UTF_8)
			
			try {
				new JsonParser(new SeqBuilder(new TokenIconBuilder)).parse(jsonReader).map{_.asInstanceOf[TokenIconParts].result}
			} finally {
				jsonReader.close()
			}
		}
		val b:Seq[(String, Icon)] = a.flatten
		val d:Map[String, Icon] = b.toMap
		d
	}
}

object TokenClassNameToIconFromJson {
	
	final class TokenIconBuilder extends Builder[TokenIconParts] {
		override val init:TokenIconParts = new TokenIconParts()
		override def apply(folding:TokenIconParts, key:String, value:Any):TokenIconParts = key match {
			case "name" => folding.copy(name = value.toString)
			case "icon" => folding.copy(iconLoc = Some(value.toString))
			case "element" => folding.copy(atkElement = Some(Elements.withName(value.toString)))
			case "atkWeapon" => folding.copy(atkWeapon = Some(Weaponkinds.withName(value.toString)))
			case _ => folding
		}
		override def childBuilder(key:String):Builder[_] = new MapBuilder
		override val resultType:Class[TokenIconParts] = classOf[TokenIconParts]
	}
	
	final case class TokenIconParts(
		val name:String = "",
		val iconLoc:Option[String] = None,
		val atkElement:Option[Elements.Element] = None,
		val atkWeapon:Option[Weaponkinds.Weaponkind] = None
	) {
		def result:(String, Icon) = {
			val name = this.name;
			val icon = if (iconLoc.isDefined) {
				loadIcon(iconLoc.get, 32)
			} else {
				generateGenericIcon(atkElement, atkWeapon)
			}
			
			(name, icon)
		}
	}
}


/**
 * Reads a set of propietary token class files and creates a map from
 * class names to `javax.swing.Icon`s
 * 
 * @author Raymond Dodge
 * @since a.6.0
 */
class TokenClassNameToIconFromBinary(sources:Seq[URL]) {
	import com.rayrobdod.deductionTactics.TokenClassFromBinary.{nameLength, enumsLength, imageLocLength}

	
	val map:Map[String, Icon] = {
		val a:Seq[Seq[(String, Icon)]] = sources.map{(jsonPath:URL) =>
			val is = jsonPath.openStream()
			val dis = new java.io.DataInputStream(is)
			val count = dis.readShort()
			
			val values = (1 to count).map{ (a) =>
				val nameBytes = new Array[Byte](nameLength)
				is.read(nameBytes)
				val name = new String(nameBytes.takeWhile{_ != 0})
				
				val atkElement = Some(Elements(is.read()))
				val atkWeapon = Some(Weaponkinds(is.read()))
				is.skip(enumsLength - 2)
				
				val imageLocBytes = new Array[Byte](imageLocLength)
				is.read(imageLocBytes)
				val imageLoc = new String(imageLocBytes.takeWhile{_ != 0})
				
				val icon = if (! imageLoc.isEmpty) {
					loadIcon(imageLoc, 32)
				} else {
					generateGenericIcon(atkElement, atkWeapon)
				}
				
				((name, icon))
			}
			dis.close();
			
			values
		}
		val b:Seq[(String, Icon)] = a.flatten
		val d:Map[String, Icon] = b.toMap
		d
	}
}
