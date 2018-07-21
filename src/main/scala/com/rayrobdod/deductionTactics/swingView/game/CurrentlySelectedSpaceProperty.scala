/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.rayrobdod.deductionTactics
package swingView.game

import scala.collection.mutable.Buffer
import com.rayrobdod.boardGame.RectangularIndex

/**
 * An observable container for a [[RectangularIndex]]
 * @see javafx.beans.value.ObservableValue
 * @since a.6.0
 */
class CurrentlySelectedSpaceProperty {
	import CurrentlySelectedSpaceProperty.ChangeListener
	
	private[this] var value:RectangularIndex = (0,0)
	private[this] val changeListeners:Buffer[ChangeListener] = Buffer.empty
	
	def addChangeListener(l:ChangeListener):Unit = {
		changeListeners += l
	}
	
	def set(s:RectangularIndex):Unit = {
		this.value = s
		changeListeners.foreach{_.apply(s)}
	}
	
	def get:RectangularIndex = this.value
}

object CurrentlySelectedSpaceProperty {
	type ChangeListener = Function1[RectangularIndex, Unit]
}
