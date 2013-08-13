package com.rayrobdod.commaSeparatedValues.parser;

/**
 * This is to be implemented by a class that wishes to receve events during a parsing
 * with a {@link CSVParser}.
 * @author Raymond Dodge
 * @version Jan 29, 2010
 * @version Nov 11, 2010 - extracted from {@link com.rayrobdod.commaSeparatedValues.CSVTable} and made public
 * @version Nov 13, 2010 - added int parameter to {@link #ended(int)} and {@link #started(int)}
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 * @see CSVParser
 */
public interface CSVParseListener
{
	/**
	 * called when a character is read
	 * @param character the read character
	 */
	void readCharacter(char character);
	/**
	 * called when the division between a field and a record is found
	 * @param index the index in the string of the comma just before the field
	 */
	void newField(int index);
	/**
	 * called when the devision between records is found
	 * @param index the index in the string of the record-starting line-feed or return-feed
	 */
	void newRecord(int index);
	/**
	 * if this returns true, the parsing should end
	 * @return whether the parsing should end 
	 */
	boolean abort();
	/**
	 * called when the parsing ends
	 * @param index the index where teh parsing starts 
	 */
	void ended(int index);
	/**
	 * called when the parsing starts 
	 * @param index the index where teh parsing starts
	 */
	void started(int index);
}