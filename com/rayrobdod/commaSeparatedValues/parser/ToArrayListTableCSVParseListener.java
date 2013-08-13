package com.rayrobdod.commaSeparatedValues.parser;

import net.verizon.rayrobdod.util.collection.ArrayListTable;

/**
 * A CSVParseListener that will directly turn a CSV winto an {@link ArrayListTable}.
 * This may be benificial if the file to read is too large to store in memory, if the
 * data will end up as an ArrayListTable anyway, or if one dislikes the CSVTable.
 * 
 * @author Raymond Dodge
 * @version Nov 13, 2010
 * @version Nov 22, 2010 - fix: makes table non-empty before reading to it now.
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 */
public class ToArrayListTableCSVParseListener implements CSVParseListener
{
	private ArrayListTable<String> table;
	private StringBuilder field;
	
	public boolean abort()
	{
		return false;
	}
	
	public void ended(int index) 
	{
		field = null;
	}
	
	public void newField(int index)
	{
		table.add(field.toString());
		field = new StringBuilder();
	}
	
	public void newRecord(int index)
	{
		table.add(field.toString());
		field = new StringBuilder();
		
		table.addRecord();
	}
	
	public void readCharacter(char character)
	{
		field.append(character);
	}
	
	public void started(int index)
	{
		table = new ArrayListTable<String>();
		field = new StringBuilder();
		
		table.addRecord();
	}
	
	/**
	 * returns the generated table
	 * @return the generated table
	 */
	public ArrayListTable<String> getTable()
	{
		return table;
	}
}
