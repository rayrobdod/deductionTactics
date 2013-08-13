package com.rayrobdod.boardGame.swingView

import java.awt.{Component => Comp, Graphics => Graph}
import javax.swing.Icon
import scala.util.Random
import com.rayrobdod.boardGame.{RectangularField => Field}

/**
 * A tilesheet that has only one rule: for anything, display black image.
 * @author Raymond Dodge
 * @version 19 Aug 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 11 Jun 2012 - changing due to the change of image in RectangularVisualizationRule
 * @version 25 Aug 2012 - change to match new type of Tilesheet
 */
object NilTilesheet extends RectangularTilesheet
{
	override val name = "Nil"
	override def getIconFor(f:Field, x:Int, y:Int, rng:Random) = getIconFor
	
	private val getIconFor = ((BlankIcon, BlankIcon))
	
	object BlankIcon extends Icon{
		def getIconWidth = 16
		def getIconHeight = 16
		def paintIcon(c:Comp, g:Graph, x:Int, y:Int) {}
	}
}
