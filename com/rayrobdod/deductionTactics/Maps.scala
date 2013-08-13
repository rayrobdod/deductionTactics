package com.rayrobdod.deductionTactics

import scala.swing.event.Event
import scala.collection.immutable.{Seq => ISeq, Set}
import scala.collection.mutable.{Seq => MSeq}
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.parallel.Future
import com.rayrobdod.util.services.{ResourcesServiceLoader, Services}
import com.rayrobdod.commaSeparatedValues.parser.{CSVParser, ToSeqSeqCSVParseListener, CSVPatterns}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
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
 * @version 2013 Jun 16 - allowing startSpaces to be inlined
 * @version 2013 Jun 23 - responding to changes in JSON module; mostly ToSeqJSONParseListener → ToScalaCollection
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
	
	private def getMetadata(index:Int):Map[String, Any] = {
		val metadataPath = Maps.paths(index)
		val metadataMap:Map[String,Any] = {
			val reader = Files.newBufferedReader(metadataPath, UTF_8);
			val listener = ToScalaCollection()
			JSONParser.parse(listener, reader)
			reader.close()
			listener.resultMap
		}
		
		metadataMap
	}
	
	def getMap(index:Int):RectangularField = {
		val metadataPath = Maps.paths(index)
		val metadataMap = getMetadata(index)
		
		val letterToSpaceClassConsMap:Map[String,SpaceClassConstructor] = {
			val path = metadataPath.getParent.resolve(metadataMap("classMap").toString)
			val reader = Files.newBufferedReader(path, UTF_8)
			val listener = ToScalaCollection()
			JSONParser.parse(listener, reader)
			val letterToClassNameMap = listener.resultMap.mapValues{_.toString}
			reader.close()
			
			letterToClassNameMap.mapValues{(objectName:String) => 
				val clazz = Class.forName(objectName + "$")
				val field = clazz.getField("MODULE$")
				
				field.get(null).asInstanceOf[SpaceClassConstructor]
			}
		}
		
		val layoutPath = metadataPath.getParent.resolve(metadataMap("layout").toString)
		val layoutReader = Files.newBufferedReader(layoutPath, UTF_8)
		val layoutTable:ISeq[ISeq[SpaceClassConstructor]] = {
			val listener = new ToSeqSeqCSVParseListener()
			new CSVParser(CSVPatterns.commaDelimeted).parse(listener, layoutReader)
			val letterTable = listener.result
			
			letterTable.map{_.map{letterToSpaceClassConsMap}}
		}
		layoutReader.close()
		
		RectangularField.applySCC(layoutTable)
	}
	
	def possiblePlayers(index:Int):Set[Int] = {
		val metadataPath = Maps.paths(index)
		val metadataMap = getMetadata(index)
		
		val startSpaceValue = metadataMap("deductionTactics::startSpaces")
		val startSpaceMap:Map[String,Any] = startSpaceValue match {
			case x:Map[_,_] => x.map{x => ((x._1.toString, x._2))}
			case _ => {
				val startSpacePath = metadataPath.getParent.resolve(startSpaceValue.toString)
				val startSpaceReader = Files.newBufferedReader(startSpacePath, UTF_8)
				
				val listener = ToScalaCollection()
				JSONParser.parse(listener, startSpaceReader)
				startSpaceReader.close()
				listener.resultMap
			}
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
		
		val startSpaceValue = metadataMap("deductionTactics::startSpaces")
		val startSpaceMapRaw:Map[String,Any] = startSpaceValue match {
			case x:Map[_,_] => x.map{x => ((x._1.toString, x._2))}
			case _ => {
				val startSpacePath = metadataPath.getParent.resolve(startSpaceValue.toString)
				val startSpaceReader = Files.newBufferedReader(startSpacePath, UTF_8)
				
				val listener = ToScalaCollection()
				JSONParser.parse(listener, startSpaceReader)
				startSpaceReader.close()
				listener.resultMap
			}
		}
		
		val startSpaceMap:Map[String,ISeq[ISeq[(Int, Int)]]] = {
			startSpaceMapRaw.mapValues{_ match {
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
