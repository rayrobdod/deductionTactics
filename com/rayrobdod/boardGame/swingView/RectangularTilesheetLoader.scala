package com.rayrobdod.boardGame.swingView

import scala.collection.immutable.Iterable
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.JavaConversions.enumerationAsScalaIterator
//import com.rayrobdod.boardGame.view.{Tilesheet, JSONTilesheet}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, FileSystems,
		FileSystemNotFoundException}
import java.net.URL
import com.rayrobdod.util.services.ResourcesServiceLoader

/**
 * Like {@link java.util.ServiceLoader}, but for Tilesheets.
 * 
 * @author Raymond Dodge
 * @version 15 Apr 2012
 * @version 31 May 2012 - adding support for resources in jar files
 * @version 08 Jun 2012 - Making sure a jar file system can't be created twice
 * @version 2012 Oct 28 - copying from com.rayrobdod.boardGame.view to com.rayrobdod.boardGame.swingview
 *                        and modifying to use appropriate swingView classes.
 * @version 2012 Dec 02 - nuking; replacing implementaiton with implementation backed by [[com.rayrobdod.util.services.ResourcesServiceLoader]]
 */
final class RectangularTilesheetLoader(val service:String)
			extends Iterable[RectangularTilesheet]
{
	// IDEA: recognise files ("com/*.json") verses classes ("com.*") and load based on that difference
	
	
	def iterator:Iterator[RectangularTilesheet] = new MyIterator()
	
	private class MyIterator() extends Iterator[RectangularTilesheet]
	{
		val backing = new ResourcesServiceLoader(service).iterator;
		
		def next():RectangularTilesheet = {
			val currentTilesheetPath = backing.next();
			val currentTilesheetURL = currentTilesheetPath.toUri.toURL
			
			JSONRectangularTilesheet(currentTilesheetURL)
		}
		
		def hasNext = backing.hasNext
	}
}
