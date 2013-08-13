package com.rayrobdod.commaSeparatedValues.parser;


/**
 * A CSVParseListener for getting a certain value
 * 
 * @author Raymond Dodge
 * @version Nov 11, 2010 - extracted from {@link com.rayrobdod.commaSeparatedValues.CSVTable}
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 */
public class GetCSVParseListener implements CSVParseListener
{
	private int fieldToGet;
	private int recordToGet;
	
	private int currentField;
	private int currentRecord;
	
	private StringBuilder gottenValue;
	
	/**
	 * Creates a CSVParseLsitener that will get the value at the specified recrod and field
	 * 
	 * @param record the record to get from
	 * @param field the field to get from
	 */
	public GetCSVParseListener(int record, int field)
	{
		this.fieldToGet = field;
		this.recordToGet = record;
	}
	
	public void newField(int index)
	{
		currentField++;
	}

	public void newRecord(int index)
	{
		currentRecord++;
		currentField = 0;
	}

	public void readCharacter(char character)
	{
		if (recordToGet == currentRecord && fieldToGet == currentField)
		{
			gottenValue.append(character);
		}
	}
	
	public boolean abort()
	{
		// desired record was passed
		return ((recordToGet < currentRecord) || (recordToGet == currentRecord
				&& fieldToGet < currentField));
	}
	
	public void ended(int index){}
	public void started(int index)
	{
		currentField = 0;
		currentRecord = 0;
		gottenValue = new StringBuilder();
	}
	
	/**
	 * returns the value gotten from the last parsing
	 * @return the value
	 */
	public String getValue()
	{
		return gottenValue.toString();
	}
}
