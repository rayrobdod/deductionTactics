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
			base.takeTurn(player, gameState, memo.asInstanceOf[Tuple5[_,_,_,_,_]]._1)
	/** Forwards command to base */
	override def buildTeam(size:Int) = base.buildTeam(size)
	
	
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	def initialize(player:Int, initialState:GameState):Memo =
	{
		val activeToken = new SharedActiveTokenProperty
		val currentState = new SharedGameStateProperty(initialState)
		val outStream = System.out
		
		val t = new Thread(new TokenSelector(
			player,
			initialState,
			System.in,
			{(index:Option[(Int, Int)]) =>
				activeToken.value = index
				printEverything(outStream, player, currentState.value, activeToken, Nil )
			}
		), "WithConsoleEventPrinting.Input")
		t.setDaemon(true)
		t.start()
		
		((
			base.initialize(player, initialState),
			Nil,
			outStream,
			currentState,
			activeToken
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
		val memoAsTuple = memo.asInstanceOf[Tuple5[_, _, _, _, _]]
		val baseMemoIn = memoAsTuple._1
		val baseMemoOut = base.notifyTurn(player, action, beforeState, afterState, baseMemoIn)
		
		val baseLogIn = memoAsTuple._2.asInstanceOf[Seq[_]].map{_.toString}
		val outStream = memoAsTuple._3.asInstanceOf[PrintStream]
		val sharedState = memoAsTuple._4.asInstanceOf[SharedGameStateProperty]
		val sharedToken = memoAsTuple._5.asInstanceOf[SharedActiveTokenProperty]
		
		sharedState.value = afterState
		val baseLogOut = (GameStateResultToMesage(action, tokensToLetters(afterState.tokens, Option(player))) +: baseLogIn).take(10)
		printEverything(outStream, player, afterState, sharedToken, baseLogOut )
		
		
		(( baseMemoOut, baseLogOut, outStream, sharedState, sharedToken))
	}
	
	private def printEverything(
		outStream:PrintStream,
		player:Int,
		afterState:GameState,
		sharedToken:SharedActiveTokenProperty,
		eventLog:Seq[String]
	
	) {
		outStream.println( controlCursorToTop )
		outStream.println( controlClearRest )
		BoardPrinter.apply(outStream, afterState.tokens, afterState.board, Option(player), None, sharedToken.value)
		outStream.println( scala.Console.RESET )
		outStream.println()
		sharedToken.value.foreach{(x:(Int, Int)) =>
			TokenPrinter(afterState.tokens.tokens(x))
		}
		outStream.println()
		eventLog.foreach{x => outStream.println(x)}
	}
	
	
	private def GameStateResultToMesage(x:GameState.Result, tokenIndexToChar:Function1[(Int, Int), Char]):String = x match {
		case GameState.TokenMoveResult(tokenIndex, space) =>
				"Token " + tokenIndexToChar(tokenIndex) + " moved"
		case GameState.TokenAttackDamageResult(attackerIndex, attackeeIndex, elem, kind) =>
				"Token " + tokenIndexToChar(attackerIndex) + " dealt " + elem.name + " " + kind.name + " damage to Token " + tokenIndexToChar(attackeeIndex)
		case GameState.TokenAttackStatusResult(attackerIndex, attackeeIndex, status) =>
				"Token " + tokenIndexToChar(attackerIndex) + " inflicted " + status.name +  " on Token " + tokenIndexToChar(attackeeIndex)
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

