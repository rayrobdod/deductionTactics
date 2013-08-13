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

/**
 * A supposed-to-be-immutable trait defining a token's class
 * @author Raymond Dodge
 * @version 19 Jan 2012
 * @version 24 Mar 2012 - implementing toString that only uses the name
 * @version 05 Jun 2012 - changing weakWeapon from Some[Map[Weaponkind, Float]]
			to Map[Weaponkind, Some[Float]]
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
 */
class CannonicalTokenClassFromMap(map:Map[String,Any]) extends CannonicalTokenClass
{
	override def name = map("name").toString
	
	override def body = Some(BodyTypes.withName(map("bodyType").toString))
	
	override def atkElement = Some(Elements.withName(map("element").toString))
	override def atkWeapon = Some(Weaponkinds.withName(map("atkWeapon").toString))
	override def atkStatus = Some(Statuses.withName(map("atkStatus").toString))
	override def range = Some(toInt(map("range")))
	override def speed = Some(toInt(map("speed")))
	override def weakDirection = Some(Directions.withName(map("weakDirection").toString))
	override def weakWeapon = toWeakWeaponMap(map("weakWeapon")).mapValues{Some(_)}
	override def weakStatus = Some(Statuses.withName(map("weakStatus").toString))
	
	override def icon:Icon = {
		if (map.contains("icon") && this.getClass().getResource(map("icon").toString) != null)
			loadIcon(this.getClass().getResource(map("icon").toString))
		else
			generateGenericIcon(this)
	}
	
	private def toInt(any:Any):Int = {any match {
		case x:Int => x
		case x:Integer => x
		case x:String => Integer.parseInt(x)
		case x:Any =>  Integer.parseInt(x.toString)
	}}
	
	private def toFloat(any:Any):Float = {any match {
		case x:Int => x.floatValue
		case x:Long => x.floatValue
		case x:Float => x
		case x:Double => x.floatValue
		case x:String => java.lang.Float.parseFloat(x)
		case x:Any => java.lang.Float.parseFloat(x.toString)
	}}
	
	private def toWeakWeaponMap(any:Any):Map[Weaponkind, Float] = {any match {
		case x:scala.collection.Map[_,_] => Map.empty ++ x
		case x:java.util.Map[_,_] => Map.empty ++ mapAsScalaMap(x)
//		case _ => Map.empty
	}}.map{(kindValue:Pair[_,_]) => ((Weaponkinds.withName(kindValue._1.toString), toFloat(kindValue._2)))} 
}

/**
 * Generates a sequence of tokens
 * 
 * @version ?? Aug 2011
 * @version 06 Oct 2011 - Java7: ListModel takes type paramters now
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 18 Jan 2011 - modified due to changes in the JSONParser
 * @version 19 Jan 2011 - renamed from TokenClass to CannonicalTokenClass
 * @version 04 Jun 2012 - making the tokens a service, instead of a fixed resource
 */
object CannonicalTokenClass
{
	private val SERVICE = "com.rayrobdod.deducitonTactics.TokenClass"
	private val PREFIX = "META-INF/services/"
	private val fullName = PREFIX + SERVICE
	
	private def listOfClassFileFiles = ClassLoader.getSystemResources(fullName)
	private val listOfClassFiles = listOfClassFileFiles.map{(oneServiceFileURL:URL) =>
		if (oneServiceFileURL.toString().startsWith("jar:"))
		{
			val env = new java.util.HashMap[String, String](); 
			env.put("create", "true");
			
			FileSystems.newFileSystem(new java.net.URI(oneServiceFileURL.toString().split('!').apply(0)), env)
		}
		
		val oneServiceFilePath = Paths.get(oneServiceFileURL.toURI)
		
		Files.readAllLines(oneServiceFilePath, StandardCharsets.UTF_8)
	}.flatten
	
	private def turnOneFileIntoASeqOfClasses(location:String) =
	{
		val reader:InputStreamReader = new InputStreamReader(
				this.getClass.getResourceAsStream(location))
		
		val listener2 = new ToSeqJSONParseListener()
		JSONParser.parse(listener2, reader)
		val jsonObjectSeq:Seq[Any] = listener2.result
		
		val jsonMapSeq = jsonObjectSeq.map{_ match{
//			case x:Map[String, _] => x
			case x:Map[_, _] => x.map{(i:(Any, Any)) => (i._1.toString, i._2)}
		}}
		
		jsonMapSeq.map{new CannonicalTokenClassFromMap(_)}
	}
	
	lazy val allKnown:ISeq[CannonicalTokenClass] =
	{
		ISeq.empty ++ listOfClassFiles.map{turnOneFileIntoASeqOfClasses(_)}.flatten
	}
	
	import javax.swing.{AbstractListModel, ListModel}
	private object AllKnownListModel extends AbstractListModel[CannonicalTokenClass]
	{
		def getElementAt(index:Int) = allKnown(index)
		def getSize = allKnown.size
	}
	
	val allKnownListModel:ListModel[CannonicalTokenClass] = AllKnownListModel
}
