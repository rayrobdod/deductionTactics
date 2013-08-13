package com.rayrobdod.boardGame.swingView

import com.rayrobdod.boardGame._
import com.rayrobdod.util.BlitzAnimImage
import com.rayrobdod.animation.{AnimationIcon, ImageFrameAnimation}
import scala.util.Random
import scala.parallel.Future
import scala.annotation.tailrec
import scala.collection.immutable.{Seq, Map, Vector, Set}
import scala.collection.mutable.{Map => MMap}
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.{TYPE_INT_RGB => nonAlphaImage, TYPE_INT_ARGB => alphaImage}
import java.net.URL
import javax.swing.{Icon, ImageIcon}
import java.util.regex.{Pattern, Matcher}
import javax.script.{Bindings, SimpleBindings, ScriptEngineManager, Compilable, CompiledScript}

/**
 * @author Raymond Dodge
 * @param 2012 Aug 23
 * @param 2012 Aug 25 - decided upon the constructors
 * @param 2012 Aug 27 - found a place where frames and layers were switched, and fixes it, and enabling animations 
 */
// class VisualizationRuleBasedRectangularTilesheet extends RectangularTilesheet
class JSONRectangularTilesheet(
		val name:String,
		val visualizationRules:Set[JSONRectangularVisualizationRule]
) extends RectangularTilesheet {
	
	def getIconFor(field:RectangularField, x:Int, y:Int, rng:Random):(Icon, Icon) =
	{
		type ImageFrames = Seq[Image]
		
		val layers:Map[Int, ImageFrames] = visualizationRules.filter{
			_.matches(field, x, y, rng)
		}.toSeq.sortBy{
			_.priority
		}.foldLeft(Map.empty[Int, ImageFrames]){
			_ ++ _.iconParts
		}
		
		val lowHighLayers = layers.partition{_._1 < 0}
		
		// assumes that all images are the same size
		def mashTogetherLayers(layers:Map[Int, ImageFrames]):ImageFrames =
		{
			val layers2:Map[Int, ImageFrames] = layers
			val layersInOrder:Seq[ImageFrames] = Vector.empty ++ layers2.toSeq.sortBy{_._1}.map{_._2}.filter{_.length > 0}
			
			object lcm extends Function2[Int, Int, Int]{
				def apply(x:Int, y:Int):Int = {
					x / gcdApply(x,y) * y	
				}
				
				@tailrec def gcdApply(x:Int, y:Int):Int = {
					if (y == 1) {x} else
					if (x == 1) {y} else
					if (x == y) {x} else
					if (x > y) {gcdApply(x, x - y)} else
					{gcdApply(y - x, x)}
				}
			}
			
			val leastCommonFrameNumber:Int = layersInOrder.map{_.length}.fold(1){lcm}
			
			// after this, all layers will have the same number of frames
			val layersWithLCMFrames:Seq[ImageFrames] = layersInOrder.map{(x:ImageFrames) =>
				val countRepeat = 1 to (leastCommonFrameNumber / x.length)
				val frameRepeat = countRepeat.map{(y:Int) => x}
				frameRepeat.flatten
			}
			
			if (! layersWithLCMFrames.isEmpty)
			{
				// FIXTHIS: assumes all images are the same size
				val imageWidth = layersWithLCMFrames.head.head.getWidth(null)
				val imageHeight = layersWithLCMFrames.head.head.getHeight(null)
				
				// merge all the layers in each frame into one image per frame
				val frames:ImageFrames = layersWithLCMFrames.foldLeft(
						Seq.fill(leastCommonFrameNumber){
							new BufferedImage(imageWidth, imageHeight, alphaImage)
						}
				){(newImage:Seq[BufferedImage], layer:ImageFrames) =>
						newImage.zip(layer).map({(newImage:BufferedImage, layer:Image) =>
							newImage.getGraphics.drawImage(layer, 0, 0, null)
							newImage
						}.tupled)
				}
				
				frames
			}
			else
			{
				Seq(new BufferedImage(1,1,alphaImage))
			}
		}
		
		def imageFramesToIcon(x:ImageFrames):Icon = {
			if (x.length == 1) {
				new ImageIcon(x.head)
			} else {
				new AnimationIcon(new ImageFrameAnimation(x, 1000/5, true))
			}
		}
		
		val lowHighImages = (
			imageFramesToIcon(mashTogetherLayers(lowHighLayers._1)),
			imageFramesToIcon(mashTogetherLayers(lowHighLayers._2)) 
		) 
		
		lowHighImages
	}
}

