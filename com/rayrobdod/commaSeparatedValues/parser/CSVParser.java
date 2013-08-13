package com.rayrobdod.commaSeparatedValues.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Pattern;

/**
 * A parser for Comma (or other Character) Separated Values.
 * 
 * @author Raymond Dodge
 * @version Jan 29, 2010
 * @version Nov 11, 2010 - extracted from {@link com.rayrobdod.commaSeparatedValues.CSVTable}
 * @version Nov 13, 2010 - was ignoring the last character of the file; now fixed
 * @version Nov 13, 2010 - was ignoring character lengths
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.commaSeparatedValues.parser} to {@code com.rayrobdod.commaSeparatedValues.parser}
 */
public class CSVParser
{
	private final CSVPatterns patterns;
	private static final Pattern TERMINATION = Pattern.compile("[\uFFFF]");
	
	/**
	 * Creates a Parser that uses the specified patterns to determine if an
	 * interesting event has occured.
	 * @param patterns the patterns to parse with.
	 */
	public CSVParser(CSVPatterns patterns)
	{
		this.patterns = patterns;
	}

	/**
	 * Parses the content from the specified String, passing interesting events to
	 * the CSVParseListener.
	 * @param l a CSVParseListener that indicates what to do when a character 
	 * is read, on a new line, ect.
	 * @param s the string to read data from
	 * @throws NullPointerException if l is null
	 */
	public void parse(CSVParseListener l, String s)
			throws NullPointerException
	{
		try
		{
			parse(l, new StringReader(s));
		}
		catch (IOException e)
		{
			String message = "StringReader threw an IOException";
			AssertionError e1 = new AssertionError(message);
			e1.initCause(e);
			throw e1;
		}
	}
	
	/**
	 * Parses the content from the specified String, passing interesting events to
	 * the CSVParseListener.
	 * @param l a CSVParseListener that indicates what to do when a character 
	 * is read, on a new line, ect.
	 * @param s the string to read data from
	 * @param skipChars the number of chracters in the string to skip before starting
	 * @throws NullPointerException if l is null
	 */
	public void parse(CSVParseListener l, String s, int skipChars)
			throws NullPointerException
	{
		try
		{
			parse(l, new StringReader(s), skipChars);
		}
		catch (IOException e)
		{
			String message = "StringReader threw an IOException";
				AssertionError e1 = new AssertionError(message);
			e1.initCause(e);
			throw e1;
		}
	}
	
	/**
	 * Parses the content from the specified Reader, passing interesting events to
	 * the CSVParseListener.
	 * @param l a CSVParseListener that indicates what to do when a character 
	 * is read, on a new line, ect.
	 * @param reader the reader to read characters from
	 * @throws NullPointerException if l is null
	 * @throws IOException if there is an IOException
	 */
	public void parse(CSVParseListener l, Reader reader)
			throws NullPointerException, IOException
	{
		this.parse(l,reader,0);
	}
	
	/**
	 * Parses the content from the specified Reader, passing interesting events to
	 * the CSVParseListener.
	 * @param l a CSVParseListener that indicates what to do when a character 
	 * is read, on a new line, ect.
	 * @param reader the reader to read characters from
	 * @param skipChars the number of chracters in the reader to skip before starting
	 * @throws NullPointerException if l is null
	 * @throws IOException if there is an IOException
	 */
	public void parse(CSVParseListener l, Reader reader, int skipChars) throws NullPointerException,
			IOException
	{
		boolean isInsideQuotes = false;		
		int currentIndex = skipChars - 1 /* -1 so first increment makes them equal */;
		
		reader.skip(skipChars);
		l.started(currentIndex+1);
		
		char firstChar = (char) reader.read();
		char secondChar = 0;

		while (reader.ready() && !l.abort()
				&& !TERMINATION.matcher(Character.toString(
						firstChar)).matches())
		{
			currentIndex++;
			secondChar = (char) reader.read();
			
			if (isInsideQuotes)
			// in this case, ignore the effects of any characters except quotes.
			{
				if (patterns.getEnclosurePattern().matcher(firstChar + "").matches())
				{
					// check if next char is another quote
					// ex <code>""""</code> renders and <samp>"</samp>, is " is the enclosure
					if (patterns.getEnclosurePattern().matcher(secondChar + "").matches())
					{
						l.readCharacter((char) firstChar);
						
						// since two chars were used, read a new one.
						// firstChar is discarded at the end of the loop anyway.
						secondChar = (char) reader.read();
						currentIndex++;
					}
					else
					{
						isInsideQuotes = false;
					}
				}
				else
				{
					l.readCharacter((char) firstChar);
				}
			}
			else
			{
				if (patterns.getEscapePattern().matcher("" + firstChar).matches())
				{
					// write the next char
					l.readCharacter((char) secondChar);
					currentIndex++;
				}
				else if (patterns.getEnclosurePattern().matcher("" + firstChar).matches())
				{
					isInsideQuotes = true;
				}
				else if (patterns.getFieldDelimeterPattern().matcher("" + firstChar).matches())
				{
					l.newField(currentIndex);
				}
				else if (patterns.getRecordDelimeterPattern().matcher("" + firstChar + secondChar).matches())
				{
					l.newRecord(currentIndex);
					
					// since two chars were used, read a new one.
					// firstChar is discarded at the end of the loop anyway.
					secondChar = (char) reader.read();
					currentIndex++;
				}
				else if (patterns.getRecordDelimeterPattern().matcher("" + firstChar).matches())
				{
					l.newRecord(currentIndex);
				}
				else
				{
					l.readCharacter((char) firstChar);
				}
			}
			
			firstChar = secondChar;
		}
		
		l.ended(currentIndex);
	}
}
