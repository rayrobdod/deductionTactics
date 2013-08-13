package com.rayrobdod.commaSeparatedValues.parser;

/**
 * A CSV Parse listener that determines the bounds of a certian record
 * 
 * @author Raymond Dodge
 * @version Nov 12, 2010 - cloned then modified form {@link EntryBoundsCSVParseListener}
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 */
public class RecordBoundsCSVParseListener implements CSVParseListener
{
	private final int nullValue = -2;
	
	private int currentRecord;
	private int recordStartIndex;
	private int recordEndIndex;
	private int record;
	
	/**
	 * Creates a RecordBoundsListener that listens for the specified record
	 * @param record the record to listen for
	 */
	public RecordBoundsCSVParseListener(int record)
	{
		currentRecord = 0;
		recordStartIndex = nullValue;
		recordEndIndex = nullValue;
		this.record = record;
	}
	
	public void newField(int index){}

	public void newRecord(int index)
	{
		currentRecord++;
		
		if (currentRecord == record)
		{
			recordStartIndex = index;
		}
		else if (recordStartIndex != nullValue && recordEndIndex == nullValue)
		{
			recordEndIndex = index;
		}
	}

	public void readCharacter(char character)
	{
		
	}
	
	public boolean abort()
	{
		// desired record was passed
		return (recordEndIndex != nullValue);
	}
	
	public void ended(int index)
	{
		newRecord(index);
	}
	
	public void started(int index)
	{
		if (record == 0)
		{
			recordStartIndex = 0;
		}
		else
		{
			recordStartIndex = nullValue;
		}
		
		recordEndIndex = nullValue;
	}
	
	/**
	 * returns the start index of the record
	 * @return the record's start index
	 */
	public int getStartIndex()
	{
		return recordStartIndex;
	}
	
	/**
	 * returns the end index of the record
	 * @return the record's end index
	 */
	public int getEndIndex()
	{
		return recordEndIndex;
	}
}
