/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

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

/**
 * @see javafx.beans.value.ObservableValue
 */
class CurrentlySelectedTokenProperty {
	import CurrentlySelectedTokenProperty.ChangeListener
	
	private[this] var value:Option[TokenIndex] = None
	private[this] val changeListeners:Buffer[ChangeListener] = Buffer.empty
	
	def addChangeListener(l:ChangeListener):Unit = {
		changeListeners += l
	}
	
	def set(s:Option[TokenIndex]):Unit = {
		this.value = s
		changeListeners.foreach{_.apply(s)}
	}
	
	def get:Option[TokenIndex] = this.value
}

object CurrentlySelectedTokenProperty {
	type ChangeListener = Function1[Option[TokenIndex], Unit]
}
