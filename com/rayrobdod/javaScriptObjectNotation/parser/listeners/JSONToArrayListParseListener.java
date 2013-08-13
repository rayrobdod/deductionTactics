package com.rayrobdod.javaScriptObjectNotation.parser.listeners;

import com.rayrobdod.javaScriptObjectNotation.parser.JSONParseListener;
import java.text.ParseException;
import java.util.ArrayList;
import com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONArray;
import com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONObject;

/**
 * This when run through the JSONParser will result in an {@link ArrayList}.
 * This is preferable to <code>new ArrayList(new JSONArray(string))</code>
 * if a {@link java.io.Reader Reader} has to be used in the parser, for example,
 * due to a huge file that would cause an {@link OutOfMemoryError} if 
 * attempted to turned into a String. Or, just because the {@link JSONArray} is
 * just unwanted.
 * 
 * @author Raymond Dodge
 * @version Sep 9, 2010
 * @version Nov 8, 2010 - changed from extending {@link JSONParseAdapter}
			to implementing {@link JSONParseListener}
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.javaScriptObjectNotation} to {@code com.rayrobdod.javaScriptObjectNotation;}
 * @version 15 Jan 2011 - moved from {@code com.rayrobdod.javaScriptObjectNotation.parser}
		to {@code com.rayrobdod.javaScriptObjectNotation.parser.listeners}
 * @see ArrayList
 */
// TODO test cases
public class JSONToArrayListParseListener implements JSONParseListener
{
	private ArrayList<Object> list;
	private StringBuilder builder;
	
	/**
	 * returns false.
	 * @return false. Always.
	 */
	public boolean abort()
	{
		return false;
	}
	
	/**
	 * Reads the given character
	 * 
	 * @param index the index of he character that was read
	 * @param character the character to read
	 * @throws IllegalStateException if the parser is in an inappropriate place to red characters
	 * 
	 */
	public void charRead(int index, char character)
			throws IllegalStateException
	{
		if (list == null) throw new IllegalStateException("Has not started parsing");
		if (builder == null) throw new IllegalStateException("Is not in an element");
		
		builder.append(character);
	}
	
	public void elemStarted(int commaIndex, char character)
			throws IllegalStateException
	{
		if (list == null) throw new IllegalStateException("Has not started parsing");
		
		builder = new StringBuilder();
	}
	
	public void elemEnded(int commaIndex, char character)
			throws IllegalStateException, ParseException
	{
		if (list == null) throw new IllegalStateException("Has not started parsing");
		
		try
		{
			//TRYTHIS find better deep parsing
			list.add(JSONObject.decode(builder.toString()));
		}
		catch (ClassCastException e)
		{
			ParseException e1 = new ParseException("Element could not be decoded", commaIndex);
			e1.initCause(e);
			throw e1;
		}
		
		builder = null;
	}
	
	public void openingBracket(int index, char character)
			throws IllegalStateException, ParseException
	{
		if (list == null) throw new IllegalStateException("Has not started parsing");
		if (character != '[') throw new ParseException("Invalid array opening:" +
				" expected '['; was '" + character + "'", index);
	}
	
	public void endingBracket(int index, char character)
			throws IllegalStateException, ParseException
	{
		if (list == null) throw new IllegalStateException("Has not started parsing");
		if (character != ']') throw new ParseException("Invalid array ending:" +
				" expected ']'; was '" + character + "'", index);
	}
	
	public void keyValueSeparation(int colonIndex, char character)
			throws IllegalStateException, ParseException
	{
		throw new ParseException("Array cannot have key-value pair", colonIndex);
	}
	
	public void started() throws IllegalStateException
	{
		list = new ArrayList<Object>();
	}
	
	public void ended() throws IllegalStateException, ParseException
	{
	}
	
	/**
	 * returns the generated ArrayList
	 * @return the generated ArrayList
	 */
	public ArrayList<Object> getArrayList()
	{
		return list;
	}
}
