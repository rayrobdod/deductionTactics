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
			while (a.takeTurnReturnValue.value == None) { 
				a.takeTurnReturnValueLock.wait()
			}
			
			val retVal = a.takeTurnReturnValue.value.get
			a.takeTurnReturnValue.value = None
			retVal
		})
	}
	
	def initialize(player:Int, initialState:GameState):Memo = {
		val memo = ConsoleInterfaceMemo(new SimpleMemo, player, initialState)
		
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


/**
 * @version a.6.0
 */
final class ConsoleInterfaceMemo(
		base:Memo,
		val takeTurnReturnValueLock:Object,
		var takeTurnReturnValue:TakeTurnRetValProperty,
		val runner:BoardNavigator
) extends Memo {
	
	
	override def attacks:Seq[GameState.Result] = base.attacks
	override def suspisions:Map[(Int, Int), TokenClassSuspision] = base.suspisions
	
	override def addAttack(r:GameState.Result):ConsoleInterfaceMemo =
		new ConsoleInterfaceMemo(
			base.addAttack(r),
			takeTurnReturnValueLock,
			takeTurnReturnValue,
			runner
		)
	override def updateSuspision(key:(Int, Int), value:TokenClassSuspision):ConsoleInterfaceMemo =
		new ConsoleInterfaceMemo(
			base.updateSuspision(key, value),
			takeTurnReturnValueLock,
			takeTurnReturnValue,
			runner
		)
}

/**
 * @version a.6.0
 */
object ConsoleInterfaceMemo {
	
	def apply(
			base:Memo,
			player:Int,
			initialState:GameState
	):ConsoleInterfaceMemo = {
		val takeTurnReturnValueLock = new Object
		val takeTurnRetValProperty = new TakeTurnRetValProperty
		val runner = new BoardNavigator(
				Option(player),
				initialState,
				{(x:GameState.Action) => takeTurnReturnValueLock.synchronized{
					takeTurnRetValProperty.value = Option(x)
					takeTurnReturnValueLock.notifyAll
				}}
		)
		
		
		new ConsoleInterfaceMemo(
			base = base,
			takeTurnReturnValueLock = takeTurnReturnValueLock,
			takeTurnReturnValue = takeTurnRetValProperty,
			runner = runner
		)
	}
	
	
	def apply(
			base:Memo,
			takeTurnReturnValueLock:Object,
			takeTurnReturnValue:TakeTurnRetValProperty,
			runner:BoardNavigator
	):ConsoleInterfaceMemo = { 
		new ConsoleInterfaceMemo(
			base = base,
			takeTurnReturnValueLock = takeTurnReturnValueLock,
			takeTurnReturnValue = takeTurnReturnValue,
			runner = runner
		)
	}
}

final class TakeTurnRetValProperty {
	var value:Option[GameState.Action] = None
}
