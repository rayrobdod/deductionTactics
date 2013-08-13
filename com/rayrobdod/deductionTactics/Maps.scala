package com.rayrobdod.deductionTactics

import scala.swing.event.Event
import scala.collection.immutable.{Seq => ISeq, Set}
import scala.collection.mutable.{Seq => MSeq}
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.parallel.Future
import com.rayrobdod.util.services.{ResourcesServiceLoader, Services}
import com.rayrobdod.commaSeparatedValues.parser.{CSVParser, ToSeqSeqCSVParseListener, CSVPatterns}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.mapValuesFromObjectNameToSpaceClassConstructor
import com.rayrobdod.boardGame.{Moved, SpaceClassConstructor}
import java.io.InputStreamReader
import java.nio.file.{Files, Path, Paths, FileSystems}
import java.nio.charset.StandardCharsets.UTF_8

/**
 * An object that deals with Maps
 * 
 * @version 28 Nov 2012
 * @version 22 Dec 2012 - making metadata a thing, and making the entry point a file rather than a string
 */
// metadata
// starting locations
// tiles
object Maps
{
	private val SERVICE = "com.rayrobdod.deductionTactics.Maps"
	
	val names:ISeq[String] = {
		ISeq.empty ++ Services.readServices(SERVICE);
	}
	
	val paths:ISeq[Path] = {
		ISeq.empty ++ new ResourcesServiceLoader(SERVICE);
	}
	
	private def getMetadata(index:Int):Map[String, String] = {
		val metadataPath = Maps.paths(index)
		val metadataReader = Files.newBufferedReader(metadataPath, UTF_8);
		val metadataMap:Map[String,String] = {
			val listener = new ToSeqJSONParseListener()
			JSONParser.parse(listener, metadataReader)
			listener.resultMap.mapValues{_.toString}
		}
		
		metadataMap
	}
	
	def getMap(index:Int):RectangularField = {
		val metadataPath = Maps.paths(index)
		val metadataMap = getMetadata(index)
		
		val letterToSpaceClassConsPath = metadataPath.getParent.resolve(metadataMap("classMap"))
		val letterToSpaceClassConsReader = Files.newBufferedReader(letterToSpaceClassConsPath, UTF_8)
		val letterToSpaceClassConsMap:Map[String,SpaceClassConstructor] = {
			val listener = new ToSeqJSONParseListener()
			JSONParser.parse(listener, letterToSpaceClassConsReader)
			val letterToClassNameMap = listener.resultMap.mapValues{_.toString}
			
			letterToClassNameMap.mapValues{(objectName:String) => 
				val clazz = Class.forName(objectName + "$")
				val field = clazz.getField("MODULE$")
				
				field.get(null).asInstanceOf[SpaceClassConstructor]
			}
		}
		
		val layoutPath = metadataPath.getParent.resolve(metadataMap("layout"))
		val layoutReader = Files.newBufferedReader(layoutPath, UTF_8)
		val layoutTable:ISeq[ISeq[SpaceClassConstructor]] = {
			val listener = new ToSeqSeqCSVParseListener()
			new CSVParser(CSVPatterns.commaDelimeted).parse(listener, layoutReader)
			val letterTable = listener.result
			
			letterTable.map{_.map{letterToSpaceClassConsMap}}
		}
		
		RectangularField.applySCC(layoutTable)
	}
	
	def possiblePlayers(index:Int):Set[Int] = {
		val metadataPath = Maps.paths(index)
		val metadataMap = getMetadata(index)
		
		val startSpacePath = metadataPath.getParent.resolve(metadataMap("deductionTactics::startSpaces"))
		val startSpaceReader = Files.newBufferedReader(startSpacePath, UTF_8)
		val startSpaceMap:Map[String,Any] = {
			val listener = new ToSeqJSONParseListener()
			JSONParser.parse(listener, startSpaceReader)
			listener.resultMap
		}
		startSpaceMap.keySet.map{ Integer.parseInt(_) }
	}
	
	def startingPositions(index:Int, numPlayers:Int):ISeq[ISeq[(Int, Int)]] = {
		def asInt(any:Any):Int = {any match {
			case x:Int => x
			case x:Integer => x
			case x:Long => x.toInt
			case x:String => Integer.parseInt(x)
			case x:Any =>  Integer.parseInt(x.toString)
		}}
		
		val metadataPath = Maps.paths(index)
		val metadataMap = getMetadata(index)
		
		val startSpacePath = metadataPath.getParent.resolve(metadataMap("deductionTactics::startSpaces"))
		val startSpaceReader = Files.newBufferedReader(startSpacePath, UTF_8)
		val startSpaceMap:Map[String,ISeq[ISeq[(Int, Int)]]] = {
			val listener = new ToSeqJSONParseListener()
			JSONParser.parse(listener, startSpaceReader)
			listener.resultMap.mapValues{_ match {
				case x:ISeq[_] => x.map{_ match {
					case y:ISeq[_] => y.map{_ match {
						case ISeq(i:Any, j:Any) => {
							((asInt(i), asInt(j)))
						}
					}}
				}}
			}}
		}
		startSpaceMap(numPlayers.toString)
	}
}
