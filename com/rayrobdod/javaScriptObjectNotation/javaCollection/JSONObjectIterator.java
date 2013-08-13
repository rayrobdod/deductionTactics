package com.rayrobdod.javaScriptObjectNotation.javaCollection;

import java.text.ParseException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.JSONObjectValidator;
import com.rayrobdod.javaScriptObjectNotation.javaCollection.parseListeners.NextJSONObjectParseListener;
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParseAdapter;
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser;
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.SizeJSONParseListener;
import com.rayrobdod.javaScriptObjectNotation.JSONString;
import static com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONObject.encode;
import static com.rayrobdod.javaScriptObjectNotation.parser.decoders.ToJavaObjectJSONDecoder.decode;

/**
 * An Iterator that iterators through JSONObjects.
 * 
 * @author Raymond Dodge
 * @version 15 Jan 2012 - extracting from JSONObject
 * @version 03 Jul 2012 - rewriting hasNext from a one liner into something without
		a NullPointerException. An unavoidable NullPointerException if the method is used
 */
public class JSONObjectIterator implements Iterator<Entry<JSONString, Object>>
{
	private JSONObjectEntry prevEntry;
	private final String unparsed;
	
	
	public JSONObjectIterator(String unparsed)
	{
		this.prevEntry = null;
		this.unparsed = unparsed;
		
		if (unparsed == null)
			throw new NullPointerException("Parameter unparsed was null");
	}
	
	public JSONObjectEntry next()
	{
		if (!this.hasNext()) throw new NoSuchElementException(); 
		
		//disassociating each JSONObjectEntry's endComma value; #toArray() is broken otherwise
		int startAt = (prevEntry == null ? unparsed.indexOf('{') : prevEntry.getEndCommaIndex());
		
		NextJSONObjectParseListener l = new NextJSONObjectParseListener();
		
		try
		{
			JSONParser.parse(l, unparsed, startAt);
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	
		prevEntry = l.getEntry(unparsed);
		return prevEntry;
	}
	
	public boolean hasNext()
	{
		int leftIndex = (prevEntry == null ? unparsed.indexOf('{') : prevEntry.getEndCommaIndex());
		int rightIndex = unparsed.lastIndexOf('}');
		
		boolean endCommaIsComma = unparsed.charAt(leftIndex) != '}';
		boolean whitespaceRemains = unparsed.substring(leftIndex, rightIndex).matches("[\\s]*");
		
		return endCommaIsComma && !whitespaceRemains;
	}
	
	public void remove() throws IllegalStateException
	{
		prevEntry.remove();
	}
	
}
