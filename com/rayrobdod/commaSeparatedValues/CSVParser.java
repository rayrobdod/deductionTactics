package com.rayrobdod.commaSeparatedValues;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a parser for Character Separated Values files.
 * 
 * @author Raymond Dodge
 * @version Jan 5, 2010
 * @version Jan 29, 2010 - added get commands
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues} to {@code com.rayrobdod.commaSeparatedValues}
 * @deprecated use {@link net.verizon.rayrobdod.commaSeparatedValues.parser.CSVParser CSVParser} instead
 * 
 */
public class CSVParser
{
	/**
	 * This parses a properly formatted CSV file from a string into an 
	 * ArrayList of ArrayLists of Strings
	 * 
	 * @param string the string to parse
	 * @return the parsed ArrayList of ArrayLists of Strings. Each string is a field, and each 
	 * 			ArrayList of String is a record, and each ArrayList of ArrayLists is a table. 
	 * @throws IOException if there was a problem reading from the reader
	 */
	public static ArrayList<ArrayList<String>> parse(String string) throws IOException
	{
		StringReader reader = new StringReader(string);
		
		ArrayList<ArrayList<String>> returnValue = parse(reader);
		
		reader.close();
		
		return returnValue;
	}
	
	/**
	 * This parses a properly formatted CSV file into an 
	 * ArrayList of ArrayLists of Strings
	 * 
	 * @param file the CSV file
	 * @return the parsed ArrayList of ArrayLists of Strings. Each string is a field, and each 
	 * 			ArrayList of String is a record, and each ArrayList of ArrayLists is a table. 
	 * @throws IOException if there was a problem reading from the reader
	 */
	public static ArrayList<ArrayList<String>> parse(File file) throws IOException
	{
		FileReader reader = new FileReader(file);
		
		ArrayList<ArrayList<String>> returnValue = parse(reader);
		
		reader.close();
		
		return returnValue;
	}
	
	/**
	 * This parses a string in a reader into a 2DArrayList.
	 * 
	 * @param reader the reader containing the string to parse.
	 * @return the parsed ArrayList of ArrayLists of Strings. Each string is a field, and each 
	 * 			ArrayList of String is a record, and each ArrayList of ArrayLists is a table. 
	 * @throws IOException if there was a problem reading from the reader
	 */
	public static ArrayList<ArrayList<String>> parse(Reader reader) throws IOException
	{
		final int nullChar = 0;
		
		ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentRecord = new ArrayList<String>();
				
		boolean isInsideQuotes = false;
		StringBuilder currentField = new StringBuilder();
		int readCharacter = reader.read();
		int secondCharacter = nullChar;
		while (readCharacter != -1)
		{
			if (isInsideQuotes)
			{
				// in this case, ignore the effects of any characters except quotes.
				if ('"' == (char) readCharacter)
				{
					// check if next char is another quote
					secondCharacter = reader.read();
					
					if ('"' == (char) secondCharacter)
					{
						currentField.append((char) readCharacter);
						secondCharacter = nullChar;
					}
					else
					{
						isInsideQuotes = false;
					}
				}
				else
				{
					currentField.append((char) readCharacter);
				}
			}
			else //(!isInsideQuotes)
			{
				if ('"' == (char) readCharacter)
				{
					isInsideQuotes = true;
				}
				else if (',' == (char) readCharacter)
				{
					currentRecord.add(currentField.toString());
					currentField = new StringBuilder();
				}
				else if (10 == readCharacter) // return feed
				{
					currentRecord.add(currentField.toString());
					currentField = new StringBuilder();
					
					table.add(currentRecord);
					currentRecord = new ArrayList<String>();
				}
				else if (13 == readCharacter) // carage return
				{
					currentRecord.add(currentField.toString());
					currentField = new StringBuilder();
					
					table.add(currentRecord);
					currentRecord = new ArrayList<String>();
					
					// if a return feed character is after this, it should be bundled with this.
					secondCharacter = reader.read();
					
					if (10 == secondCharacter)
					{
						secondCharacter = nullChar;
					}
					else 
					{
						/* 
						 * technically not a part of the specification; but I'll allow
						 * lone carriage return characters to count as a new line
						 */ 
					}
				}
				else
				{
					currentField.append((char) readCharacter);
				}
			}
			
			if (secondCharacter != nullChar)
			{
				readCharacter = secondCharacter;
				secondCharacter = nullChar;
			}
			else
			{
				readCharacter = reader.read();
			}
		}

		currentRecord.add(currentField.toString());
		table.add(currentRecord);
		
		return table;
	}
	
	/**
	 * Reads a particular element of a CVS file.
	 * 
	 * @param string the String containing the cvs file
	 * @param record the record to read
	 * @param field the field of the record to read
	 * @return the element of the cvs file at [record][field]
	 * @throws IOException if an IOException occurs
	 */
	public static String get(String string, int record, int field) throws IOException
	{
		return CSVParser.get(new StringReader(string), record, field);
	}
	
	/**
	 * Reads a particular element of a CVS file.
	 * 
	 * @param file the File handle
	 * @param record the record to read
	 * @param field the field of the record to read
	 * @return the element of the cvs file at [record][field]
	 * @throws FileNotFoundException if the file does not exist
	 * @throws IOException if an IOException occurs
	 */
	public static String get(File file, int record, int field) throws FileNotFoundException, IOException
	{
		return CSVParser.get(new FileReader(file), record, field);
	}
	