/**
 * @author Raymond Dodge
 * @param 2012 Aug 25
 */
object JSONRectangularTilesheet
{
	import java.nio.file.{Files, Paths}
	import java.nio.charset.StandardCharsets.UTF_8
	import javax.imageio.ImageIO
	import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
	import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
	import scala.collection.JavaConversions.mapAsScalaMap

	def apply(url:URL):JSONRectangularTilesheet = 
	{
		val fileReader = Files.newBufferedReader(Paths.get(url.toURI), UTF_8)
		
		val listener = new ToSeqJSONParseListener()
		JSONParser.parse(listener, fileReader)
		fileReader.close()
		
		val jsonMap = listener.resultMap.map{(x) => ((x._1.toString, x._2))}
		this.apply(url, jsonMap)
	}
	
	def apply(baseURL:URL, jsonMap:Map[String,Any]):JSONRectangularTilesheet = {
		import JSONRectangularViualizationRule.asInt
		
		val frameImage = 
		{
			val sheetURL:URL = new URL(baseURL, jsonMap("tiles").toString)
			val sheetImage:BufferedImage = ImageIO.read(sheetURL)
			val tileWidth:Int = asInt(jsonMap("tileWidth"))
			val tileHeight:Int = asInt(jsonMap("tileHeight"))
			val tilesX = sheetImage.getWidth / tileWidth
			val tilesY = sheetImage.getHeight / tileHeight
			
			new BlitzAnimImage(sheetImage, tileWidth, tileHeight, 0, tilesX * tilesY)
		}
		
		val classMap:Map[String, SpaceClassConstructor] = 
		{
			// TODO: allow inline
			val classMapURL = new URL(baseURL, jsonMap("classMap").toString)
			val classMapReader = Files.newBufferedReader(Paths.get(classMapURL.toURI), UTF_8)
			
			val listener = new ToSeqJSONParseListener()
			JSONParser.parse(listener, classMapReader)
			classMapReader.close()
			val classNames = listener.resultMap.mapValues{_.toString}
			
			classNames.mapValues{(objectName:String) => 
				val clazz = Class.forName(objectName + "$")
				val field = clazz.getField("MODULE$")
				
				field.get(null).asInstanceOf[SpaceClassConstructor]
			}
		}
	
		val rules:Seq[JSONRectangularVisualizationRule] = 
		{
			val rulesURL = new URL(baseURL, jsonMap("rules").toString)
			val rulesReader = Files.newBufferedReader(Paths.get(rulesURL.toURI), UTF_8)
			
			val listener = new ToSeqJSONParseListener()
			JSONParser.parse(listener, rulesReader)
			rulesReader.close()
			
			val rulesJSON:Seq[Map[_,_]] = Seq.empty ++ listener.result.map{_ match {
				case x:scala.collection.Map[_,_] => Map.empty ++ x
				case x:java.util.Map[_,_] => Map.empty ++ mapAsScalaMap(x)
				case x:Any => Map.empty
			}}.filterNot{_.isEmpty}
			val rulesJSON2:Seq[Map[String,_]] = rulesJSON.map{_.map{pair:(Any,Any) => (pair._1.toString, pair._2)}}
			
			rulesJSON2.map{new JSONRectangularVisualizationRule(_, Seq.empty ++ frameImage.getImages, classMap)}
		}
		
		this.apply(jsonMap("name").toString, rules)
	}
	
