package com.rayrobdod.util;

/**
 * This wraps an object.
 * 
 * The only reason one of these would be used is for using a parameter to 
 * pass an object back to the caller through a parameter.
 * 
 * @author Raymond Dodge
 * @version Sep 28, 2009
 * @version 16 Dec 2011 - moved from net.verizon.rayrobdod.util to com.rayrobdod.util
 * @param <E> the object type this wraps
 */
public class WrappedObject<E>
{
	private E item;

	/**
	 * creates a wrapper for a null object
	 */
	public WrappedObject()
	{
		item = null;
	}
	
	/**
	 * creates a wrapper for the specified object
	 * @param e the object to wrap
	 */
	public WrappedObject(E e)
	{
		item = e;
	}
	
	/**
	 * Sets the wrapped item
	 * @param item the object to wrap
	 */
	public void set(E item)
	{
		this.item = item;
	}
	
	/**
	 * returns the wrapped item
	 * @return the wrapped item
	 */
	public E get()
	{
		return item;
	}
}