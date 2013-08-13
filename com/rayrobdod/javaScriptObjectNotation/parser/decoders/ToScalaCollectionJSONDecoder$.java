package com.rayrobdod.javaScriptObjectNotation.parser.decoders;

import com.rayrobdod.javaScriptObjectNotation.parser.JSONDecoder;

// stub for JavaDoc

/**
 * This takes a String that is properly JSONEncoded and turns it into a Scala Collections object
 * 
 * (written in Scala)
 * 
 * @author Raymond Dodge
 * @version 16 Jan 2012
 */
public final class ToScalaCollectionJSONDecoder$ implements JSONDecoder
{
	public static final ToScalaCollectionJSONDecoder$ MODULE$ = new ToScalaCollectionJSONDecoder$();
	
	/**
	 * This returns a valid decoding of a CharSequence into a Scala Object
	 * @param c the CharSequence to decode
	 * @return a Seq, JSONString, Number, Boolean or null
	 * @throws NullPointerException if c is null
	 * @throws ClassCastException if the string is does not match any associate types
	 */
	 public final Object decode(String c) { return null;}
}
