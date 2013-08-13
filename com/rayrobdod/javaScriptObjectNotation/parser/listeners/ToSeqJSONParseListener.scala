package com.rayrobdod.javaScriptObjectNotation.parser.listeners;

import com.rayrobdod.javaScriptObjectNotation.parser.{JSONParseListener, JSONDecoder};
import scala.collection.immutable.Vector
import scala.collection.mutable.StringBuilder
import com.rayrobdod.javaScriptObjectNotation.JSONString
import java.text.ParseException
import com.rayrobdod.javaScriptObjectNotation.parser.decoders.{
		ToScalaCollectionJSONDecoder => ToScalaCollection}

/**
 * Parses a JSON entity into a Sequence. If the parsed entity was an object,
 * the result can be turned into a Map with Seq.toMap
 * 
 * This does not care a lot about errors in the JSON source - it will create
 * a Pair and put that in an array if it happens to find a colon in a JSONArray
 * for example - but if there is nested arrays, the decoder may have a problem
 * with invalid JSONObjects - in fact the default one does.
 * 
 * @param decoder - the object used to turn unparsed JSON objects into an appropriate class. 
 * @author Raymond Dodge
 * @version 03 Aug 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.javaScriptObjectNotation.parser} to {@code com.rayrobdod.javaScriptObjectNotation.parser}
 * @version 15 Jan 2011 - moved from {@code com.rayrobdod.javaScriptObjectNotation.parser}
		to {@code com.rayrobdod.javaScriptObjectNotation.parser.listeners}
 * @version 16 Jan 2011 - modifying to use a JSONDecoder, now that they exist.
 */
class ToSeqJSONParseListener(val decoder:JSONDecoder)
		extends JSONParseListener
{
	def this() = this(ToScalaCollection)
	
	private var vector:Vector[Any] = Vector.empty
	private var builder:StringBuilder = StringBuilder.newBuilder
	private var key:Option[String] = None
	
	override def abort = false
	override def charRead(index:Int, charact:Char) = builder += charact
	override def started() {vector = Vector.empty}
	override def ended() {}
	override def elemStarted(index:Int, charact:Char) {}
	override def openingBracket(index:Int, charact:Char) {}
	override def endingBracket(index:Int, charact:Char) {}
	
	override def elemEnded(index:Int, charact:Char) =
	{
		if (!builder.toString.trim.isEmpty)
		{
			try
			{
				val value = decoder.decode(builder.toString())
				
				vector = vector :+ (key match {
					case Some(x) => (x, value)
					case None => value
				})
			}
			catch
			{
				case e:ClassCastException => 
				{
					val e1 = new ParseException("Element could not be decoded: " + builder.toString.trim, index);
					e1.initCause(e);
					throw e1;
				}
			}
		}
		
		builder = StringBuilder.newBuilder
		key = None
	}
	
	override def keyValueSeparation(index:Int, charact:Char) =
	{
		key = Some(JSONString.generateUnparsed(builder.toString).toString)
		builder = StringBuilder.newBuilder
	}
	
	/** Returns the parsed result. May include Pairs. */
	def result:Seq[Any] = vector
	
	/**
	 * Returns the parsed result converted to a Map.
	 * Most non-pair values are destroyed in the transition. 
	 */
	def resultMap:Map[String,Any] = {
		result.map{_ match {
				case x:Tuple2[_,_] => x
				case x:Any => ("",x)
			}
		}.toMap.map{pair:(Any, Any) => (pair._1.toString, pair._2)}
	}
}
