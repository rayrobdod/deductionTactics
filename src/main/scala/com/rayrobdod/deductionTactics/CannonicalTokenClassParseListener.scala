package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction

import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import com.rayrobdod.javaScriptObjectNotation.parser.JSONDecoder
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.javaScriptObjectNotation.JSONString
import java.io.{StringReader, InputStreamReader}
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsJavaMap
import LoggerInitializer.{tokenClassDecoderLogger => Logger}



/**
 * A builder for CannonicalTokenClasses.
 * @version 2013 Jun 23
 * @version 2013 Aug 06 - removing icon
 */
class CannonicalTokenClassBuilder extends TokenClass {
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
			
			new CannonicalTokenClassBlunt (
				CannonicalTokenClassBuilder.this.name,
				Some(CannonicalTokenClassBuilder.this.body.get),
				Some(CannonicalTokenClassBuilder.this.atkElement.get),
				Some(CannonicalTokenClassBuilder.this.atkWeapon.get),
				Some(CannonicalTokenClassBuilder.this.atkStatus.get),
				Some(CannonicalTokenClassBuilder.this.range.get),
				Some(CannonicalTokenClassBuilder.this.speed.get),
				Some(CannonicalTokenClassBuilder.this.weakDirection.getOrElse(arbitraryDirection)),
				CannonicalTokenClassBuilder.this.weakWeapon.mapValues{x => Some(x.get)},
				Some(CannonicalTokenClassBuilder.this.weakStatus.get)
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
 * @version 2013 Jun 23
 * @version 2013 Jun 24 - internal FloatDecoder directly makes Some(Float)s now
 * @version 2013 Aug 06 - removing icon
 */
class CannonicalTokenClassParseListener extends JSONParseListener {
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
