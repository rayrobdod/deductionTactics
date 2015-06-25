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
package ai

import com.rayrobdod.deductionTactics.swingView.game

/**
 * A decorator for PlayerAIs. It provides a viewport to a player
 * that might not provide one itself: useful for observing Computer
 * only matches.
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithSwingViewport(val base:PlayerAI) extends DecoratorPlayerAI(base)
{
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	override def initialize(player:Int, initialState:GameState):Memo =
	{
		val tokens = initialState.tokens
		val viewmodel = new game.Top(tokens, player, initialState.board)
		viewmodel.setVisible(true)
		
		SwingInterfaceMemo(
				base = base.initialize(player, initialState), 
				panel = viewmodel
		)
	}
	
	
	/**  */
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = {
		val memo2 = memo.asInstanceOf[SwingInterfaceMemo]
		val panel = memo2.panel
		
		panel.fireNotificationListeners(action, afterState)
		
		super.notifyTurn(player, action, beforeState, afterState, memo)
	}
	
	
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[WithSwingViewport]}
	override def equals(other:Any):Boolean = {
		this.canEquals(other) && other.asInstanceOf[WithSwingViewport].canEquals(this) &&
				this.base == other.asInstanceOf[WithSwingViewport].base
	}
	override def hashCode:Int = base.hashCode * 7 + 23
	
	override def toString:String = base.toString + " with " + this.getClass.getName
}
