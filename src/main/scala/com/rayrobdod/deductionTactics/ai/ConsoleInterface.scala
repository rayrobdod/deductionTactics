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
package com.rayrobdod.deductionTactics.ai

import com.rayrobdod.deductionTactics.{PlayerAI, Player, Token}
import com.rayrobdod.boardGame.{RectangularField => Field}
import com.rayrobdod.deductionTactics.consoleView.BoardNavigator

/**
 * A PlayerAI for user interaction via console.
 * 
 * Requires ANSI escape codes to work. Is not reusable.
 * @author Raymond Dodge
 * @version a.5.1
 */
final class ConsoleInterface extends PlayerAI
{
	private val endOfTurnLock = new Object();
	
	def takeTurn(player:Player) {
		endOfTurnLock.synchronized( endOfTurnLock.wait() );
	}
	
	def initialize(player:Player, field:Field) {
		val runner = new BoardNavigator(player.tokens, field)
		
		player.addVictoryReaction(runner.EndOfGameListener)
		player.addDefeatReaction (runner.EndOfGameListener)
		
		runner.addEndOfTurnReaction({() =>
			endOfTurnLock.synchronized( endOfTurnLock.notifyAll() );
		})
		player.tokens.tokens.flatten.foreach{(t:Token) => 
			t.selectedReactions_+=(new runner.SelectedListener(t))
		}
		
		new Thread(runner, "ConsoleInterface").start()
	}
	
	def buildTeam = {
		// TODO: actual prompts
		randomTeam()
	}
	
	
	
	
	// use default equals
	
	// arbitrary number (17)
	override def hashCode = 13
	
	override def toString = this.getClass.getName
}
