package com.rayrobdod.util.services;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.charset.StandardCharsets;

/**
 * A class with a single static method that reads all lines in a ServiceLoader style
 * file.
 * @author Raymond Dodge
 * @version 09 Jul 2012
 * @version 12 Jul 2012 - only making a new jar file system if there isn't already one
 */
public class Services
{
	private final static String PREFIX = "META-INF/services/";
	
	private Services(){}
	
	/**
	 * Reads all lines in all Service style classes with the specificed name.
	 *
	 * Anything on a line after a '#' is ignored. An empty line is ignored.
	 * Anything else is recorded and returned.
	 */
	public static String[] readServices(String service) throws java.io.IOException, java.net.URISyntaxException
	{
		final String fullPath = PREFIX + service;
		final Enumeration<URL> listOfFiles = ClassLoader.getSystemResources(fullPath);
		List<String> allLines = new ArrayList<String>();
		
		while (listOfFiles.hasMoreElements())
		{
			URL serviceFileURL = listOfFiles.nextElement();
			
			if (serviceFileURL.toString().startsWith("jar:"))
			{
				try {
					java.util.HashMap<String, String> env = new java.util.HashMap<String, String>();
					env.put("create", "true");
					
					FileSystems.newFileSystem(new java.net.URI(serviceFileURL.toString().split("!")[0]), env);
				}
				catch (java.nio.file.FileSystemAlreadyExistsException w) {
					// FileSystem exists, so it doesn't have to be made
				}
			}
			
			Path serviceFilePath = Paths.get(serviceFileURL.toURI());
			
			List<String> lines = Files.readAllLines(serviceFilePath, StandardCharsets.UTF_8);
			
			for (String line : lines)
			{
				final String trimLine = line.trim();
				
				String usedPart = "";
				if (trimLine.contains("#"))
					usedPart = trimLine.split("#")[0].trim();
				else
					usedPart = trimLine;
				
				if (!usedPart.isEmpty()) {allLines.add(usedPart);}
			}
		}
		
		return allLines.toArray(new String[0]);
	}
}
