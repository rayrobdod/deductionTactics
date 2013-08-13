package com.rayrobdod.boardGame.view

import scala.collection.immutable.Iterable
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.JavaConversions.enumerationAsScalaIterator
//import com.rayrobdod.boardGame.view.{Tilesheet, JSONTilesheet}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, FileSystems}
import java.net.URL

/**
 * Like {@link java.util.ServiceLoader}, but for Tilesheets.
 * 
 * @author Raymond Dodge
 * @version 15 Apr 2012
 */
final class TilesheetLoader(val service:String) extends Iterable[Tilesheet]
{
	private val PREFIX = "META-INF/services/"
	private val fullName = PREFIX + service
	
	private val enumOfFiles = ClassLoader.getSystemResources(fullName)
	private val enumOfTilesheetFileURLs = enumOfFiles.map{(aListOfTilesheetsURL:URL) => 
		if (aListOfTilesheetsURL.toString().startsWith("jar:"))
		{
			val env = new java.util.HashMap[String, String](); 
			env.put("create", "true");
			
			FileSystems.newFileSystem(new java.net.URI(aListOfTilesheetsURL.toString().split('!').apply(0)), env)
		}
		
		val aListOfTilesheetsPath = Paths.get(aListOfTilesheetsURL.toURI)
		
		Files.readAllLines(aListOfTilesheetsPath, StandardCharsets.UTF_8)
	}.flatten.map{ClassLoader.getSystemResource(_)}
	private val listOfTilesheetFileURLs = Seq.empty ++ enumOfTilesheetFileURLs
	
	def iterator:Iterator[Tilesheet] = new TilesheetIterator()
	
	private class TilesheetIterator() extends Iterator[Tilesheet]
	{
		private var currentPointer = 0;
		
		def next():Tilesheet = {
			val currentTilesheetURL = listOfTilesheetFileURLs(currentPointer)
			currentPointer = currentPointer + 1
			
			new JSONTilesheet(currentTilesheetURL)
		}
		
		def hasNext():Boolean = {
			return currentPointer < listOfTilesheetFileURLs.size
		}
	}
}
