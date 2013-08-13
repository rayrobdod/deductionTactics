package com.rayrobdod.javaScriptObjectNotation.parser;

import java.text.ParseException;
import com.rayrobdod.javaScriptObjectNotation.JSONString
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.{
		JSONObjectValidator, JSONArrayValidator, ToSeqJSONParseListener}

/**
 * Methods that determine whether this is a valid item of the specified type
 * 
 * @author Raymond Dodge
 * @version 16 Jan 2012
 * @see decoders.isValid
 */
package object decoders
{
	/**
	 * @param c the string to test
	 * @return true iff this string is a JSONEncoded string and an array
	 */
	def isValidJSONArray(c:String) = 
	{
		val l = new JSONArrayValidator()
		try
		{
			JSONParser.parse(l, c)
			true
		}
		catch
		{
			case x:ParseException => false
		}
	}
	
	/**
	 * @param c the string to test
	 * @return true iff this string is a JSONEncoded string and an object
	 */
	def isValidJSONObject(c:String) = 
	{
		val l = new JSONObjectValidator()
		try
		{
			JSONParser.parse(l, c)
			true
		}
		catch
		{
			case x:ParseException => false
		}
	}
}

package decoders
{
	/**
	 * Methods that determine whether this is a valid item of the specified type
	 * </p><p>
	 * Java can't touch anything with the name package, so this exists. There are similar methods in package$, but not here.
	 * 
	 * @author Raymond Dodge
	 * @version 16 Jan 2012
	 */
	object isValid
	{
		def jsonArray(c:String) = isValidJSONArray(c)
		def jsonObject(c:String) = isValidJSONObject(c)
	}
}
