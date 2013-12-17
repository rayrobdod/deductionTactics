/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.rayrobdod.deductionTactics

import scala.collection.immutable.{Seq => ISeq, Set}
import scala.collection.mutable.{Seq => MSeq}
import scala.collection.JavaConversions.iterableAsScalaIterable
import com.rayrobdod.util.services.{ResourcesServiceLoader, Services}
import au.com.bytecode.opencsv.CSVReader;
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.boardGame.{RectangularField, SpaceClassConstructor}
import com.rayrobdod.boardGame.mapValuesFromObjectNameToSpaceClassConstructor
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8

/**
 * An object that deals with Maps
 * 
 * @version a.5.0
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
	
	val paths:ISeq[URL] = {
		ISeq.empty ++ new ResourcesServiceLoader(SERVICE);
	}
	
	private def getMetadata(index:Int):Map[String, Any] = {
		val metadataPath = Maps.paths(index)
		val metadataMap:Map[String,Any] = {
			val reader = new java.io.InputStreamReader(metadataPath.openStream(), UTF_8)
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
			val path = new URL(metadataPath, metadataMap("classMap").toString)
			val reader = new java.io.InputStreamReader(path.openStream(), UTF_8)
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
		
		val layoutPath = new URL(metadataPath, metadataMap("layout").toString)
		val layoutReader = new java.io.InputStreamReader(layoutPath.openStream(), UTF_8)
		val layoutTable:ISeq[ISeq[SpaceClassConstructor]] = {
			val reader = new CSVReader(layoutReader);
			val letterTable3 = reader.readAll();
			val letterTable = ISeq.empty ++ letterTable3.map{ISeq.empty ++ _}
			
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
				val startSpacePath = new URL(metadataPath, startSpaceValue.toString)
				val startSpaceReader = new java.io.InputStreamReader(startSpacePath.openStream(), UTF_8)
				
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
				val startSpacePath = new URL(metadataPath, startSpaceValue.toString)
				val startSpaceReader = new java.io.InputStreamReader(startSpacePath.openStream(), UTF_8)
				
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
