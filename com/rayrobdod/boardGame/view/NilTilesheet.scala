package com.rayrobdod.boardGame.view

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.{TYPE_INT_RGB => ImageTypeIntRBG}

/**
 * A tilesheet that has only one rule: for anything, display black image.
 * @author Raymond Dodge
 * @version 19 Aug 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
object NilTilesheet extends Tilesheet
{
	override val name = "Nil"
	override val rules:Seq[RectangularVisualizationRule] = Seq(
		new BluntRectangularVisualizationRule(
			image = new BufferedImage(16,16,ImageTypeIntRBG)
		)		
	)
}
