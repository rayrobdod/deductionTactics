package com.rayrobdod.boardGame.swingView

import java.awt.Color
import java.awt.Dimension
import scala.util.Random
import com.rayrobdod.swing.SolidColorIcon
import com.rayrobdod.boardGame.RectangularField

/**
 * A tilesheet that creates a Checked pattern.
 * 
 * @author Raymond Dodge
 * @version 2012 Apr 20
 * @version 11 Jun 2012 - changing due to the change of image in RectangularVisualizationRule
 * @version 25 Aug 2012 - change to match new type of Tilesheet
 * @param light,dark the two colors the checkerboard should show
 * @param dim the size of each tile in the checkerboard
 */
case class CheckerboardTilesheet(
		val light:Color = Color.white,
		val dark:Color = Color.black,
		val dim:Dimension = new Dimension(16,16)
) extends RectangularTilesheet {
	override def name = "Checkerboard: " + light + "/" + dark;
	override def toString = name + ", " + dim;
	
	val lightIcon = new SolidColorIcon(light, dim.width, dim.height)
	val darkIcon  = new SolidColorIcon(dark,  dim.width, dim.height)
	val transparentIcon = new SolidColorIcon(new Color(0,0,0,0), dim.width, dim.height)
	
	def getIconFor(f:RectangularField, x:Int, y:Int, rng:Random) = {
		(( if ((x+y)%2 == 0) {lightIcon} else {darkIcon}, transparentIcon ))
	}
}
