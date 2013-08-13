package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.view.{Tilesheet}
import com.rayrobdod.deductionTactics.view.{AvailibleTilesheetListModel}

/**
 * A set of options used by the program
 * @author Raymond Dodge
 * @version 29 May 2012 - only contains currentTilesheet
 */
object Options
{
	var currentTilesheet:Tilesheet = AvailibleTilesheetListModel.getElementAt(0);
}
