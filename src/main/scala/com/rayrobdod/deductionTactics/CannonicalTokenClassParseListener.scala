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
package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.BodyType
import Directions.Direction
import com.rayrobdod.deductionTactics.{TokenClass => CannonicalTokenClass}

import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import com.rayrobdod.javaScriptObjectNotation.parser.JSONDecoder
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.javaScriptObjectNotation.JSONString
import java.io.{StringReader, InputStreamReader}
import LoggerInitializer.{tokenClassDecoderLogger => Logger}



/**
 * A builder for CannonicalTokenClasses.
 * @since a.4.1
 * @version a.5.0
 */
final class CannonicalTokenClassBuilder extends TokenClass {
	var nameOpt:Option[String] = None
	def name = nameOpt.getOrElse("???")
	
	var body:Option[BodyType] = None
	var atkElement:Option[Element] = None
	var atkWeapon:Option[Weaponkind] = None
	var atkStatus:Option[Status] = None
	var range:Option[Int] = None
	var speed:Option[Int] = None
	
	var weakDirection:Option[Direction] = None
	var weakWeapon:Map[Weaponkind,Option[Float]] = Weaponkinds.values.zipAll(Nil, null, None).toMap
	var weakStatus:Option[Status] = None
	
	def clear() = {
		nameOpt = None;
		body = None;
		atkElement = None
		atkWeapon = None
		atkStatus = None
		range = None
		speed = None
		weakDirection = None
		weakWeapon = Weaponkinds.values.zipAll(Nil, null, None).toMap
		weakStatus = None
	}
	
	private def arbitraryDirection:Direction = {
		val nameHash = this.name.hashCode
		Directions.values((nameHash % Directions.values.length + Directions.values.length) % Directions.values.length)
	}
	
	/**
	 * @throws IllegalStateException if required fields were not set
	 */
	def build():CannonicalTokenClass = {
		try {
			// B*CKING DELAYED EXECUTION
			weakWeapon.values.foreach{_.get}
			
			new TokenClassBlunt (
				CannonicalTokenClassBuilder.this.name,
				CannonicalTokenClassBuilder.this.body.get,
				CannonicalTokenClassBuilder.this.atkElement.get,
				CannonicalTokenClassBuilder.this.atkWeapon.get,
				CannonicalTokenClassBuilder.this.atkStatus.get,
				CannonicalTokenClassBuilder.this.range.get,
				CannonicalTokenClassBuilder.this.speed.get,
				CannonicalTokenClassBuilder.this.weakDirection.getOrElse(arbitraryDirection),
				CannonicalTokenClassBuilder.this.weakWeapon.map{(a) => ((a._1, a._2.get))},
				CannonicalTokenClassBuilder.this.weakStatus.get
			)
		} catch {
			// TODO: be more specific?
			case e:java.util.NoSuchElementException => throw new IllegalStateException(
					"Not all information gotten about subject: " + this.toString, e)
					
		}
	}
}


/**
 * Decodes a JSON-encoded string directly into a CannonicalTokenClass, using CannonicalTokenClassParseListener
 * @version 2013 Jun 23
 */
object CannonicalTokenClassDecoder extends JSONDecoder[CannonicalTokenClass] {
	override def decode(s:String):CannonicalTokenClass = {
		val l = new CannonicalTokenClassParseListener
		Logger.finer(s)
		JSONParser.parse(l, s)
		l.result
	}
}

/**
 * When parsing a token, parses it into a CannonicalTokenClass
 * @version a.5.0
 */
final class CannonicalTokenClassParseListener extends JSONParseListener {
	private val builder = new CannonicalTokenClassBuilder
	private var strBuilder = StringBuilder.newBuilder
	private var key:Option[String] = None
	
	override def abort = false
	override def charRead(index:Int, charact:Char) = strBuilder += charact
	override def started() {builder.clear; strBuilder = StringBuilder.newBuilder}
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
					case "body" => builder.body = Some(BodyTypes.withName(value))
					case "element" => builder.atkElement = Some(Elements.withName(value))
					case "atkWeapon" => builder.atkWeapon = Some(Weaponkinds.withName(value))
					case "atkStatus" => builder.atkStatus = Some(Statuses.withName(value))
					case "range" => builder.range = Some(valueRaw.toInt)
					case "speed" => builder.speed = Some(valueRaw.toInt)
					case "weakStatus" => builder.weakStatus = Some(Statuses.withName(value))
					case "weakDirection" => {
						if (value != "DontCare") {
							builder.weakDirection = Some(Directions.withName(value))
						} else {
							builder.weakDirection = None
						}
					}
					case "weakWeapon" => builder.weakWeapon = {
						object FloatDecoder extends JSONDecoder[Some[Float]] {
							override def decode(s:String):Some[Float] = {
								try {
									Some(s.toFloat)
								} catch {
									case e:NumberFormatException => throw new ClassCastException(
											"Value is not a float: " + s)
								}
							}
						}
						
						val l = new ToScalaCollection(FloatDecoder)
						JSONParser.parse(l, value)
						l.resultMap.map({(x:String,y:Some[Float]) => (( Weaponkinds.withName(x), y))}.tupled)
					}
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

	def result = builder.build()
}
