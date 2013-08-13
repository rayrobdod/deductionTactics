package com.rayrobdod.deductionTactics.meta

import java.nio.file.FileSystems.{getDefault => defaultFileSystem, newFileSystem}
import scala.collection.JavaConversions.{iterableAsScalaIterable, mapAsJavaMap}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.deductionTactics.{CannonicalTokenClass,
		CannonicalTokenClassDecoder, Weaponkinds}
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
	
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.{
		JSONParser, JSONDecoder, JSONParseListener}
import com.rayrobdod.javaScriptObjectNotation.JSONString
import com.rayrobdod.binaryJSON.BSONWriter
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.nio.file.{Path, Files}

/**
 * Reads a set of JSON-encoded token class files and creates a map from
 * class anems to image resources
 * 
 * @author Raymond Dodge
 * @version 2013 Aug 05
 */
class TokenClassNameToImageLocation(sources:Seq[Path]) {
	
	
	val map:Map[String, String] = {
		val a:Seq[Seq[(String, Option[String])]] = sources.map{(jsonPath:Path) =>
			val jsonReader = Files.newBufferedReader(jsonPath, UTF_8)
			
			val l = new ToScalaCollection(this.Decoder)
			JSONParser.parse(l, jsonReader)
			jsonReader.close()
			l.resultSeq
		}
		val b:Seq[(String, Option[String])] = a.flatten
		val c:Seq[(String, String)] = b.filter{_._2.isDefined}.map{(a) => (( a._1, a._2.get ))}
		val d:Map[String, String] = c.toMap
		d
	}
	
	
	
	private object Decoder extends JSONDecoder[(String, Option[String])] {
		override def decode(s:String):(String, Option[String]) = {
			val l = new TokenClassNameToImageLocation.this.ParseListener
			JSONParser.parse(l, s)
			l.result
		}
	}
	
	private class ParseListener extends JSONParseListener {
		private var strBuilder = StringBuilder.newBuilder
		private var key:Option[String] = None
		private var name:Option[String] = None
		private var iconLoc:Option[String] = None
		
		override def abort = false
		override def charRead(index:Int, charact:Char) = strBuilder += charact
		override def started() {name = None; iconLoc = None; key = None; strBuilder = StringBuilder.newBuilder}
		override def ended() {}
		override def elemStarted(index:Int, charact:Char) {}
		override def openingBracket(index:Int, charact:Char) {}
		override def endingBracket(index:Int, charact:Char) {}
		
		override def elemEnded(index:Int, charact:Char) =
		{
			val valueRaw = strBuilder.toString
			val value = try {
				JSONString.generateUnparsed(valueRaw).toString
			} catch {
				case x:java.text.ParseException => valueRaw
			}
			
			key.foreach{ (x:String) =>
				try {
					x match {
						case "name" => name = Some(value)
						case "icon" => iconLoc = Some(value)
						case x => {  }
					}
				} catch {
					case e:java.util.NoSuchElementException => {throw e}
				}
			}
			strBuilder = StringBuilder.newBuilder
		}
	
		override def keyValueSeparation(index:Int, charact:Char) =
		{
				key = Some(JSONString.generateUnparsed(strBuilder.toString).toString)
				strBuilder = StringBuilder.newBuilder;
		}
	
		def result:(String, Option[String]) = (( name.get, iconLoc ))
	}
}
