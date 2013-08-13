package com.rayrobdod.javaScriptObjectNotation.parser.listeners;

// stub for JavaDoc

import com.rayrobdod.javaScriptObjectNotation.parser.JSONParseListener;
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser;
import com.rayrobdod.javaScriptObjectNotation.parser.JSONDecoder;
import java.text.ParseException;

/**
 * Parses a JSON entity into a Sequence. If the parsed entity was an object,
 * the result can be turned into a Map with Seq.toMap
 * 
 * This does not care a lot about errors in the JSON source - it will create
 * a Pair and put that in an array if it happens to find a colon in a JSONArray
 * for example - but if there is nested arrays, the decoder may have a problem
 * with invalid JSONObjects - in fact the default one does.
 * 
 * (written in Scala)
 * 
 * @param decoder - the object used to turn unparsed JSON objects into an appropriate class. 
 * @author Raymond Dodge
 * @version 03 Aug 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.javaScriptObjectNotation.parser} to {@code com.rayrobdod.javaScriptObjectNotation.parser}
 * @version 15 Jan 2011 - moved from {@code com.rayrobdod.javaScriptObjectNotation.parser}
		to {@code com.rayrobdod.javaScriptObjectNotation.parser.listeners}
 * @version 16 Jan 2011 - modifying to use a JSONDecoder, now that they exist.
 */
public class ToSeqJSONParseListener implements JSONParseListener
{
	public JSONDecoder decoder() {
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}
	
	/**
	 * @param decoder - the object used to turn unparsed JSON objects into an appropriate class.
	 */
	public ToSeqJSONParseListener(JSONDecoder decoder) {}
	
	public ToSeqJSONParseListener() {}
	
	public boolean abort() {
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}
	
	public void charRead(int index, char character)
			throws IllegalStateException {
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}

	public void elemEnded(int commaIndex, char character)
			throws IllegalStateException, ParseException {
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}

	public void elemStarted(int commaIndex, char character)
			throws IllegalStateException {
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}

	public void ended() throws IllegalStateException, ParseException
	{}

	public void endingBracket(int index, char character)
			throws IllegalStateException, ParseException
	{
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}

	public void keyValueSeparation(int colonIndex, char character)
			throws IllegalStateException, ParseException, ClassCastException{
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}

	public void openingBracket(int index, char character)
			throws IllegalStateException, ParseException{
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}

	public void started() throws IllegalStateException{
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}
	
	/**
	 * returns the string generated by running this though the parser
	 * @return the string generated by running this though the parser
	 */
	public String getString(){
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}
	
	/** Returns the parsed result. May include Pairs. */
	public scala.collection.Seq<java.lang.Object> result() {
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}
	
	
	/**
	 * Returns the parsed result converted to a Map.
	 * Most non-pair values are destroyed in the transition. 
	 */
	public scala.collection.immutable.Map<java.lang.String, java.lang.Object> resultMap() {
		throw new UnsupportedOperationException("Try compiling the scala file.");
	}

}
