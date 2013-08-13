package com.rayrobdod.javaScriptObjectNotation.parser.listeners;

import com.rayrobdod.javaScriptObjectNotation.parser.JSONParseListener;
import java.text.ParseException;
import com.rayrobdod.javaScriptObjectNotation.javaCollection.JSONObject;

/**
 * A JSONParseListener that finds an item in an array, and extracts the value
 * 
 * @author Raymond Dodge
 * @version Sep 9, 2010
 * @version Oct 14, 2010 - now implements JSONParseListener instead of extending adapter
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.javaScriptObjectNotation} to {@code com.rayrobdod.javaScriptObjectNotation;}
 * @version 15 Jan 2011 - moved from {@code com.rayrobdod.javaScriptObjectNotation.parser}
		to {@code com.rayrobdod.javaScriptObjectNotation.parser.listeners}
 */
public class GetJSONArrayParseListener implements JSONParseListener
{
	private final int getIndex;
	private int currentIndex;
	
	private StringBuilder builder;
	
	/**
	 * Constructs a GetJSONArrayParseListener that finds the specified index
	 * @param index the index to look for
	 */
	public GetJSONArrayParseListener(int index) 
	{
		getIndex = index;
		currentIndex = Integer.MIN_VALUE;
	}
	
	public boolean abort() throws IllegalStateException
	{
		return (currentIndex > getIndex);
	}
	
	public void charRead(int index, char character)
			throws IllegalStateException
	{
		if (currentIndex == getIndex) builder.append(character);
	}
	
	public void elemStarted(int commaIndex, char character)
			throws IllegalStateException
	{
		currentIndex++;
	}
	
	public void keyValueSeparation(int colonIndex, char character)
			throws IllegalStateException, ParseException, ClassCastException
	{
		throw new ParseException("Array cannot have key-value pair", colonIndex);
	}
	
	public void started() throws IllegalStateException
	{
		builder = new StringBuilder();
		currentIndex = -1;
	}
	
	public void ended() throws IllegalStateException, ParseException
	{
		currentIndex++;
		if (currentIndex <= getIndex) 
		{
			ParseException e = new ParseException("Could not find specified element", 0);
			e.initCause(new java.lang.IndexOutOfBoundsException("Tried to get element " + getIndex
					+ "of a " + currentIndex + "size list"));
			throw e;
		}
	}
	
	/**
	 * this returns the element gotten in its unparsed form
	 * @return a string representing the object
	 * @throws IllegalStateException if the parsing is not ended
	 */
	public String getUnparsedElement() throws IllegalStateException
	{
		if (currentIndex <= getIndex) throw new IllegalStateException(
				"Parsing has not ended yet!");
		return builder.toString();
	}
	
	/**
	 * this returns the element gotten in its parsed form
	 * @return an object
	 * @throws IllegalStateException  if the parsing is not ended
	 * @throws ClassCastException if the string does not represent a valid object.
	 */
	public Object getParsedElement() throws IllegalStateException, ClassCastException
	{
		return JSONObject.decode(this.getUnparsedElement());
	}
	
	/** does nothing 
	 * @param commaIndex no effect
	 * @param character no effect
	 */
	public void elemEnded(int commaIndex, char character) {}
	
	/** does nothing 
	 * @param index no effect
	 * @param character no effect
	 */
	public void endingBracket(int index, char character) {}
	
	/** does nothing 
	 * @param index no effect
	 * @param character no effect
	 */
	public void openingBracket(int index, char character) {}
}
