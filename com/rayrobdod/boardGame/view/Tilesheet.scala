package com.rayrobdod.boardGame.view

import com.rayrobdod.boardGame.{RectangularField, SpaceClassConstructor}
import scala.util.Random
import scala.parallel.Future
import java.io.{File, StringReader, Reader}
import java.awt.{Image, Color}
import com.rayrobdod.util.BlitzAnimImage
import java.net.URL
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import com.rayrobdod.boardGame.{RectangularSpaceConstructedField => RSCField}
import scala.collection.JavaConversions.mapAsScalaMap
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Path, Paths, Files}

/**
 * A sequence of rules that defines which images reqresent which spaces in a Component
 * 
 * @author Raymond Dodge
 * @version 03 Aug 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
trait Tilesheet
{
	/** The tilesheet's name */
	def name:String
	/** The rules in the tilesheet */
	def rules:Seq[RectangularVisualizationRule]
	
//	def frameImage:BlitzAnimImage
//	def classMap:Map[String, SpaceClassConstructor]
}

/**
 * 
 * 
 * @author Raymond Dodge
 * @version 03 Aug 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 18 Jan 2011 - modified due to changes in the JSONParser
 * @version 29 May 2012 - added function this(java.io.Reader, java.net.URL)
 * @version 30 May 2012 - removed function this(java.io.Reader, java.net.URL)
 * @version 30 May 2012 - changing a FileReader based approach to a Files.newBufferedreader based approach
 * @version 24 Jun 2012 - closing a few opened Readers
 * @version 25 Jun 2012 - there is no point to mapReader being public, so it is no longer public.
 		Also, making map public - incase the map includes more information than the tilesheet knows what to do with.
 * @param jsonURL the URL of the JSONFile to read and turn into a tilesheet
 */
class JSONTilesheet(jsonURL:URL) extends Tilesheet
{
	private implicit def urlToUri(url:URL) = url.toURI
	
	val map:Map[String,Any] = {
		val mapReader = Files.newBufferedReader(Paths.get(jsonURL.toURI), UTF_8)
		
		val listener = new ToSeqJSONParseListener()
		JSONParser.parse(listener, mapReader)
		mapReader.close()
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
	
	val classMap:Map[String, SpaceClassConstructor] = 
	{
		val classMapURL = new URL(jsonURL, map("classMap").toString)
		val classMapReader = Files.newBufferedReader(Paths.get(classMapURL.toURI), UTF_8)
		
		val listener = new ToSeqJSONParseListener()
		JSONParser.parse(listener, classMapReader)
		classMapReader.close()
		val classNames = listener.resultMap.mapValues{_.toString}
		
		classNames.mapValues{this.sterilizeSpaceClassConstructorName(_)}
	}
	
	override val rules:Seq[RectangularVisualizationRule] = 
	{
		val rulesURL = new URL(jsonURL, map("rules").toString)
		val rulesReader = Files.newBufferedReader(Paths.get(rulesURL.toURI), UTF_8)
		
		val listener = new ToSeqJSONParseListener()
		JSONParser.parse(listener, rulesReader)
		rulesReader.close()
		
		val rulesJSON:Seq[Map[_,_]] = listener.result.map{_ match {
				case x:scala.collection.Map[_,_] => Map.empty ++ x
/*				case x:JSONObject => {
					// TODO get JSONObject to work correctly so this is not neccessary
					val listener2 = new ToSeqJSONParseListener()
					JSONParser.parse(listener2, new StringReader(x.getUnparsed.toString))
					listener2.resultMap
				}
*/				case x:java.util.Map[_,_] => Map.empty ++ mapAsScalaMap(x)
				case x:Any => Map.empty
			}}.filterNot{_.isEmpty}
		val rulesJSON2:Seq[Map[String,_]] = rulesJSON.map{_.map{pair:(Any,Any) => (pair._1.toString, pair._2)}}
		
		rulesJSON2.map{new MapRunVisualizationRule(classMap, _, frameImage)}
	}
	
	def sterilizeSpaceClassConstructorName(objectName:String):SpaceClassConstructor =
	{
		val clazz = Class.forName(objectName + "$")
		val field = clazz.getField("MODULE$")
		
		field.get(null).asInstanceOf[SpaceClassConstructor]
	}
}
