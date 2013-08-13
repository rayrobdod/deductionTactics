package com.rayrobdod.commaSeparatedValues;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import com.rayrobdod.commaSeparatedValues.parser.*;
import com.rayrobdod.commaSeparatedValues.parser.CSVParser;
import com.rayrobdod.util.CloneNotSupportedError;
import net.verizon.rayrobdod.util.collection.AbstractTable;
import net.verizon.rayrobdod.util.collection.Table;

/**
 * This is a list that internaly stores everything as a single string.
 * 
 * Yes, this is one of those novelties that means nothing...
 * 
 * @author Raymond Dodge
 * @version Jan 29, 2010
 * @version Mar 12, 2010 - made a bunch of private fields protected.
 * @version Mar 13, 2010 - renamed CSVTable.CSVParseListener to CSVTable.ParseListener
 * @version Nov 9, 2010 - fix in {@link #size()}
 * @version Nov 11, 2010 - extracted {@link CSVParser}, {@link CSVParseListener}, {@link GetCSVParseListener}, {@link SizeCSVParseListener}
 * @version Nov 11, 2010 - modified to use {@link CSVPatterns}
 * @version Nov 12, 2010 - extracted {@link EntryBoundsCSVParseListener}; modified to use {@link RecordBoundsCSVParseListener}.
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues} to {@code com.rayrobdod.commaSeparatedValues}
 */
public class CSVTable extends AbstractTable<String> implements Cloneable
{
	private String string;
	private final CSVParser parser;
	private static final CSVPatterns patterns = CSVPatterns.commaDelimeted;
	
	/**
	 * Creates an empty CSVTable
	 */
	public CSVTable()
	{
		string = "";
		
		parser = new CSVParser(patterns);
	}
	
	/**
	 * Creates a CSVTable from the included CSV source
	 * @param str the string to initialize this from
	 */
	public CSVTable(String str)
	{
		this();
		
		string = str;
	}
	
	/**
	 * Creates a CSVTable initialized from the specified table.
	 * @param table the table to initialize this from
	 */
	public CSVTable(Table<String> table)
	{
		this();
		
		List<Integer> size = table.detailedSize();
		
		for (int record = 0; record < size.size(); record++)
		{
			this.addRecord();
			
			for (int field = 0; field < size.get(record); field++)
			{
				this.add(table.get(record, field));
			}
		}
	}
	
	@SuppressWarnings("all")
	protected String encode(String str)
	{
		if (!patterns.getFieldDelimeterPattern().matcher(str).find()
				&& !patterns.getEnclosurePattern().matcher(str).find()
				 && !patterns.getEscapePattern().matcher(str).find()
				 && !patterns.getRecordDelimeterPattern().matcher(str).find())
		{
			return str;
		}
		
		if (patterns.getEnclosure() == 0)
		{
			str = str.replace("" + patterns.getFieldDelimeter(), "" + patterns.getEscape() + patterns.getFieldDelimeter());
			str = str.replace("" + patterns.getEscape(), "" +  patterns.getEscape() +  patterns.getEscape());
			str = str.replace("" + (char) 10, "" +  patterns.getEscape() + (char) 10);
			str = str.replace("" + (char) 13, "" +  patterns.getEscape() + (char) 13);
			str = str.replace("" + patterns.getRecordDelimeter(), "" +  patterns.getEscape() + patterns.getRecordDelimeter());
		}
		else
		{
			char enclosure = patterns.getEnclosure();
			
			str = enclosure + str.replaceAll("" + enclosure, "" + enclosure
					+ enclosure) + enclosure;
		}
		
		return str;
	}
	
	/**
	 * This ensures that a record and field exist. It throws an exception if it doesn't.
	 * @param record the record number
	 * @param field the field number
	 * @throws IndexOutOfBoundsException if the (record, field) does not exist
	 */
	protected void ensureExists(int record, Integer field) throws IndexOutOfBoundsException
	{
		List<Integer> size = this.detailedSize();
		
		if (record > size.size()) throw new IndexOutOfBoundsException("Table does not have that many" +
				" records. Has: " + size.size() + "; Requested: " + record);
		if (field != null)
		{
			if (field > size.get(record)) throw new IndexOutOfBoundsException("Record does not have that many" +
					" fields. Has: " + size.get(record) + "; Requested: " + field);
		}
		if (record < 0) throw new IndexOutOfBoundsException("requested a negative record");
		if (field != null)
		{
			if (field < 0) throw new IndexOutOfBoundsException("requested a negative field");
		}
	}
	