	def apply(
			name:String,
//			tiles:BlitzAnimImage,
//			classMap:Map[String, SpaceClassConstructor]
			rules:Seq[JSONRectangularVisualizationRule]
	):JSONRectangularTilesheet = {
		new JSONRectangularTilesheet(name, Set.empty ++ rules)
	}
	
	
}

/**
 * @author Raymond Dodge
 * @param 2012 Aug 24
 * @todo needed? wanted?
 */
abstract class RectangularVisualizationRule
{
	def indexiesMatch(x:Int, y:Int, width:Int, height:Int):Boolean
	def surroundingTilesMatch(field:RectangularField, x:Int, y:Int):Boolean
	def randsMatch(rng:Random):Boolean
	
	final def matches(field:RectangularField, x:Int, y:Int, rng:Random):Boolean =
	{
		indexiesMatch(x, y, field.spaces.size, field.spaces(0).size) &&
				surroundingTilesMatch(field, x, y) &&
				randsMatch(rng)
	}
	
	def priority:Int
}

/**
 * @author Raymond Dodge
 * @param 2012 Aug 23-24
 * @param 2012 Aug 27 - implementing priority
 * @param 2012 Aug 28 - making out-of-bounds matches work
 */
class JSONRectangularVisualizationRule(
		jsonMap:Map[String, Any],
		tileSeq:Seq[Image],
		sccMapping:Map[String, SpaceClassConstructor]
) extends RectangularVisualizationRule {
	import JSONRectangularViualizationRule.{asInt, asBoolean, asMapOfFrameIndexies, asIndexTranslationFunction}
	type IndexConverter = Function1[(Int, Int), (Int, Int)]
	
	// Map[layer, frames]
	def iconParts:Map[Int, Seq[Image]] = asMapOfFrameIndexies(jsonMap("tiles")).mapValues{_.map{tileSeq}}
	def surroundingTiles:Map[IndexConverter, SpaceClassConstructor] =
		jsonMap.getOrElse("surroundingSpaces", Map.empty).asInstanceOf[Map[_,_]].map{(x:(Any,Any)) => (( asIndexTranslationFunction(x._1.toString), sccMapping(x._2.toString) ))}
	def tileRand:Int = asInt(jsonMap.getOrElse("tileRand", 1))
	def indexEquation:String = jsonMap.getOrElse("indexies", true).toString
	
	override def indexiesMatch(x:Int, y:Int, width:Int, height:Int):Boolean =
	{
		import JSONRectangularViualizationRule.{scriptEngine, buildBindings, executeScript}
		
		// identified as a bottleneck
		//asBoolean( scriptEngine.eval(indexEquation, buildBindings(x, y, width, height)) )
		asBoolean( executeScript(indexEquation, buildBindings(x, y, width, height)) )
	}
	
	override def surroundingTilesMatch(field:RectangularField, x:Int, y:Int) =
	{
		surroundingTiles.forall({(conversion:IndexConverter, scc:SpaceClassConstructor) =>
			val newIndexies = conversion( ((x,y)) )
			if (field.containsIndexies(newIndexies._1, newIndexies._2))
			{
				val spaceClass = field.space(newIndexies._1, newIndexies._2).typeOfSpace
				
				scc.unapply(spaceClass)
			} else {true}
		}.tupled)
	}
	
	override def randsMatch(rng:Random):Boolean = {
		rng.nextInt(tileRand) == 0
	}
	
	final override def priority:Int = {
		@tailrec def countMatches(m:Matcher, total:Int = 0):Int =
		{
			if (! m.hitEnd)
			{
				m.find
				countMatches(m, total + 1)
			}
			else {total}
		}
		import JSONRectangularViualizationRule.{divisionPattern, andPattern}
		
		surroundingTiles.size * 10000 + tileRand +
			(if (indexEquation != "true") {
				1000 / {
					countMatches( divisionPattern.matcher(indexEquation) ) + 1
				} * {
					countMatches( andPattern.matcher(indexEquation) )
				} + {
					import JSONRectangularViualizationRule.numberPattern
					
					@tailrec def sumMatches(m:Matcher, total:Int = 0):Int =
					{
						if (! m.hitEnd)
						{
							m.find()
							val number = Integer.parseInt(m.group)
							sumMatches(m, total + number)
						} else {total}
					}
					
					val m = numberPattern.matcher(indexEquation)
					sumMatches(m)
				}
			} else 0)
	}
}

