package net.verizon.rayrobdod.boardGame.view

import net.verizon.rayrobdod.boardGame.{RectangularField, SpaceClassConstructor => SpaceConstructor}
import scala.util.Random
import scala.parallel.Future
import java.io.{FileReader,File,StringReader}
import java.awt.{Image, Color}
import net.verizon.rayrobdod.util.BlitzAnimImage
import java.net.URL
import net.verizon.rayrobdod.javaScriptObjectNotation.parser.{ToSeqJSONParseListener, JSONParser}
import net.verizon.rayrobdod.javaScriptObjectNotation.JSONObject
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import net.verizon.rayrobdod.boardGame.{RectangularSpaceConstructedField => RSCField}
import scala.collection.JavaConversions.mapAsScalaMap

/**
 * 
 * @author Raymond Dodge
 * @version 03 Aug 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 */
trait Tilesheet
{
	def name:String
//	def frameImage:BlitzAnimImage
	def rules:Seq[RectangularVisualizationRule]
//	def classMap:Map[String, SpaceConstructor]
}

/**
 * 
 * @author Raymond Dodge
 * @version 03 Aug 2011
 */
class JSONTilesheet(jsonURL:URL) extends Tilesheet
{
	private implicit def urlToUri(url:URL) = url.toURI
	
	val mapReader = new FileReader(new File(jsonURL))
	private val map:Map[String,Any] = {
		val listener = new ToSeqJSONParseListener()
		JSONParser.parse(listener, mapReader)
		listener.resultMap
	}
	
	override val name:String = map("name").toString
	
	private val frameImage = 
	{
		val sheetURL:URL = new URL(jsonURL, map("tiles").toString)
		val sheetImage:BufferedImage = ImageIO.read(sheetURL)
		val tileWidth:Int = Integer.parseInt(map("tileWidth").toString)
		val tileHeight:Int = Integer.parseInt(map("tileHeight").toString)
		val tilesX = sheetImage.getWidth / tileWidth
		val tilesY = sheetImage.getHeight / tileHeight
		
		new BlitzAnimImage(sheetImage, tileWidth, tileHeight, 0, tilesX * tilesY)
	}
	
	val classMap:Map[String, SpaceConstructor] = 
	{
		val classMapURL = new URL(jsonURL, map("classMap").toString)
		val classMapReader = new FileReader(new File(classMapURL))
		
		val listener = new ToSeqJSONParseListener()
		JSONParser.parse(listener, classMapReader)
		val classNames = listener.resultMap.mapValues{_.toString}
		
		classNames.mapValues{this.sterilizeSpaceConstructorName(_)}
		
	}
	
	override val rules:Seq[RectangularVisualizationRule] = 
	{
		val rulesURL = new URL(jsonURL, map("rules").toString)
		val rulesReader = new FileReader(new File(rulesURL))
		
		val listener = new ToSeqJSONParseListener()
		JSONParser.parse(listener, rulesReader)
		
		val rulesJSON:Seq[Map[_,_]] = listener.result.map{_ match {
				case x:scala.collection.Map[_,_] => Map.empty ++ x
				case x:JSONObject => {
					// TODO get JSONObject to work correctly so this is not neccessary
					val listener2 = new ToSeqJSONParseListener()
					JSONParser.parse(listener2, new StringReader(x.getUnparsed.toString))
					listener2.resultMap
				}
				case x:java.util.Map[_,_] => Map.empty ++ mapAsScalaMap(x)
				case x:Any => Map.empty
			}}.filterNot{_.isEmpty}
		val rulesJSON2:Seq[Map[String,_]] = rulesJSON.map{_.map{pair:(Any,Any) => (pair._1.toString, pair._2)}}
		
		rulesJSON2.map{new MapRunVisualizationRule(classMap, _, frameImage)}
	}
	
	def sterilizeSpaceConstructorName(objectName:String):SpaceConstructor =
	{
		val clazz = Class.forName(objectName + "$")
		val field = clazz.getField("MODULE$")
		
		field.get(null).asInstanceOf[SpaceConstructor]
	}
}
