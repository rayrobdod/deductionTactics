package com.rayrobdod.commaSeparatedValues.parser;

import java.util.regex.Pattern;
import com.rayrobdod.util.CloneNotSupportedError;

/**
 * This tells a CSVParser what characters to treat as an enclosure, escape, or delimeter.
 * The included defaults are the three most common character delimeters for files, and thr fourth is
 * what Unicode claims in the only purpose for those two control characters. If none of the four
 * defaults are appropriate, any character can be put in any spot in the constructor.
 * <p>
 * This class is immutable.
 * 
 * @author Raymond Dodge
 * @version Nov 11, 2010
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 * @see CSVParser
 */
public final class CSVPatterns implements Cloneable
{
	private final char enclosure;
	private final char escape;
	private final char fieldDelimeter;
	private final char recordDelimeter;
	
	private final Pattern enclosurePattern;
	private final Pattern escapePattern;
	private final Pattern fieldDelimeterPattern;
	private final Pattern recordDelimeterPattern;
	
	private final boolean isUsingDefaultDelimeter;

	/** 
	 * A group for when a field Delimeter is a comma (<kbd>,</kbd>). It uses the default CR/LF/CRLF
	 * record delimeter, and a '"' as an enclosure.
	 */
	public static final CSVPatterns commaDelimeted = new CSVPatterns('"', (char) 0, ',');
	/** A group for when a field Delimeter is a semicolon (<kbd>;</kbd>) */
	public static final CSVPatterns semicolonDelimeted = new CSVPatterns('"', (char) 0, ';');
	/** A group for when a field Delimeter is a tab (<kbd>\t</kbd> or <kbd>\u0009</kbd>) */
	public static final CSVPatterns tabDelimeted = new CSVPatterns('"', (char) 0, '\t');
	/**
	 * A group for the field delimeter (<kbd>\u0031</kbd>) and record Delimeter
	 * (\u0030) ASCII chars are used. this uses neither enclosures nor escapes.
	 */
	public static final CSVPatterns controlDelimeted = new CSVPatterns((char) 0,
			(char) 0, '\u0031', '\u0030');
	
	/**
	 * Creates a group where the record delimeter is the default (maches CR, LF, and CRLF),
	 * and the other characters are set to the below values
	 * @param enclosure the character that indicates the start or end of a sequence that lacks control characters
	 * @param escape the character that indicates that the next character is literal and not a control character
	 * @param fieldDelimeter the character that delimits fields
	 */
	public CSVPatterns(char enclosure, char escape, char fieldDelimeter)
	{
		this.enclosure = enclosure;
		this.escape = escape;
		this.fieldDelimeter = fieldDelimeter;
		this.recordDelimeter = '\n';
		
		this.enclosurePattern = Pattern.compile("" + enclosure);
		this.escapePattern = Pattern.compile("" + escape);
		this.fieldDelimeterPattern = Pattern.compile("" + fieldDelimeter);
		this.recordDelimeterPattern = Pattern.compile("\\r\\n?|\\n");
		
		this.isUsingDefaultDelimeter = true;
	}

	/**
	 * Creates a group where characters are set to the below values
	 * @param enclosure the character that indicates the start or end of a sequence that lacks control characters
	 * @param escape the character that indicates that the next character is literal and not a control character
	 * @param fieldDelimeter the character that delimits fields
	 * @param recordDelimeter the character that delimits records
	 */
	public CSVPatterns(char enclosure, char escape, char fieldDelimeter,
			char recordDelimeter)
	{
		this.enclosure = enclosure;
		this.escape = escape;
		this.fieldDelimeter = fieldDelimeter;
		this.recordDelimeter = recordDelimeter;
		
		this.enclosurePattern = Pattern.compile("" + enclosure);
		this.escapePattern = Pattern.compile("" + escape);
		this.fieldDelimeterPattern = Pattern.compile("" + fieldDelimeter);
		this.recordDelimeterPattern = Pattern.compile("" + recordDelimeter);

		this.isUsingDefaultDelimeter = false;
	}
	
	/**
	 * returns the character used as a enclosing; indicates that whatever is inside is
	 * a literal string.
	 * @return the enclosing character
	 */
	public final char getEnclosure()
	{
		return enclosure;
	}
	
	/**
	 * returns the character used as a escape
	 * @return the escape character
	 */
	public final char getEscape()
	{
		return escape;
	}
	
	/**
	 * returns the character used as a field delimeter
	 * @return the fieldDelimeter character
	 */
	public final char getFieldDelimeter()
	{
		return fieldDelimeter;
	}

	/**
	 * returns the character used as a record delimeter.
	 * <p>
	 * If this object was compiled with the {@link #CSVPatterns(char, char, char) the 
	 * three-char constructor}, the Pattern returned with {@link #getRecordDelimeterPattern()}
	 * may match additional new-line sequences, but this char will match the record delimeter.
	 * 
	 * @return the record delimeter character
	 */
	public final char getRecordDelimeter()
	{
		return recordDelimeter;
	}

	/**
	 * A convienience method for getting the pattern that matches the enclosure
	 * @return the enclousure as a Pattern
	 */
	public final Pattern getEnclosurePattern()
	{
		return enclosurePattern;
	}

	/**
	 * A convienience method for getting the pattern that matches the escape
	 * @return the escape as a Pattern
	 */
	public final Pattern getEscapePattern()
	{
		return escapePattern;
	}
	
	/**
	 * A convienience method for getting the pattern that matches the fieldDelimter
	 * @return the fieldDelimeter as a Pattern
	 */
	public final Pattern getFieldDelimeterPattern()
	{
		return fieldDelimeterPattern;
	}

	/**
	 * A convienience method for getting the pattern that matches the recordDelimter
	 * @return the recordDelimeter as a Pattern
	 */
	public final Pattern getRecordDelimeterPattern()
	{
		return recordDelimeterPattern;
	}
	
	protected CSVPatterns clone()
	{
		try
		{
			return (CSVPatterns) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new CloneNotSupportedError(e);
		}
	}
	
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + enclosure;
		result = prime * result + escape;
		result = prime * result + fieldDelimeter;
		result = prime * result + (isUsingDefaultDelimeter ? 1231 : 1237);
		result = prime * result + recordDelimeter;
		return result;
	}
	
	public boolean equals(Object obj)
	{
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (!(obj instanceof CSVPatterns)) { return false; }
		CSVPatterns other = (CSVPatterns) obj;
		if (enclosure != other.enclosure) { return false; }
		if (escape != other.escape) { return false; }
		if (fieldDelimeter != other.fieldDelimeter) { return false; }
		if (isUsingDefaultDelimeter != other.isUsingDefaultDelimeter) { return false; }
		if (recordDelimeter != other.recordDelimeter) { return false; }
		return true;
	}
	
	/**
	 * Creates a human-readable representation of this class.
	 * @return a string representing this class
	 */
	public String toString()
	{
		return this.getClass().toString() + " [" + 
				"enclosure=" + enclosure + ", " +
				"escape=" + escape + ", " +
				"fieldDelimeter=" + fieldDelimeter + ", " +
				"recordDelimeter=" + (isUsingDefaultDelimeter ? "default"
						: recordDelimeter) + "]";
	}
}
