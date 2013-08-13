package com.rayrobdod.boardGame.view

import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.{TYPE_INT_RGB => ImageTypeIntRGB}
import com.rayrobdod.boardGame.view.{BluntRectangularVisualizationRule => VisRule}

/**
 * A tilesheet that creates a Checked pattern.
 * 
 * @author Raymond Dodge
 * @version 2012 Apr 20
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
		val returnValue = new BufferedImage(dim.width, dim.height, ImageTypeIntRGB)
		val g = returnValue.getGraphics 
		g.setColor(color)
		g.fillRect(0, 0, dim.width, dim.height)
		returnValue
	}
}
