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
package ai

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import java.io.PrintStream
import com.rayrobdod.deductionTactics.consoleView._
import scala.collection.immutable.Seq

/**
 * A decorator for PlayerAIs. It prints to console events that happen
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithConsoleEventPrinting(val base:PlayerAI) extends DecoratorPlayerAI(base)
{
	/** Number of values kept in the event log */
	private val maxEventsCount = 10
	
	
	
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	override def initialize(player:Int, initialState:GameState):Memo =
	{
		val activeToken = new SharedActiveTokenProperty
		val currentState = new SharedGameStateProperty(initialState)
		val outStream = System.out
		
		val t = new Thread(new TokenSelector(
			player,
			initialState,
			System.in,
			{(index:Option[TokenIndex]) =>
				activeToken.value = index
				printEverything(outStream, player, currentState.value, activeToken, Nil, Map.empty )
			}
		), "WithConsoleEventPrinting.Input")
		t.setDaemon(true)
		t.start()
		
		new ConsoleEventPrintingMemo(
			base.initialize(player, initialState),
			Nil,
			outStream,
			currentState,
			activeToken
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
		val memo2 = memo.asInstanceOf[ConsoleEventPrintingMemo]
		val baseMemoOut = base.notifyTurn(player, action, beforeState, afterState, memo2.base)
		
		val baseLogOut = (GameStateResultToMesage(action, tokensToLetters(afterState.tokens, Option(player))) +: memo2.baseLog).take(maxEventsCount)
		
		
		printEverything(
			memo2.outStream,
			player,
			afterState,
			memo2.sharedToken,
			baseLogOut,
			memo2.suspicions
		)
		
		
		new ConsoleEventPrintingMemo(
			baseMemoOut,
			memo2.baseLog,
			memo2.outStream,
			memo2.sharedState,
			memo2.sharedToken
		)
	}
	
	private def printEverything(
		outStream:PrintStream,
		player:Int,
		afterState:GameState,
		sharedToken:SharedActiveTokenProperty,
		eventLog:Seq[String],
		suspicions:Map[(Int, Int), TokenClassSuspicion]
	) {
		outStream.println( controlCursorToTop )
		outStream.println( controlClearRest )
		BoardPrinter.apply(outStream, afterState.tokens, afterState.board, Option(player), None, sharedToken.value)
		outStream.println( scala.Console.RESET )
		outStream.println()
		sharedToken.value.foreach{(x:TokenIndex) =>
			TokenPrinter(afterState.tokens.tokens(x), suspicions(x) )
		}
		outStream.println()
		eventLog.foreach{x => outStream.println(x)}
	}
	
	
	private def GameStateResultToMesage(x:GameState.Result, tokenIndexToChar:Function1[TokenIndex, Char]):String = x match {
		case GameState.TokenMoveResult(tokenIndex, space) =>
				"Token " + tokenIndexToChar(tokenIndex) + " moved"
		case GameState.TokenAttackDamageResult(attackerIndex, attackeeIndex, elem, kind) =>
				"Token " + tokenIndexToChar(attackerIndex) + " dealt " + elem.name + " " + kind.name + " damage to Token " + tokenIndexToChar(attackeeIndex)
		case GameState.TokenAttackStatusResult(attackerIndex, attackeeIndex, status) =>
				"Token " + tokenIndexToChar(attackerIndex) + " inflicted " + status.name +  " on Token " + tokenIndexToChar(attackeeIndex)
		case _ =>
			x.toString
	}
	
	
	
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[WithConsoleEventPrinting]}
	override def equals(other:Any):Boolean = {
		this.canEquals(other) && other.asInstanceOf[WithConsoleEventPrinting].canEquals(this) &&
				this.base == other.asInstanceOf[WithConsoleEventPrinting].base
	}
	override def hashCode:Int = base.hashCode * 7 + 43
	
	override def toString:String = base.toString + " with " + this.getClass.getName
}


/**
 * @version a.6.0
 */
final class ConsoleEventPrintingMemo(
		val base:Memo,
		val baseLog:Seq[String],
		val outStream:PrintStream,
		val sharedState:SharedGameStateProperty,
		val sharedToken:SharedActiveTokenProperty
) extends Memo {
	
	
	override def attacks:Seq[GameState.Result] = base.attacks
	override def suspicions:Map[(Int, Int), TokenClassSuspicion] = base.suspicions
	
	override def addAttack(
			r:GameState.Result
	):ConsoleEventPrintingMemo =
		new ConsoleEventPrintingMemo(
			base.addAttack(r),
			baseLog,
			outStream,
			sharedState,
			sharedToken
		)
	override def updateSuspicion(
			key:(Int, Int),
			value:TokenClassSuspicion
	):ConsoleEventPrintingMemo =
		new ConsoleEventPrintingMemo(
			base.updateSuspicion(key, value),
			baseLog,
			outStream,
			sharedState,
			sharedToken
		)
}

