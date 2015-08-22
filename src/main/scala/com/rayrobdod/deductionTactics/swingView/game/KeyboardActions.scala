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

import java.awt.event.KeyEvent
import javax.swing.{KeyStroke, Action}
import scala.collection.immutable.{Seq, Set}

/**
 * The set of actions that can be input via keyboard commands
 * @since next
 */
object KeyboardActions {
	
	/**
	 * Represents an action that can be input via keyboard commands
	 * 
	 * Doesn't include the swing.Action because the actions are not easily sharable
	 * between Top instances, and are not that easy to construct
	 */
	final class KeyboardAction(val name:String, val defaultKey:KeyStroke)
	
	val MoveLeft = new KeyboardAction("MoveLeft", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0))
	val MoveRight = new KeyboardAction("MoveRight", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0))
	val MoveUp = new KeyboardAction("MoveUp", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0))
	val MoveDown = new KeyboardAction("MoveDown", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0))
	val Select = new KeyboardAction("Select", KeyStroke.getKeyStroke(KeyEvent.VK_X, 0))
	val Clear = new KeyboardAction("Clear", KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0))
	val FindNextToken = new KeyboardAction("FindNextActionableToken", KeyStroke.getKeyStroke(KeyEvent.VK_N, 0))
	
	def values:Seq[KeyboardAction] = Seq[KeyboardAction](MoveLeft, MoveRight, MoveUp, MoveDown, Select, Clear, FindNextToken)
	def apply(x:Int):KeyboardAction = values(x) //.find{_.id == x}.get
	
	def withName(s:String):KeyboardAction = {
		try {
			values.find{_.name equalsIgnoreCase s}.get
		} catch {
			case x:NoSuchElementException => 
				val y = new NoSuchElementException("No element with name: "+ s)
				y.initCause(x)
				throw y
		}
	}
}
