package com.rayrobdod.javaScriptObjectNotation;

import java.text.ParseException;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.text.Segment;
import com.rayrobdod.util.WrappedObject;

/**
 * <p>
 * This is a String in the JSON language.
 * </p><p>
 * This input or output either parsed or unparsed versions of the JSONString.
 * However, all methods inherited by {@link CharSequence} output parsed versions. 
 * </p>
 * @author Raymond Dodge
 * @version Mar 13, 2010
 * @version June 28, 2010 - increased consistancy of equals and hashcode.
 * @version Aug 15, 2010 - made {@link #equals(Object)} and {@link #hashCode()} evalueate on the parsed string, not the unparsed string
 * @version Aug 29, 2010 - {@link #isValid(CharSequence)} no longer accepts empty strings
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.javaScriptObjectNotation} to {@code com.rayrobdod.javaScriptObjectNotation;}
 */
public final class JSONString implements CharSequence, Comparable<JSONString>, Iterable<Character>
{
	private CharSequence unparsed;
	
	/**
	 * Constructs a JSONString that is equivalent to the given CharSequence.
	 * This means that for all i in <code>0&lt;i&lt;c.length()</code>,
	 * <code>generateParsed(c).charAt(i) == c.charAt(i)</code>
	 * 
	 * @param c the CharSequence to copy
	 * @return a JSONString equal to c
	 */
	public static final JSONString generateParsed(CharSequence c)
	{
		try
		{
			return new JSONString(encode(c));
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
	}
	
	/**
	 * This generates a JSONString from a raw CharSequence. This will parse c, and
	 * be equivalent to the result.
	 * @param c the CharSequence to parse
	 * @return a parsed JSONString
	 * @throws ParseException if c is not a properly formed JSON string.
	 */
	public static final JSONString generateUnparsed(CharSequence c) throws ParseException
	{
		return new JSONString(c);
	}
	
	/**
	 * Creates a JSONString from the specified unparsedString
	 * @param unparsedString the string to build this from
	 * @throws ParseException if the string is malformed.
	 */
	protected JSONString(CharSequence unparsedString) throws ParseException
	{
		// this will throw an error if there is one
		parse(new ParseAdapter(), unparsedString);
		
		this.unparsed = unparsedString.toString();
	}

	public char charAt(final int index) throws IndexOutOfBoundsException
	{
		final WrappedObject<Character> returnValue = new WrappedObject<Character>();
		
		class CharAtParseListener implements ParseListener
		{
			private int currentIndex = -1;
			
			public boolean abort()
			{
				return (returnValue.get() != null);
			}

			public void charRead(char character)
			{
				currentIndex++;
				
				if (currentIndex == index)
				{
					returnValue.set(character);
				}
			}
			
			public void ended()
			{
			}
		}
		
		try
		{
			this.parse(new CharAtParseListener());
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
		
		if (returnValue.get() != null)
		{
			return returnValue.get();
		}
		else
		{
			throw new IndexOutOfBoundsException("index: " + index);
		}
	}
	
	public int length()
	{
		final WrappedObject<Integer> returnValue = 
			new WrappedObject<Integer>();
		
		class ToStringParseListener implements ParseListener
		{
			int index = 0;
			
			public boolean abort()
			{
				return false;
			}

			public void charRead(char character)
			{
				index++;
			}

			public void ended()
			{
				returnValue.set(index);
			}
		}
		
		try
		{
			parse(new ToStringParseListener());
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
		
		return returnValue.get();
	}
	
	public CharSequence subSequence(int start, int length)
	{
		return new Segment(this.toString().toCharArray(), start, length);
	}
	
	public int compareTo(JSONString other)
	{
		Iterator<Character> thisIterator = this.iterator();
		Iterator<Character> otherIterator = other.iterator();
		
		while (thisIterator.hasNext() && otherIterator.hasNext())
		{
			Character thisChar = thisIterator.next();
			Character otherChar = otherIterator.next();
			
			if (!thisChar.equals(otherChar))
			{
				return (thisChar.compareTo(otherChar));
			}
		}
		
		if (thisIterator.hasNext())
		{
			return 1;
		}
		else if (otherIterator.hasNext())
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
	
	public Iterator<Character> iterator()
	{
		class JSONStringIterator implements Iterator<Character>
		{
			int currentIndex;
			
			public boolean hasNext()
			{
				return (unparsed.charAt(currentIndex + 1) != '"');
			}

			public Character next() throws NoSuchElementException
			{
				currentIndex++;
				int currentChar = unparsed.charAt(currentIndex);
				int secondChar = (currentIndex + 1 < unparsed.length() ? 
						unparsed.charAt(currentIndex + 1) : -1);
				
				if (currentChar == '"')
				{
					throw new NoSuchElementException();
				}
				else if (currentChar == '\\')
				{
					if (secondChar == '/')
					{
						currentIndex++;
						return '/';
					}
					else if (secondChar == '\\')
					{
						currentIndex++;
						return '\\';
					}
					else if (secondChar == '"')
					{
						currentIndex++;
						return '\"';
					}
					else if (secondChar == 'b')
					{
						currentIndex++;
						return '\b';
					}
					else if (secondChar == 'f')
					{
						currentIndex++;
						return '\f';
					}
					else if (secondChar == 'n')
					{
						currentIndex++;
						return '\n';
					}
					else if (secondChar == 'r')
					{
						currentIndex++;
						return '\r';
					}
					else if (secondChar == 't')
					{
						currentIndex++;
						return '\t';
					}
					else if (secondChar == 'u')
					{
						CharSequence hexcode = unparsed.subSequence(currentIndex + 2,
								currentIndex + 2 + 4);
						
						char readChar = (char) Integer.parseInt(hexcode.toString(), 16);

						currentIndex += 5;
						return readChar;
					}
					else
					{
						throw new NoSuchElementException();
					}
				}
				else
				{
					return (char) currentChar;
				}
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
			
		}
		
		return new JSONStringIterator();
	}
	
	/**
	 * Determines if the string is a valid string
	 * @param unparsed
	 * @return true if the unparsed string can parse without exceptions
	 */
	public static boolean isValid(CharSequence unparsed)
	{
		if (unparsed.length() == 0) return false;
		try
		{
			parse(new ParseAdapter(), unparsed);
			
			return true;
		}
		catch (ParseException e)
		{
//			e.printStackTrace();
			
			return false;
		}
	}
	
	/**
	 * returns the unparsed version of this string
	 * @return the unparsed version of this string
	 */
	public String getUnparsedString()
	{
		return unparsed.toString();
	}
	
	public String toString()
	{
		final WrappedObject<StringBuilder> wrappedBuilder = 
			new WrappedObject<StringBuilder>(new StringBuilder());
		
		class ToStringParseListener implements ParseListener
		{
			public boolean abort()
			{
				return false;
			}

			public void charRead(char character)
			{
				wrappedBuilder.get().append(character);
			}

			public void ended()
			{
				// do nothing
			}
		}
		
		try
		{
			parse(new ToStringParseListener());
		}
		catch (ParseException e)
		{
			throw new AssertionError(e);
		}
		
		return wrappedBuilder.get().toString();
	}

	
	/**
	 * This is a listener used by {@link #parse(ParseListener)} to
	 * notify about interesting events.
	 */
	protected interface ParseListener extends EventListener
	{
		/**
		 * called each time is character is read
		 * @param character the read character
		 */
		void charRead(char character);
		/**
		 * called to determine if parsing should end
		 * @return true if the parsing should end
		 */
		boolean abort();
		/**
		 * called when parsing ends
		 */
		void ended();
	}
	
	/** an implementation of Parselistener that does nothing */
	protected static class ParseAdapter implements ParseListener
	{
		public boolean abort()
		{
			return false;
		}

		public void charRead(char character)
		{
			// do nothing
		}

		public void ended()
		{
			// do nothing
		}
	}
	
	/**
	 * Parses the specified string
	 * @param l the listener that is told what to do upon interesting events
	 * @param string the string to parse
	 * @throws ParseException if an exception occurs parsing the string
	 * @throws NullPointerException if the listener is null
	 */
	protected static void parse(ParseListener l, CharSequence string)
			throws ParseException, NullPointerException
	{
		string = string.toString().trim();
		
		if (string.charAt(0) != '"')
		{
			throw new ParseException("the first character of the string was not a '\"'", 0);
		}	
		else if (string.charAt(string.length() - 1) != '"')
		{
			throw new ParseException("the last character of the string was not a '\"'", string.length() - 1);
		}

		Map<Character, Character> codes = getEscapeToCharacters();
		
		int currentIndex = 1;
		int currentChar = -1;
		int secondChar = -1;
		
		while (currentIndex < string.length() - 1 && !l.abort())
		{
			currentChar = string.charAt(currentIndex);
			secondChar = (currentIndex + 1 < string.length() ? 
					string.charAt(currentIndex + 1) : -1);
			
			if (currentChar == '"')
			{
				throw new ParseException("all '\"' in string must be escaped",
						currentChar);
			}
			else if (currentChar == '\\')
			{
				if (codes.containsKey(new Character((char) secondChar)))
				{
					l.charRead(codes.get((char) secondChar));
					currentIndex++;
				}
				else if (secondChar == 'u')
				{
					CharSequence hexcode = string.subSequence(currentIndex + 2,
							currentIndex + 2 + 4);
					
					char readChar = (char) Integer.parseInt(hexcode.toString(), 16);
					
					l.charRead(readChar);
					currentIndex += 5;
				}
				else
				{
					throw new ParseException("unexpected chatacter after '\\':" +
							" char: " + secondChar + "; index: " + currentIndex, currentIndex);
				}
			}
			else
			{
				l.charRead((char) currentChar);
			}

			currentIndex++;
		}
		
		l.ended();
	}
	
	/**
	 * Parses the string contained in this
	 * @param l the listener that is told what to do upon interesting events
	 * @throws ParseException if an exception occurs parsing the string
	 * @throws NullPointerException if the listener is null
	 */
	protected void parse(ParseListener l) throws ParseException, NullPointerException
	{
		parse(l, this.unparsed);
	}
	
	/**
	 * This is the inverse of parse.
	 * 
	 * @param s the CharSequence to encode
	 * @return an encoded JSONString
	 */
	protected static CharSequence encode(CharSequence s)
	{
		Map<Character, Character> codes = getCharactersToEscape(); 
		// for (int i = 0x0000; i < 0x000F; i++)
		
		StringBuilder returnValue = new StringBuilder();
		int currentIndex = 0;
		
		returnValue.append('"');
		
		while (currentIndex < s.length())
		{
			if (codes.containsKey(s.charAt(currentIndex)))
			{
				returnValue.append("\\" + codes.get(s.charAt(currentIndex)));
			}
			else
			{
				returnValue.append(s.charAt(currentIndex));
			}
			
			currentIndex++;
		}
		
		returnValue.append('"');
		
		return returnValue;
	}
	
	/**
	 * returns the map of character it represents to the escape characters.
	 * @return a map with escaped character as the key, and it's escape code as the value
	 */
	protected static Map<Character, Character> getCharactersToEscape()
	{
		Map<Character, Character> returnValue = new HashMap<Character, Character>();
		returnValue.put('\\', '\\');
		returnValue.put('/', '/');
		returnValue.put('\"', '\"');
		returnValue.put('\b', 'b');
		returnValue.put('\f', 'f');
		returnValue.put('\n', 'n');
		returnValue.put('\r', 'r');
		returnValue.put('\t', 't');
		
		return returnValue;
	}
	
	/**
	 * returns the map of escape characters to the character it represents.
	 * @return a map with escape codes as the key, and what it represents as the value
	 */
	protected static Map<Character, Character> getEscapeToCharacters()
	{
		Map<Character, Character> charToEsca = getCharactersToEscape();
		Map<Character, Character> returnValue = new HashMap<Character, Character>();
		
		for (Map.Entry<Character, Character> e : charToEsca.entrySet())
		{
			returnValue.put(e.getValue(), e.getKey());
		}
		
		return returnValue;
		
	}
	
	protected JSONString clone() throws CloneNotSupportedException
	{
		JSONString clone = (JSONString) super.clone();
		
		clone.unparsed = this.unparsed.toString();
		
		return clone;
	}
	
	public boolean equals(Object other)
	{
		if (!(other instanceof JSONString)) return false;
		
		return (this.toString().equals(((JSONString) other).toString()));
	}
	
	public int hashCode()
	{
		return this.toString().hashCode();
	}
}
