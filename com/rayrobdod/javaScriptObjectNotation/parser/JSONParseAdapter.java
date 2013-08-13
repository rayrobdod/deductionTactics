package com.rayrobdod.javaScriptObjectNotation.parser;

import java.text.ParseException;

/**
 * This is an Adapter for JSONParseListener. All methods do nothing, except abort which
 * always returns false.
 * 
 * @author Raymond Dodge
 * @version Sep 9, 2010
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.javaScriptObjectNotation} to {@code com.rayrobdod.javaScriptObjectNotation;}
 */
public abstract class JSONParseAdapter implements JSONParseListener
{
	/**
	 * returns false
	 * @return false
	 * @throws IllegalStateException never 
	 */
	public boolean abort() throws IllegalStateException
	{
		return false;
	}
	
	/**
	 * does nothing
	 * @param index the index of the read char
	 * @param character the character read
	 * @throws IllegalStateException if a character should not be read
	 */
	public void charRead(int index, char character)
			throws IllegalStateException {}
	
	/**
	 * does nothing
	 * @param commaIndex the index of the ended element
	 * @param character the char that ended the element
	 * @throws IllegalStateException if a character should not be read
	 * @throws ParseException if the element is illegal in some way
	 */
	public void elemEnded(int commaIndex, char character)
			throws IllegalStateException, ParseException {}
	
	/**
	 * does nothing
	 * @param commaIndex the index of the ended element
	 * @param character the char that started the element
	 * @throws IllegalStateException if a character should not be read
	 */
	public void elemStarted(int commaIndex, char character)
			throws IllegalStateException {}
	
	/**
	 * does nothing
	 * @throws IllegalStateException if this should not end
	 * @throws ParseException if anything is amis
	 */
	public void ended() throws IllegalStateException, ParseException {}
	
	/**
	 * does nothing
	 * @param index the index of the ended bracket
	 * @param character the char of the ended bracket
	 * @throws IllegalStateException if a character should not be read
	 * @throws ParseException if the element is illegal in some way
	 */
	public void endingBracket(int index, char character)
			throws IllegalStateException, ParseException {}
	
	/**
	 * does nothing
	 * @param colonIndex the index of the colon indicating the separation
	 * @param character the character indicating the separation
	 * @throws IllegalStateException if this cannot accept such an event
	 * @throws ParseException if there is no way a key-value separation should occur here
	 * @throws ClassCastException if the key is not a JSONString, or cannot be otherwise parsed
	 */
	public void keyValueSeparation(int colonIndex, char character)
			throws IllegalStateException, ParseException, ClassCastException {}
	
	/**
	 * does nothing
	 * @param index the index of the opened bracket
	 * @param character the char that opened the bracket
	 * @throws IllegalStateException if a character should not be read
	 * @throws ParseException if the element is illegal in some way
	 */
	public void openingBracket(int index, char character)
			throws IllegalStateException, ParseException {}
	
	/**
	 * does nothing
	 * @throws IllegalStateException if this should not be starting
	 */
	public void started() throws IllegalStateException {}
	
}
