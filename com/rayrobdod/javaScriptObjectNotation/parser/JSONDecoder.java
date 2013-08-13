package com.rayrobdod.javaScriptObjectNotation.parser;

/**
 * This takes a String that is properly JSON encoded and turns it into a Java Object
 * 
 * This is basically used to affect the deep parsing of the listeners.
 * 
 * It is expected that these should be functional, as in without variables.
 */
public interface JSONDecoder
{
	/**
	 * This takes a String that is properly JSONEncoded and turns it into a Java Object
	 * @param s the CharSequence to decode
	 * @return the represented object 
	 */
	 public Object decode(String s) throws NullPointerException, ClassCastException;
}