	/**
	 * Reads a particular element of a CVS file.
	 * 
	 * @param reader the Reader containing the cvs file
	 * @param record the record to read
	 * @param field the field of the record to read
	 * @return the element of the cvs file at [record][field]
	 * @throws IOException if an IOException occurs
	 */
	public static String get(Reader reader, int record, int field) throws IOException
	{
		final int nullChar = 0;
		
		int currentRecord = 0;
		int currentField = 0;
		boolean isInsideQuotes = false;
		StringBuilder returnValue = new StringBuilder();
		int readCharacter = reader.read();
		int secondCharacter = nullChar;
		while (readCharacter != -1)
		{
			if (isInsideQuotes) // in this case, ignore the effects of any characters except quotes.
			{
				if ('"' == (char) readCharacter)
				{
					// check if next char is another quote
					secondCharacter = reader.read();
					
					if ('"' == (char) secondCharacter)
					{
						if (currentRecord == record && currentField == field)
						{
							returnValue.append((char) readCharacter);
						}
						secondCharacter = nullChar;
					}
					else
					{
						isInsideQuotes = false;
					}
				}
				else
				{
					if (currentRecord == record && currentField == field)
					{
						returnValue.append((char) readCharacter);
					}
				}
			}
			else //(!isInsideQuotes)
			{
				if ('"' == (char) readCharacter)
				{
					isInsideQuotes = true;
				}
				else if (',' == (char) readCharacter)
				{
					if (currentRecord == record)
					{
						currentField++;
						
						if (currentField > field) return returnValue.toString();
					}
				}
				else if (10 == readCharacter) // return feed
				{
					currentRecord++;

					if (currentField > field) throw new IndexOutOfBoundsException(
							"The record does not have that many fields.");
				}
				else if (13 == readCharacter) // carage return
				{
					currentRecord++;

					if (currentField > field) throw new IndexOutOfBoundsException(
							"The record does not have that many fields.");
					
					// if a return feed character is after this, it should be bundled with this.
					secondCharacter = reader.read();
					
					if (10 == secondCharacter)
					{
						secondCharacter = nullChar;
					}
					else 
					{
						/* 
						 * technically not a part of the specification; but I'll allow
						 * lone carriage return characters to count as a new line
						 */ 
					}
				}
				else
				{
					if (currentRecord == record && currentField == field)
					{
						returnValue.append((char) readCharacter);
					}
				}
			}
			
			if (secondCharacter != nullChar)
			{
				readCharacter = secondCharacter;
				secondCharacter = nullChar;
			}
			else
			{
				readCharacter = reader.read();
			}
		}
		
		throw new IndexOutOfBoundsException(
				"The CSV does not have that many records.");
	}
	
	/**
	 * Determines the size of the csv contained in the reader
	 * 
	 * @param string the String the csv is stored in
	 * @return An ArrayList in which the total length is the number of records,
	 * and each elemnt is the size of the respective record 
	 * @throws IOException if an IOException occurs.
	 */
	public static List<Integer> size(String string) throws IOException
	{
		return CSVParser.size(new StringReader(string));
	}
	
	/**
	 * Determines the size of the csv contained in the reader
	 * 
	 * @param file the csv File Handle
	 * @return An ArrayList in which the total length is the number of records,
	 * and each elemnt is the size of the respective record 
	 * @throws FileNotFoundException if the file does not exist
	 * @throws IOException if an IOException occurs.
	 */
	public static List<Integer> size(File file) throws FileNotFoundException, IOException
	{
		return CSVParser.size(new FileReader(file));
	}
	
	/**
	 * Determines the size of the csv contained in the reader
	 * 
	 * @param reader the reader the csv is stored in
	 * @return An ArrayList in which the total length is the number of records,
	 * and each elemnt is the size of the respective record 
	 * @throws IOException if an IOException occurs.
	 */
	public static List<Integer> size(Reader reader) throws IOException
	{
		final int nullChar = 0;
		
		ArrayList<Integer> size = new ArrayList<Integer>();
		int recordSize = 0;
				
		boolean isInsideQuotes = false;
		int readCharacter = reader.read();
		int secondCharacter = nullChar;
		while (readCharacter != -1)
		{
			if (isInsideQuotes)
			{
				// in this case, ignore the effects of any characters except quotes.
				if ('"' == (char) readCharacter)
				{
					// check if next char is another quote
					secondCharacter = reader.read();
					
					if ('"' == (char) secondCharacter)
					{
						secondCharacter = nullChar;
					}
					else
					{
						isInsideQuotes = false;
					}
				}
				else
				{
				}
			}
			else //(!isInsideQuotes)
			{
				if ('"' == (char) readCharacter)
				{
					isInsideQuotes = true;
				}
				else if (',' == (char) readCharacter)
				{
					recordSize++;
				}
				else if (10 == readCharacter) // return feed
				{
					recordSize++;
					size.add(recordSize);
					recordSize = 0;
				}
				else if (13 == readCharacter) // carage return
				{
					recordSize++;
					size.add(recordSize);
					recordSize = 0;
					
					// if a return feed character is after this, it should be bundled with this.
					secondCharacter = reader.read();
					
					if (10 == secondCharacter)
					{
						secondCharacter = nullChar;
					}
					else 
					{
						/* 
						 * technically not a part of the specification; but I'll allow
						 * lone carriage return characters to count as a new line
						 */ 
					}
				}
				else
				{
					//do nothing
				}
			}
		}
		
		return size;
	}
}
