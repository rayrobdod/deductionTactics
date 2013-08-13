package com.rayrobdod.util;

/**
 * <p>
 * Thrown when a class that expects to be able to use the {@link Object#clone()} method
 * because it extends {@link Cloneable} and other similar reasons, but still receives a 
 * {@link CloneNotSupportedException}
 * </p>
 * <p>
 * The best way to deal with this is
 * 
<pre>try
{
    Cloneable clone = (Cloneable) super.clone();
        clone.field = this.field.clone();
        ...
        clone.field = this.field.clone();
    return clone;
}
catch (CloneNotSupportedException e)
{
    throw new CloneNotSupportedError(e);
}</pre>
 * 
 * @author Raymond Dodge
 * @version Oct 18, 2009
 * @version Oct 20, 2009 - realized what AssertionError meant; so this extends that now.
 * @version 16 Dec 2011 - moved from net.verizon.rayrobdod.util to com.rayrobdod.util
 * @see CloneNotSupportedException
 * @see Cloneable
 */
public class CloneNotSupportedError extends AssertionError
{
	/**
	 * creates a CloneNotSupportedError with no message or cause
	 */
	public CloneNotSupportedError()
	{
	}
	
	/**
	 * creates a CloneNotSupportedError with a message but no cause
	 * 
	 * @param message the message
	 */
	public CloneNotSupportedError(String message)
	{
		super(message);
	}
	
	/**
	 * creates a CloneNotSupportedError with a cause
	 * 
	 * @param cause the CloneNotSupportedException that caused this
	 */
	public CloneNotSupportedError(CloneNotSupportedException cause)
	{
		super(cause);
	}
}
