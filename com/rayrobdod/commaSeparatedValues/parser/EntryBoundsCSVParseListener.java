package com.rayrobdod.commaSeparatedValues.parser;

/**
 * A CSV Parse listener that determines the bounds of a certian field
 * 
 * @author Raymond Dodge
 * @version Nov 12, 2010 - extracted from {@link com.rayrobdod.commaSeparatedValues.CSVTable}
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 */
public class EntryBoundsCSVParseListener implements CSVParseListener
{
	private final int nullValue = -2;
	
	private int currentField;
	private int currentRecord;
	private int fieldStartIndex;
	private int fieldEndIndex;
	private int record;
	private int field;
	
	/**
	 * Creates an EntryboundsListener that listens for the sepified record
	 * @param record the record to listen for
	 * @param field the field in the record to listen for
	 */
	public EntryBoundsCSVParseListener(int record, int field)
	{
		currentField = 0;
		currentRecord = 0;
		fieldStartIndex = nullValue;
		fieldEndIndex = nullValue;
		this.field = field;
		this.record = record;
	}
	
	public void newField(int index)
	{
		currentField++;
		
		if (currentRecord == record && currentField == field)
		{
			fieldStartIndex = index + 1;
		}
		else if (fieldStartIndex != nullValue && fieldEndIndex == nullValue)
		{
			fieldEndIndex = index;
		}
	}

	public void newRecord(int index)
	{
		currentRecord++;
		currentField = 0;
		
		if (fieldStartIndex != nullValue && fieldEndIndex == nullValue)
		{
			fieldEndIndex = index;
		}
	}

	public void readCharacter(char character)
	{
		
	}
	
	public boolean abort()
	{
		// desired record was passed
		return (fieldEndIndex != nullValue);
	}
	
	public void ended(int index)
	{
		this.newRecord(index);
	}
	
	public void started(int index)
	{
		if (record == 0 && field == 0)
		{
			fieldStartIndex = 0;
		}
		else
		{
			fieldStartIndex = nullValue;
		}
		
		fieldEndIndex = nullValue;
	}
	
	/**
	 * returns the end index of the field
	 * @return the field's end index
	 */
	public int getStartIndex()
	{
		return fieldStartIndex;
	}
	
	/**
	 * returns the end index of the field
	 * @return the field's end index
	 */
	public int getEndIndex()
	{
		return fieldEndIndex;
	}
}
