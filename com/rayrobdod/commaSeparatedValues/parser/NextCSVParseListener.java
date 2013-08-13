package com.rayrobdod.commaSeparatedValues.parser;

/**
 * Gets the next entry after the start point
 * <p>
 * TODO junit this
 * 
 * @author Raymond Dodge
 * @version Nov 13, 2010
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 */
public class NextCSVParseListener implements CSVParseListener
{
	private StringBuilder builder;
	private int endIndex;
	
	public boolean abort()
	{
		return endIndex != -1;
	}
	
	public void ended(int index)
	{
		if (endIndex == -1) endIndex = index;
	}

	public void newField(int index)
	{
		endIndex = index;
	}
	
	public void newRecord(int index)
	{
		endIndex = index;
	}
	
	public void readCharacter(char character)
	{
		builder.append(character);
	}
	
	public void started(int index)
	{
		builder = new StringBuilder();
		endIndex = -1;
	}
	
	/**
	 * returns the string 
	 * @return the string
	 */
	public String getString()
	{
		return builder.toString();
	}
	
	/**
	 * returns the endIndex 
	 * @return the endIndex
	 */
	public int getEndIndex()
	{
		return endIndex;
	}
}
