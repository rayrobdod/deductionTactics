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

import java.net.URL
import java.io.{Reader, StringReader, InputStreamReader}
import java.nio.charset.StandardCharsets.UTF_8
import scala.collection.immutable.{Seq, Set}
import scala.collection.JavaConversions.iterableAsScalaIterable
import au.com.bytecode.opencsv.CSVReader;
import com.rayrobdod.util.services.{ResourcesServiceLoader, Services}
import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.json.builder.{Builder, SeqBuilder, MapBuilder}
import com.rayrobdod.boardGame.RectangularField

final case class Arena (
	val name:String,
	layoutStrs:Seq[Seq[String]],
	val startSpaces:Map[Int,Seq[Seq[(Int, Int)]]]
) {
	def layout:Seq[Seq[SpaceClass]] = layoutStrs.map{_.map{x => SpaceClassFactory(x)}}
	
	def possiblePlayers:Set[Int] = startSpaces.keySet
}

final class ArenaBuilder(baseDir:URL) extends Builder[Arena] {
	override val init:Arena = new Arena("", Nil, Map.empty)
	override def apply(folding:Arena, key:String, value:Any):Arena = key match {
		case "name" => folding.copy(name = value.toString)
		case "layout" => {
			val layoutPath:URL = new URL(baseDir, value.toString)
			var layoutReader:Reader = new StringReader("")
			val result = try {
				layoutReader = new java.io.InputStreamReader(layoutPath.openStream(), UTF_8)
				
				val layoutParser = new CSVReader(layoutReader);
				val letterTable3 = layoutParser.readAll();
				Seq.empty ++ letterTable3.map{Seq.empty ++ _}
			} finally {
				layoutReader.close()
			}
			folding.copy(layoutStrs = result)
		}
		case "classMap" => folding
		case "deductionTactics::startSpaces" => {
			folding.copy(startSpaces = value match {
				case a:String => {
					val spacePath = new URL(baseDir, value.toString)
					var spaceReader:Reader = new StringReader("{}")
					try {
						spaceReader = new java.io.InputStreamReader(spacePath.openStream(), UTF_8)
						new JsonParser(childBuilder(key)).parse(spaceReader).asInstanceOf[Map[_,_]].map{x => ((x._1.toString.toInt, castToStartSpaces(x._2)))}
					} finally {
						spaceReader.close()
					}
				}
				case a:Map[_,_] => {
					a.map{x => ((x._1.toString.toInt, castToStartSpaces(x._2)))}
				}
			})
		}
		case _ => folding
	}
	override def childBuilder(key:String):Builder[_] = key match {
		case "deductionTactics::startSpaces" => new MapBuilder({x:String => new SeqBuilder})
	}
	override val resultType:Class[Arena] = classOf[Arena]
	
	private def castToStartSpaces(x:Any):Seq[Seq[Tuple2[Int,Int]]] = {
		def tuple(x:Any):Tuple2[Int,Int] = x match {case Seq(a:Long,b:Long) => Tuple2(a.intValue, b.intValue)}
		def seqTuple(x:Any):Seq[(Int,Int)] = x match {case x:Seq[_] => x.map{tuple _}}
		def seqSeqTuple(x:Any):Seq[Seq[(Int,Int)]] = x match {case x:Seq[_] => x.map{seqTuple _}}
		
		seqSeqTuple(x)
	}
}

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
	
	@deprecated("getArenas(index).name", "a.6.0")
	val names:Seq[String] = {
		Seq.empty ++ Services.readServices(SERVICE);
	}
	
	@deprecated("why is this even a thing?", "a.6.0")
	val paths:Seq[URL] = {
		Seq.empty ++ new ResourcesServiceLoader(SERVICE);
	}
	
	def arenas:Seq[Arena] = paths.map{x =>
		var reader:Reader = new StringReader("{}")
		try {
			reader = new java.io.InputStreamReader(x.openStream(), UTF_8)
			new JsonParser(new ArenaBuilder(x)).parse(reader)
		} finally {
			reader.close()
		}
	}
	
	@deprecated("getArenas(index).layout", "a.6.0")
	def getMap(index:Int):RectangularField[SpaceClass] = {
		RectangularField(arenas(index).layout)
	}
	
	@deprecated("getArenas(index).possiblePlayers", "a.6.0")
	def possiblePlayers(index:Int):Set[Int] = {
		arenas(index).possiblePlayers
	}
	
	@deprecated("getArenas(index).startSpaces(numPlayers)", "a.6.0")
	def startingPositions(index:Int, numPlayers:Int):Seq[Seq[(Int, Int)]] = {
		arenas(index).startSpaces(numPlayers)
	}
}
