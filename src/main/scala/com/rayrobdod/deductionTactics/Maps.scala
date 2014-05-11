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

import scala.collection.immutable.{Seq, Set}
import scala.collection.JavaConversions.iterableAsScalaIterable
import com.rayrobdod.util.services.{ResourcesServiceLoader, Services}
import au.com.bytecode.opencsv.CSVReader;
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.boardGame.RectangularField
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
object Maps {
	private val SERVICE = "com.rayrobdod.deductionTactics.Maps"
	
	val names:Seq[String] = {
		Seq.empty ++ Services.readServices(SERVICE);
	}
	
	val paths:Seq[URL] = {
		Seq.empty ++ new ResourcesServiceLoader(SERVICE);
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
	
	private def getStringLayout(index:Int):Seq[Seq[String]] = {
		val metadataPath = Maps.paths(index)
		val metadataMap = getMetadata(index)
		
		val layoutPath = new URL(metadataPath, metadataMap("layout").toString)
		val layoutReader = new java.io.InputStreamReader(layoutPath.openStream(), UTF_8)
		val layoutTable:Seq[Seq[String]] = {
			val reader = new CSVReader(layoutReader);
			val letterTable3 = reader.readAll();
			val letterTable = Seq.empty ++ letterTable3.map{Seq.empty ++ _}
			
			letterTable
		}
		layoutReader.close()
		
		layoutTable
	}
	
	private def getSpaceClassLayout(index:Int):Seq[Seq[SpaceClass]] = {
		val strings = getStringLayout(index)
		
		strings.map{_.map{(s) => SpaceClassFactory(s)}}
	}
	
	def getMap(index:Int):RectangularField[SpaceClass] = {
		RectangularField(getSpaceClassLayout(index))
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
	
	def startingPositions(index:Int, numPlayers:Int):Seq[Seq[(Int, Int)]] = {
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
		
		val startSpaceMap:Map[String,Seq[Seq[(Int, Int)]]] = {
			startSpaceMapRaw.mapValues{_ match {
				case x:Seq[_] => x.map{_ match {
					case y:Seq[_] => y.map{_ match {
						case Seq(i:Any, j:Any) => {
							((asInt(i), asInt(j)))
						}
					}}
				}}
			}}
		}
		startSpaceMap(numPlayers.toString)
	}
}
