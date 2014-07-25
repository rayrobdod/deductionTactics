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

import com.rayrobdod.boardGame.{RectangularField => Field, Space}

import com.rayrobdod.deductionTactics.consoleView.{
			BoardPrinter,
			TokenEventPrinter
}

/**
 * A decorator for PlayerAIs. It provides a viewport to a player
 * that might not provide one itself: useful for observing Computer
 * only matches.
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithConsoleViewport(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	override def takeTurn(player:Int, gameState:GameState, memo:Memo) =
			base.takeTurn(player, gameState, memo)
	/** Forwards command to base */
	override def buildTeam(size:Int) = base.buildTeam(size)
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	def initialize(player:Int, initialState:GameState):Memo = base.initialize(player, initialState)
	
	
	
	/**  */
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = {
		
		val boardPrinter = new BoardPrinter(player.tokens, field)
		boardPrinter.printField()
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithConsoleViewport]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithConsoleViewport].canEquals(this) &&
				this.base == other.asInstanceOf[WithConsoleViewport].base
	}
	override def hashCode = base.hashCode * 7 + 31
	
	override def toString = base.toString + " with " + this.getClass.getName
}
