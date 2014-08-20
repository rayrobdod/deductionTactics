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

import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.{PlayerAI, Token, GameState}
import com.rayrobdod.boardGame.{RectangularField => Field}
import com.rayrobdod.deductionTactics.consoleView.BoardNavigator

/**
 * A PlayerAI for user interaction via console.
 * 
 * Requires ANSI escape codes to work. Is not reusable.
 * @author Raymond Dodge
 * @version a.6.0
 */
final class ConsoleInterface extends PlayerAI
{
	
	
	def buildTeam(size:Int) = {
		// TODO: actual prompts
		randomTeam(size)
	}
	
	override def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
		val a = memo.asInstanceOf[ConsoleInterfaceMemo]
		
		return Seq(a.takeTurnReturnValueLock.synchronized{
			while (a.takeTurnReturnValue == None) { 
				a.takeTurnReturnValueLock.wait()
			}
			
			val retVal = a.takeTurnReturnValue.get
			a.takeTurnReturnValue = None
			retVal
		})
	}
	
	def initialize(player:Int, initialState:GameState):Memo = {
		val memo = new ConsoleInterfaceMemo(player, initialState)
		
		new Thread(memo.runner, "ConsoleInterface").start()
		return memo
	}
	
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = {
		val memo2:ConsoleInterfaceMemo = memo.asInstanceOf[ConsoleInterfaceMemo]
		
		memo2.runner.currentState = afterState
		
		memo2
	}
	
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[ConsoleInterface]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[ConsoleInterface].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 13
	
	override def toString = this.getClass.getName
}

final class ConsoleInterfaceMemo(
		player:Int,
		initialState:GameState
) {
	val takeTurnReturnValueLock = new Object();
	var takeTurnReturnValue:Option[GameState.Action] = None
	
	val runner = new BoardNavigator(
			Option(player),
			initialState,
			{(x:GameState.Action) => this.takeTurnReturnValueLock.synchronized{
				this.takeTurnReturnValue = Option(x)
				this.takeTurnReturnValueLock.notifyAll
			}}
	)
}
