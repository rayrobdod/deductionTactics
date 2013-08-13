package com.rayrobdod.util.services;

import java.util.ServiceConfigurationError;
import static com.rayrobdod.util.services.Services.readServices;

/**
 * A ServiceLoader-like that loads the classes of the services rather than the
 * services themselves.
 * @author Raymond Dodge
 * @version 09 Jul 2012
 */
public class ClassServiceLoader<A> implements Iterable<Class<? extends A>>
{
	private final Class<A> clazz;
	private final String serviceName;
	
	public ClassServiceLoader(Class<A> clazz) {this(clazz, clazz.getName());}
	
	public ClassServiceLoader(Class<A> clazz, String serviceName) 
	{
		this.clazz = clazz;
		this.serviceName = serviceName;
	}
	
	public java.util.Iterator<Class<? extends A>> iterator()
	{
		return new Iterator();
	}
	
	private class Iterator implements java.util.Iterator<Class<? extends A>>
	{
		private int current = 0;
		private final String[] readLines;
		
		public Iterator()
		{
			try
			{
				readLines = readServices(serviceName);
			}
			catch (java.io.IOException e)
			{
				throw new ServiceConfigurationError("", e);
			}
			catch (java.net.URISyntaxException e)
			{
				throw new ServiceConfigurationError("", e);
			}
		}
		
		public Class<? extends A> next()
		{
			if (!hasNext()) throw new java.util.NoSuchElementException();
			
			// TODO: more checks, such as ? acutally extends A
			try
			{
				Class<?> returnValue = Class.forName(readLines[current]);
				current++;
				return (Class<? extends A>) returnValue;
			}
			catch (ClassNotFoundException e)
			{
				throw new ServiceConfigurationError("Class not found", e);
			}
		}
		
		public boolean hasNext()
		{
			return current < readLines.length;
		}
		
		public void remove() throws UnsupportedOperationException
		{
			throw new UnsupportedOperationException("Cannot remove from a Service");
		}
	}
}