	public void clear()
	{
		string = "";
	}

	public String get(final int record, final int field) throws IndexOutOfBoundsException
	{
		ensureExists(record,field);
		
		GetCSVParseListener getter = new GetCSVParseListener(record,field);
		
		parser.parse(getter, string);
		
		return getter.getValue();
	}

	public List<Integer> detailedSize()
	{
		SizeCSVParseListener listener = new SizeCSVParseListener();
		
		parser.parse(listener, string);
		
		return listener.getSize();
	}

	public String set(final int record, final int field, final String set) throws IndexOutOfBoundsException
	{
		ensureExists(record,field);
		
		String returnValue = get(record,field);
		
		EntryBoundsCSVParseListener l = new EntryBoundsCSVParseListener(record,field); 
		parser.parse(l, string);
		string = string.substring(0, l.getStartIndex()) + set + string.substring(l.getEndIndex());
		
		return returnValue;
	}

	public String remove(final int record, final int field) throws IndexOutOfBoundsException
	{
		ensureExists(record,field);
		
		String returnValue = get(record,field);
		
		EntryBoundsCSVParseListener l = new EntryBoundsCSVParseListener(record,field); 
		parser.parse(l, string);
		
		if (string.charAt(l.getEndIndex()) == patterns.getFieldDelimeter())
		{
			// +1 to remove comma too
			string = string.substring(0, l.getStartIndex())
					+ string.substring(l.getEndIndex() + 1);
		}
		else
		{
			// +1 to remove comma too
			string = string.substring(0, l.getStartIndex() - 1)
					+ string.substring(l.getEndIndex());
		}
		
		return returnValue;
	}

	public void removeRecord(final int record) throws IndexOutOfBoundsException
	{
		ensureExists(record,null);

		RecordBoundsCSVParseListener l = new RecordBoundsCSVParseListener(record); 
		parser.parse(l, string);
		
		// this one apparently counts in the record breaks...
		string = string.substring(0, l.getStartIndex())
				+ string.substring(l.getEndIndex());
	}

	public void add(final int record, final String e) throws IndexOutOfBoundsException
	{
		ensureExists(record,null);

		RecordBoundsCSVParseListener l = new RecordBoundsCSVParseListener(record); 
		parser.parse(l, string);
		string = string.substring(0, l.getEndIndex()) + patterns.getFieldDelimeter() +
				e + string.substring(l.getEndIndex());
	}
	
	public void addRecord()
	{
		string = string + patterns.getRecordDelimeter();
	}
	
	public boolean add(String str)
	{
		if (string.charAt(string.length() - 1) == 10 || string.charAt(string.length() - 1) == 13)
		{
			string = string + (str == "" ? "\"\"" : this.encode(str));
		}
		else
		{
			string = string + patterns.getFieldDelimeter() + this.encode(str);
		}
		return true;
	}
	
	public Iterator<String> iterator()
	{
		class CSVIterator implements Iterator<String>
		{
			private int currentIndex;
			private NextCSVParseListener l;
			
			public CSVIterator()
			{
				currentIndex = 0;
			}
			
			public boolean hasNext()
			{
				return currentIndex != string.length();
			}

			public String next() throws NoSuchElementException
			{
				parser.parse(l, string, currentIndex);
				
				currentIndex = l.getEndIndex();
				return l.getString();
			}

			public void remove() throws UnsupportedOperationException, IllegalStateException
			{
				// TODO Auto-generated method stub
				
			}
		}
		
		return new CSVIterator();
	}
	
	public <T> T[][] to2DArray(T[][] array) throws ArrayStoreException,
			NullPointerException
	{
		// TODO Auto-generated method stub
		return super.to2DArray(array);
	}
	
	public CSVTable clone()
	{
		try
		{
			return (CSVTable) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new CloneNotSupportedError(e);
		}
	}
	
	/**
	 * returns the CSV representation of this file.
	 * @return the string representatino of thsi file
	 */
	public String toString()
	{
		return string;
	}
}
