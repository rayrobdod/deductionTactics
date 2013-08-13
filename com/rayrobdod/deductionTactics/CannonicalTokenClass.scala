package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction

import javax.swing.Icon
import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import java.io.{StringReader, InputStreamReader}
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.enumerationAsScalaIterator
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, FileSystems}
import com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONObject
import com.rayrobdod.javaScriptObjectNotation.JSONString
import scala.collection.JavaConversions.mapAsJavaMap

/**
 * A supposed-to-be-immutable trait defining a token's class
 * @author Raymond Dodge
 * @version 19 Jan 2012
 * @version 24 Mar 2012 - implementing toString that only uses the name
 * @version 05 Jun 2012 - changing weakWeapon from Some[Map[Weaponkind, Float]]
			to Map[Weaponkind, Some[Float]]
 * @version 03 Jul 2012 - adding method toJSONObject
 */
trait CannonicalTokenClass extends TokenClass
{
	def name:String
	def icon:Icon
	
	def body:Some[BodyType]
	def atkElement:Some[Element]
	def atkWeapon:Some[Weaponkind]
	def atkStatus:Some[Status]
	def range:Some[Int]
	def speed:Some[Int]
	
	def weakDirection:Some[Direction]
	def weakWeapon:Map[Weaponkind,Some[Float]]
	def weakStatus:Some[Status]
	
	override def toString = "CannonicalTokenClass{name:" + name + ";}"
	
	def toJSONObject:JSONObject = {
		
		implicit def stringToJSONString(s:String) = {JSONString.generateParsed(s)}
		
		val returnValue = new JSONObject
		returnValue.put("name", name)
		returnValue.put("body", body.get.toString)
		returnValue.put("element", atkElement.get.name)
		returnValue.put("atkWeapon", atkWeapon.get.name)
		returnValue.put("atkStatus", atkStatus.get.name)
		returnValue.put("range", range.get)
		returnValue.put("speed", speed.get)
		returnValue.put("weakDirection", weakDirection.get.name)
		returnValue.put("weakWeapon", new JSONObject(mapAsJavaMap(
			weakWeapon.map({(x:Weaponkind, y:Some[Float]) => ((JSONString.generateParsed(x.name), y.get))}.tupled)
		)))
		returnValue.put("weakStatus", weakStatus.get.name)
		returnValue
	}
}

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
		case "Rand" => arbitraryDirection
		case x:String => Directions.withName(x)
		case x:Any => asDirection(x.toString)
	}}
}

/**
 * Generates a sequence of tokens
 * 
 * @version ?? Aug 2011
 * @version 06 Oct 2011 - Java7: ListModel takes type paramters now
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 18 Jan 2012 - modified due to changes in the JSONParser
 * @version 19 Jan 2012 - renamed from TokenClass to CannonicalTokenClass
 * @version 04 Jun 2012 - making the tokens a service, instead of a fixed resource
 * @version 12 Jul 2012 - only making a new jar file system if there isn't already one
 * @version 18 Jul 2012 - Changing to use com.rayrobdod.util.services.ResourcesServiceLoader,
			as well as making futher use of Scala Collection's functional interface.
 */
object CannonicalTokenClass
{
	private val SERVICE = "com.rayrobdod.deductionTactics.TokenClass"
	
	val allKnown:ISeq[CannonicalTokenClass] =
	{
		import scala.collection.JavaConversions.iterableAsScalaIterable
		import com.rayrobdod.util.services.ResourcesServiceLoader
		import java.nio.charset.StandardCharsets.UTF_8
		
		val a:Seq[Path] = new ResourcesServiceLoader(SERVICE).toSeq
		val b:Seq[Seq[Any]] = a.map{(jsonPath:Path) => 
			val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
			
			val l = new ToSeqJSONParseListener()
			JSONParser.parse(l, jsonReader)
			l.result
		}
		val c:Seq[Any] = b.flatten
		val d:Seq[Map[String,Any]] = c.map{_ match{
			case x:Map[_, _] => x.map{(i:(Any, Any)) => (i._1.toString, i._2)}
		}}
		val e:Seq[CannonicalTokenClass] = d.map{
			new CannonicalTokenClassFromMap(_)
		}
		ISeq.empty ++ e
	}
	
	import javax.swing.{AbstractListModel, ListModel}
	private object AllKnownListModel extends AbstractListModel[CannonicalTokenClass]
	{
		def getElementAt(index:Int) = allKnown(index)
		def getSize = allKnown.size
	}
	
	val allKnownListModel:ListModel[CannonicalTokenClass] = AllKnownListModel
}
