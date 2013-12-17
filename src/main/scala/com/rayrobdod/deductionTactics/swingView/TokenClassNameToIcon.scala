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
package com.rayrobdod.deductionTactics.swingView

import javax.swing.{Icon, ImageIcon}
import scala.collection.JavaConversions.{iterableAsScalaIterable, mapAsJavaMap}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.deductionTactics.{CannonicalTokenClass,
		CannonicalTokenClassDecoder, Weaponkinds, CannonicalTokenClassBuilder, Elements}
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
	
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.{
		JSONParser, JSONDecoder, JSONParseListener}
import com.rayrobdod.javaScriptObjectNotation.JSONString
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.net.URL
import javax.imageio.ImageIO

/**
 * Reads a set of JSON-encoded token class files and creates a map from
 * class names to `javax.swing.Icon`s
 * 
 * @author Raymond Dodge
 * @since a.5.3
 */
class TokenClassNameToIconFromJson(sources:Seq[URL]) {
	
	val map:Map[String, Icon] = {
		val a:Seq[Seq[(String, Icon)]] = sources.map{(jsonPath:URL) =>
			val jsonReader = new java.io.InputStreamReader(jsonPath.openStream(), UTF_8)
			
			val l = new ToScalaCollection(this.Decoder)
			JSONParser.parse(l, jsonReader)
			jsonReader.close()
			l.resultSeq
		}
		val b:Seq[(String, Icon)] = a.flatten
		val d:Map[String, Icon] = b.toMap
		d
	}
	
	private object Decoder extends JSONDecoder[(String, Icon)] {
		override def decode(s:String):(String, Icon) = {
			val l = new TokenClassNameToIconFromJson.this.ParseListener
			JSONParser.parse(l, s)
			l.result
		}
	}
	
	private class ParseListener extends JSONParseListener {
		private val builder = new CannonicalTokenClassBuilder
		private var strBuilder = StringBuilder.newBuilder
		private var key:Option[String] = None
		private var iconLoc:Option[String] = None
		
		override def abort = false
		override def charRead(index:Int, charact:Char) = strBuilder += charact
		override def started() {builder.clear; iconLoc = None; key = None; strBuilder = StringBuilder.newBuilder}
		override def ended() {}
		override def elemStarted(index:Int, charact:Char) {}
		override def openingBracket(index:Int, charact:Char) {}
		override def endingBracket(index:Int, charact:Char) {}
		
		override def elemEnded(index:Int, charact:Char) =
		{
			val valueRaw = strBuilder.toString
			val value = try {
				JSONString.generateUnparsed(valueRaw).toString
			} catch {
				case x:java.text.ParseException => valueRaw
			}
			
			key.foreach{ (x:String) =>
				try {
					x match {
						case "name" => builder.nameOpt = Some(value)
						case "icon" => iconLoc = Some(value)
						case "element" => builder.atkElement = Some(Elements.withName(value))
						case "atkWeapon" => builder.atkWeapon = Some(Weaponkinds.withName(value))
						case x => {  }
					}
				} catch {
					case e:java.util.NoSuchElementException => {throw e}
				}
			}
			strBuilder = StringBuilder.newBuilder
		}
		
		override def keyValueSeparation(index:Int, charact:Char) =
		{
				key = Some(JSONString.generateUnparsed(strBuilder.toString).toString)
				strBuilder = StringBuilder.newBuilder;
		}
		
		def result:(String, Icon) = {
			val name = builder.name;
			val icon = if (iconLoc.isDefined) {
				loadIcon(iconLoc.get, 32)
			} else {
				generateGenericIcon(builder)
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
 * @since a.5.3
 */
class TokenClassNameToIconFromBinary(sources:Seq[URL]) {
	import com.rayrobdod.deductionTactics.CannonicalTokenClassFromBinary.{nameLength, enumsLength, imageLocLength}

	
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
					generateGenericIcon({
						val retVal = new com.rayrobdod.deductionTactics.SuspicionsTokenClass;
						retVal.atkElement = atkElement
						retVal.atkWeapon = atkWeapon
						retVal
					})
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