/**
 * @author Raymond Dodge
 * @param 2012 Aug 23-24
 * @param 2012 Aug 27 - asInt and asMapOfFrameIndexies can now handle Long values
 */
object JSONRectangularViualizationRule
{
	val divisionPattern = Pattern.compile("[%//]")
	val numberPattern = Pattern.compile("\\d+")
	val andPattern = Pattern.compile("&")
	
	val scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript")
	
	def buildBindings(x:Int, y:Int, width:Int, height:Int):Bindings =
	{
		val binding = new SimpleBindings()
		binding.put("x", x)
		binding.put("y", y)
		binding.put("w", width)
		binding.put("h", height)
		binding
	}
	
	// TODO: move to helper thing?
	// doesn't actually seem to help. Like at all.
//	private val compiledScripts:MMap[String, CompiledScript] = MMap.empty
	def executeScript(script:String, bindings:Bindings) = {
/*		scriptEngine match {
			case x:Compilable => {
				if (compiledScripts.contains(script)) {
					compiledScripts(script).eval(bindings);
				} else {
					val compiled = x.compile(script);
					compiledScripts(script) = compiled;
					compiled.eval(bindings);
				}
			}
			case _ => {
*/				scriptEngine.eval(script, bindings)
//			}
//		}
	}
	
	// TODO: see how much turning this into a function will help
	def asInt(x:Any):Int = x match {
		case y:Int => y
		case y:String => Integer.parseInt(y)
		case y:Integer => y
		case y:Long => y.intValue
	}
	
	def asBoolean(x:Any):Boolean = x match {
		case y:Boolean => y
		case y:Int => y != 0
		case y:String => java.lang.Boolean.parseBoolean(y)
	}
	
	def asMapOfFrameIndexies(frameIndexies:Any) =
	{
		val normalizedFrameIndex:Map[Int, Seq[Int]] = frameIndexies match {
			case x:Int => Map(-127 → Seq(x) )
			case x:Long => Map( -127 → Seq(x.intValue) )
			case x:Seq[_] => Map( -127 → x.map{asInt(_)} )
			case x:Map[_, _] => {
				x.map{(y:Tuple2[_,_]) => (( asInt(y._1), y._2)) }
						.mapValues{_ match {
							case y:Int => Seq(y)
							case y:Long => Seq(y.intValue)
							case y:Seq[_] => y.map{asInt(_)}
						}}
			}
		}
		
		normalizedFrameIndex
	}
	
	def asIndexTranslationFunction(s:String) =
	{
		import java.util.regex.{Pattern, Matcher}
		
		val pairPattern = Pattern.compile("""\(([\+\-]?\d+),([\+\-]?\d+)\)""")
		val matcher = pairPattern.matcher(s)
		if (!matcher.matches())
			throw new IllegalArgumentException(s + " does not match pair pattern.")
		val firstStr = matcher.group(1)
		val secondStr = matcher.group(2)
		val firstInt = asInt(firstStr)
		val secondInt = asInt(secondStr)
		
		new Function1[(Int, Int), (Int, Int)]
		{
			def apply(x:(Int, Int)) = 
				((x._1 + firstInt, x._2 + secondInt))
		}
	}
}
