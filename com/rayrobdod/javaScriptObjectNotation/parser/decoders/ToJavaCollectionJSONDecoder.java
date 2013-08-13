package com.rayrobdod.javaScriptObjectNotation.parser.decoders;

import com.rayrobdod.javaScriptObjectNotation.parser.decoders.isValid;
import com.rayrobdod.javaScriptObjectNotation.parser.JSONDecoder;
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser;
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.JSONToArrayListParseListener;
import com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONObject;
import com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONArray;
import com.rayrobdod.javaScriptObjectNotation.JSONString;
import java.text.ParseException;

/**
 * This takes a String that is properly JSONEncoded and turns it into a Java Collections object
 * @author Raymond Dodge
 * @version 16 Jan 2012
 */
public class ToJavaCollectionJSONDecoder
{
	/**
	 * This returns a valid decoding of a CharSequence into a Java Object
	 * @param c the CharSequence to decode
	 * @return a HashMap, ArrayList, JSONString, Number, Boolean or null
	 * @throws NullPointerException if c is null
	 * @throws ClassCastException if the string is does not match any associate types
	 */
	public static Object decode(String c) throws NullPointerException, ClassCastException
	{
		if (c == null) throw new NullPointerException();
		
		c = c.trim();
		
		if (c.equalsIgnoreCase("null")) return null;
		if (c.equalsIgnoreCase("true")) return true;
		if (c.equalsIgnoreCase("false")) return false;
		else if (JSONString.isValid(c))
		{
			try
			{
				return JSONString.generateUnparsed(c);
			}
			catch (ParseException e)
			{
				throw new AssertionError(e);
			}
		}
		else if (isValid.jsonArray(c))
		{
			try
			{
				JSONToArrayListParseListener l =
						new JSONToArrayListParseListener();
				JSONParser.parse(l, c);
				return l.getArrayList();
			}
			catch (ParseException e)
			{
				throw new AssertionError(e);
			}
		}
		else if (isValid.jsonObject(c))
		{
			try
			{
				return new JSONObject(c);
			}
			catch (ParseException e)
			{
				throw new AssertionError(e);
			}
		}
		else
		{
			try
			{
				return Long.decode(c.toString());
			}
			catch (NumberFormatException e)
			{
				try
				{
					return Double.parseDouble(c.toString());
				}
				catch (NumberFormatException e1)
				{
					throw new ClassCastException("the object does not fit any of" +
							" the supported types: " + c + ".");
				}
			}
		}
	}
}
