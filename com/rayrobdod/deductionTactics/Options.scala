package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.view.{Tilesheet}
import com.rayrobdod.deductionTactics.view.{AvailibleTilesheetListModel}
import com.rayrobdod.swing.layouts.{MoveToLayout,
		MoveToInstantLayout, MoveToGradualLayout2,
		MoveToGradualLayout, SequentialMoveToLayout
}

/**
 * A set of options used by the program
 * @author Raymond Dodge
 * @version 29 May 2012 - only contains currentTilesheet
 * @version 24 Jul 2012 - adding movementSpeed and movementLayout 
 */
object Options
{
	var currentTilesheet:Tilesheet = AvailibleTilesheetListModel.getElementAt(0);
	
	var movementSpeed:Int = 15
	def movementLayout:MoveToLayout = if (movementSpeed <= 0) {new MoveToInstantLayout()} else {new MoveToGradualLayout(movementSpeed)}
}
