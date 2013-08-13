package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction

import javax.swing.Icon
import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import java.io.{StringReader, InputStreamReader}
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.enumerationAsScalaIterator
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, FileSystems}
import com.rayrobdod.javaScriptObjectNotation.JSONString
import com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONObject
import scala.collection.JavaConversions.mapAsJavaMap

/**
 * A Token Class that gets its values from a Map.
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 06 Oct 2011 - implemented icon
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 19 Jan 2012 - renamed from TokenClassFromMap to CannonicalTokenClassFromMap,
 			and now extends CannonicalTokenClass instead of TokenClass
 * @version 29 Feb 2012 - icon uses #generateGenericIcon if there is no token specified
 * @version 24 Mar 2012 - made the generic toWhatever functions private
 * @version 05 Jun 2012 - changing weakWeapon from Option[Map[Weaponkind, Float]]
			to Map[Weaponkind, Option[Float]]
 * @version 03 Jul 2012 - adding method toJSONObject
 * @version 09 Jul 2012 - renaming a few private methods from "toXXX" to "asXXX",
			because they convert parameters, not the object
 * @version 09 Jul 2012 - if the map contains (weakDirection => "Rand"), an
			arbitrary direction is chosen based on other attributes of the map. 
 * @version 15 Jun 2012 - Changing "Rand" to "DontCare" - is slightly more precise
 */
class CannonicalTokenClassFromMap(map:Map[String,Any]) extends CannonicalTokenClass
{
	override def name = map("name").toString
	
	override def body = Some(BodyTypes.withName(map("body").toString))
	
	override def atkElement = Some(Elements.withName(map("element").toString))
	override def atkWeapon = Some(Weaponkinds.withName(map("atkWeapon").toString))
	override def atkStatus = Some(Statuses.withName(map("atkStatus").toString))
	override def range = Some(asInt(map("range")))
	override def speed = Some(asInt(map("speed")))
	override def weakDirection = Some(asDirection(map("weakDirection")))
	override def weakWeapon = asWeakWeaponMap(map("weakWeapon")).mapValues{Some(_)}
	override def weakStatus = Some(Statuses.withName(map("weakStatus").toString))
	
	override def icon:Icon = {
		if (map.contains("icon") && this.getClass().getResource(map("icon").toString) != null)
			loadIcon(this.getClass().getResource(map("icon").toString))
		else
			generateGenericIcon(this)
	}
	
	override def toJSONObject = new JSONObject(mapAsJavaMap(
			map.map({(x:String, y:Any) => 
				y match {
					case z:Map[_, _] => ((JSONString.generateParsed(x), mapAsJavaMap(z)))
					case _ => ((JSONString.generateParsed(x), y))
				}
			}.tupled)
	))
	
	
	
	private def asInt(any:Any):Int = {any match {
		case x:Int => x
		case x:Integer => x
		case x:String => Integer.parseInt(x)
		case x:Any =>  Integer.parseInt(x.toString)
	}}
	
	private def asFloat(any:Any):Float = {any match {
		case x:Int => x.floatValue
		case x:Long => x.floatValue
		case x:Float => x
		case x:Double => x.floatValue
		case x:String => java.lang.Float.parseFloat(x)
		case x:Any => java.lang.Float.parseFloat(x.toString)
	}}
	
	private def asWeakWeaponMap(any:Any):Map[Weaponkind, Float] = {any match {
		case x:scala.collection.Map[_,_] => Map.empty ++ x
		case x:java.util.Map[_,_] => Map.empty ++ mapAsScalaMap(x)
//		case _ => Map.empty
	}}.map{(kindValue:Pair[_,_]) => ((Weaponkinds.withName(kindValue._1.toString), asFloat(kindValue._2)))}
	
	private def arbitraryDirection:Direction = {
		val nameHash = this.name.hashCode
		Directions.values((nameHash % Directions.values.length + Directions.values.length) % Directions.values.length)
	}
	
	private def asDirection(any:Any):Direction = {any match {
		case x:Direction => x
		case "DontCare" => arbitraryDirection
		case x:String => Directions.withName(x)
		case x:Any => asDirection(x.toString)
	}}
}
