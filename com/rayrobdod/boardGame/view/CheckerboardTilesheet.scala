package com.rayrobdod.boardGame.view

import java.awt.Color
import java.awt.Dimension
import com.rayrobdod.boardGame.view.{BluntRectangularVisualizationRule => VisRule}

/**
 * A tilesheet that creates a Checked pattern.
 * 
 * @author Raymond Dodge
 * @version 2012 Apr 20
 * @version 11 Jun 2012 - changing due to the change of image in RectangularVisualizationRule
 * @param light,dark the two colors the checkerboard should show
 * @param dim the size of each tile in the checkerboard
 */
case class CheckerboardTilesheet(
		val light:Color = Color.white,
		val dark:Color = Color.black,
		val dim:Dimension = new Dimension(16,16)
) extends Tilesheet {
	override def name = "Checkerboard: " + light + "/" + dark;
	override def toString = name + ", " + dim;
	
	override def rules = {
		Seq(
			new VisRule(image = image(dark), equation = "(x+y)%2==1"),
			new VisRule(image = image(light), equation = "(x+y)%2==0")
		)
	}
	
	private def image(color:Color) = {
		import com.rayrobdod.swing.SolidColorIcon
		
		new SolidColorIcon(color, dim.width, dim.height)
	}
}
