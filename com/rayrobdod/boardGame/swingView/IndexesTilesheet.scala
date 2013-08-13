package com.rayrobdod.boardGame.swingView

import java.awt.Color
import java.awt.Dimension
import scala.util.Random
import com.rayrobdod.swing.SolidColorIcon
import com.rayrobdod.boardGame.RectangularField

/**
 * A tilesheet that prints indexies on a tile
 * 
 * @author Raymond Dodge
 * @version 25 Aug 2012
 */
object IndexesTilesheet extends RectangularTilesheet {
	override def name = "IndexesTilesheet"
	val dim = new Dimension(32,32)
	
	val lightIcon = new SolidColorIcon(Color.magenta, dim.width, dim.height)
	val darkIcon  = new SolidColorIcon(Color.cyan, dim.width, dim.height)
	
	def getIconFor(f:RectangularField, x:Int, y:Int, rng:Random) = {
		((
			if ((x+y)%2 == 0) {lightIcon} else {darkIcon},
			new IndexIcon(x,y)
		))
	}
	
	class IndexIcon(xIndex:Int, yIndex:Int) extends javax.swing.Icon {
		override def getIconWidth = dim.width
		override def getIconHeight = dim.height
		
		import java.awt.{Component, Graphics}
		override def paintIcon(c:Component, g:Graphics, x:Int, y:Int)
		{
			g.setColor(Color.black)
			g.drawString("(x,y)", x, y + dim.height - 18)
			g.drawString("(" + xIndex + "," + yIndex + ")" , x, y + dim.height - 5)
		}
	}
}
