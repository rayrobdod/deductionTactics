package com.rayrobdod.javaScriptObjectNotation.parser.decoders;

import com.rayrobdod.javaScriptObjectNotation.parser.JSONDecoder;

/**
 * Decodes a JSONObject
 * @author Raymond Dodge
 * @version 16 Jan 2012
 */
public class ToJavaCollectionJSONDecoder$ implements JSONDecoder
{
	private ToJavaCollectionJSONDecoder$() {}
	
	public Object decode(String c)
			throws NullPointerException, ClassCastException
	{return ToJavaCollectionJSONDecoder.decode(c);}
	
	public static final ToJavaCollectionJSONDecoder$ MODULE$ =
			new ToJavaCollectionJSONDecoder$();
}
