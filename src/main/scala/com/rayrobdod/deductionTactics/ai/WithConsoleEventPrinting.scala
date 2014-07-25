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

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import java.io.PrintStream
import com.rayrobdod.boardGame.{RectangularField => Field, Token => BoardGameToken, Space}
import com.rayrobdod.deductionTactics.consoleView._

/**
 * A decorator for PlayerAIs. It prints to console events that happen
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithConsoleEventPrinting(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	override def takeTurn(player:Int, gameState:GameState, memo:Memo) =
			base.takeTurn(player, gameState, memo.asInstanceOf[Tuple3[_,_,_]]._1)
	/** Forwards command to base */
	override def buildTeam(size:Int) = base.buildTeam(size)
	
	
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	def initialize(player:Int, initialState:GameState):Memo =
	{
		((
			base.initialize(player, initialState),
			Nil,
			System.out
		))
	}
	
	
	
	/**  */
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = {
		val memoAsTuple = memo.asInstanceOf[Tuple3[_, _, _]]
		val baseMemoIn = memoAsTuple._1
		val baseMemoOut = base.notifyTurn(player, action, beforeState, afterState, baseMemoIn)
		
		val baseLogIn = memoAsTuple._2.asInstanceOf[Seq[_]].map{_.toString}
		val outStream = memoAsTuple._3.asInstanceOf[PrintStream]
		
		
		outStream.println( scala.Console.RESET )
		BoardPrinter.apply(outStream, afterState.tokens, afterState.board, Option(player))
		outStream.println()
		outStream.println()
		val baseLogOut = GameStateResultToMesage(action) +: baseLogIn
		baseLogOut.foreach{x => outStream.println(x)}
		
		
		(( baseMemoOut, baseLogOut.take(5), outStream))
	}
	
	
	def GameStateResultToMesage(x:GameState.Result):String = x match {
		case GameState.TokenMoveResult(tokenIndex, space) =>
				"Token " + tokenIndex + " moved"
		case GameState.TokenAttackDamageResult(attackerIndex, attackeeIndex, elem, kind) =>
				"Token " + attackerIndex + " dealt " + elem + " " + kind + " damage to Token " + attackeeIndex
		case GameState.TokenAttackStatusResult(attackerIndex, attackeeIndex, status) =>
				"Token " + attackerIndex + " inflicted " + status +  " on Token " + attackeeIndex
		case _ =>
			x.toString
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithConsoleEventPrinting]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithConsoleEventPrinting].canEquals(this) &&
				this.base == other.asInstanceOf[WithConsoleEventPrinting].base
	}
	override def hashCode = base.hashCode * 7 + 43
	
	override def toString = base.toString + " with " + this.getClass.getName
}

