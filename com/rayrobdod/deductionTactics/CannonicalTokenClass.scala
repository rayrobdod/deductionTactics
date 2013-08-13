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
import com.rayrobdod.javaScriptObjectNotation.parser.decoders.ToScalaCollectionJSONDecoder
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
	
	// TODO: remove dependency on JSONObject
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
 * A CannonicalTokenClass that has all of it's values defined
 * directly in the constructor 
 * @author Raymond Dodge
 * @version 03 Jul 2012 - adding method toJSONObject
 */
final class CannonicalTokenClassBlunt(
	override val name:String,
	override val icon:Icon,
	
	override val body:Some[BodyType],
	override val atkElement:Some[Element],
	override val atkWeapon:Some[Weaponkind],
	override val atkStatus:Some[Status],
	override val range:Some[Int],
	override val speed:Some[Int],
	
	override val weakDirection:Some[Direction],
	override val weakWeapon:Map[Weaponkind,Some[Float]],
	override val weakStatus:Some[Status]
) extends CannonicalTokenClass

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
 * @version 14 Jun 2013 - Closing jsonReader
 * @version 2013 Jun 23 - using CannonicalTokenClassParseListener and friends instead of CannonicalTokenClassFromMap
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
		
		
		// I can't tell which one is faster, but I think this one uses less memory 
		// also, this one has more classes and is probably larger in code size than the other one
		val b:Seq[Seq[CannonicalTokenClass]] = a.map{(jsonPath:Path) => 
			val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
			
			val l = new ToScalaCollection(CannonicalTokenClassDecoder)
			JSONParser.parse(l, jsonReader)
			jsonReader.close()
			l.resultSeq
		}
		val e = b.flatten
		
		
		/*
		val b:Seq[Seq[Any]] = a.map{(jsonPath:Path) => 
			val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
			
			val l = new ToScalaCollection(ToScalaCollectionJSONDecoder)
			JSONParser.parse(l, jsonReader)
			jsonReader.close()
			l.resultSeq
		}
		val c:Seq[Any] = b.flatten
		val d:Seq[Map[String,Any]] = c.map{_ match{
			case x:Map[_, _] => x.map{(i:(Any, Any)) => (i._1.toString, i._2)}
		}}
		val e:Seq[CannonicalTokenClass] = d.map{
			new CannonicalTokenClassFromMap(_)
		} */
		
		
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
