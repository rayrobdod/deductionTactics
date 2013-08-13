package com.rayrobdod.boardGame.view

import java.awt.{Component => Comp, Graphics => Graph}
import javax.swing.Icon

/**
 * A tilesheet that has only one rule: for anything, display black image.
 * @author Raymond Dodge
 * @version 19 Aug 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 11 Jun 2012 - changing due to the change of image in RectangularVisualizationRule
 */
object NilTilesheet extends Tilesheet
{
	override val name = "Nil"
	override val rules:Seq[RectangularVisualizationRule] = Seq(
		new BluntRectangularVisualizationRule(
			image = new Icon{
				def getIconWidth = 16
				def getIconHeight = 16
				def paintIcon(c:Comp, g:Graph, x:Int, y:Int) {}
			}
		)		
	)
}
