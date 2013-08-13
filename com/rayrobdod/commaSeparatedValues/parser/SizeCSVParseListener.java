package com.rayrobdod.commaSeparatedValues.parser;

import java.util.LinkedList;
import java.util.List;

/**
 * A CSVParseListener that determines the size of a CSV file
 * @author Raymond Dodge
 * @version Nov 11, 2010 - extracted from {@link com.rayrobdod.commaSeparatedValues.CSVTable}
 * @version Nov 13, 2010 - was reporting record lengths off by one; now fixed.
 * @version Nov 13, 2010 - now considers an empty record to have a size of 0.
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 */
public class SizeCSVParseListener implements CSVParseListener
{
	private List<Integer> detailedSize;
	/** the index of the current field */
	private int currentField;
	private boolean recordContainsCharacters;
	
	public void newField(int index)
	{
		currentField++;
	}

	public void newRecord(int index)
	{
		if (recordContainsCharacters)
		{
			// 0-indexed systems, size is one more than the current field
			detailedSize.add(currentField + 1);
		}
		else
		{
			detailedSize.add(0);
		}
		
		currentField = 0;
		recordContainsCharacters = false;
	}

	public void readCharacter(char character){recordContainsCharacters = true;}
	
	public boolean abort()
	{
		return false;
	}
	
	public void ended(int index)
	{
		this.newRecord(-1);
	}

	public void started(int index)
	{
		detailedSize = new LinkedList<Integer>();
		currentField = 0;
		recordContainsCharacters = false;
	}
	
	/**
	 * returns the computed size
	 * @return the size of the CSVFile
	 */
	public List<Integer> getSize()
	{
		return detailedSize;
	}
}