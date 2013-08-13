package com.rayrobdod.boardGame.swingView

import scala.util.Random
import javax.swing.Icon
import com.rayrobdod.boardGame.RectangularField

/**
 * @author Raymond Dodge
 * @param 2012 Aug 23
 */
trait RectangularTilesheet
{
	def name:String
	
	def getIconFor(field:RectangularField, x:Int, y:Int, rng:Random):(Icon, Icon) 
}
