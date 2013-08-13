package com.rayrobdod.javaScriptObjectNotation.parser.decoders;

import com.rayrobdod.javaScriptObjectNotation.parser.JSONDecoder;

/**
 * TODO: see if this works as expected in Scala
 * UPDATE: Apparently, "object ToJavaObjectJSONDecoder is not a value", unless I go the long way...
 * UPDATE: The scala compiler treats scala classes as special.
 * @author Raymond Dodge
 * @version 16 Jan 2012
 */
public final class ToJavaObjectJSONDecoder$ implements JSONDecoder, scala.ScalaObject 
{
	private ToJavaObjectJSONDecoder$() {}
	
	public Object decode(String c)
			throws NullPointerException, ClassCastException
	{return ToJavaObjectJSONDecoder.decode(c);}
	
	public static final ToJavaObjectJSONDecoder$ MODULE$ =
			new ToJavaObjectJSONDecoder$();
}
