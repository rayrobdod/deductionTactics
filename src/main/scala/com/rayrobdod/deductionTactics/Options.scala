package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.swingView.{RectangularTilesheet => Tilesheet}
import com.rayrobdod.deductionTactics.swingView.{AvailibleTilesheetListModel}
import com.rayrobdod.swing.layouts.{MoveToLayout,
		MoveToInstantLayout, MoveToGradualLayout2,
		MoveToGradualLayout, SequentialMoveToLayout
}

/**
 * A set of options used by the program
 * @author Raymond Dodge
 * @version 29 May 2012 - only contains currentTilesheet
 * @version 24 Jul 2012 - adding movementSpeed and movementLayout
 * @version 28 Oct 2012 - changing imports from com.rayrobdod.boardGame.view to com.rayrobdod.boardGame.swingView
 * @todo make observable
 * @todo use java.util.prefs.Preferences
 */
object Options
{
	var currentTilesheet:Tilesheet = AvailibleTilesheetListModel.getElementAt(0);
	
	var movementSpeed:Int = 15
	def movementLayout:MoveToLayout = if (movementSpeed <= 0) {new MoveToInstantLayout()} else {new MoveToGradualLayout(movementSpeed)}
}
