package com.rayrobdod.util.services;

import java.util.ServiceConfigurationError;
import static com.rayrobdod.util.services.Services.readServices;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A ServiceLoader-like that loads the path names for resources 
 * from a service file
 * @author Raymond Dodge
 * @version 18 Jul 2012
 */
public class ResourcesServiceLoader implements Iterable<Path>
{
	private final String serviceName;
	
	public ResourcesServiceLoader(String serviceName)
	{
		this.serviceName = serviceName;
	}
	
	public java.util.Iterator<Path> iterator()
	{
		return new Iterator();
	}
	
	private class Iterator implements java.util.Iterator<Path>
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
		
		public Path next() throws java.util.NoSuchElementException
		{
			if (!hasNext()) throw new java.util.NoSuchElementException();
			
			// TODO: more checks
			try
			{
				String returnString = readLines[current];
				java.net.URL returnURL = ClassLoader.getSystemResource(returnString);
				
				if (returnURL == null) throw new ServiceConfigurationError(
					"Service " + serviceName + " pointed at nonexistant resource: " +
					returnString);
				
				Path returnPath = Paths.get(returnURL.toURI());
				current++;
				return returnPath;
			}
			catch (java.net.URISyntaxException e)
			{
				throw new ServiceConfigurationError("Invalid Path", e);
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
