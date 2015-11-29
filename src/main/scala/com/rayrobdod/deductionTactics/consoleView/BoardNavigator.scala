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
package consoleView

import scala.collection.immutable.{Seq, Map}
import com.rayrobdod.deductionTactics.ai.TokenClassSuspicion
import com.rayrobdod.boardGame.{RectangularSpace}

/**
 * A console application that can let a user navigate a RectangularField
 * @author Raymond Dodge
 * @since a.5.1
 * @version a.6.0
 */
class BoardNavigator(
		val player:Option[Int],
		var currentState:GameState,
		val setNextAction:Function1[GameState.Action, Unit],
		val out:java.io.PrintStream = System.out,
		val in:java.io.InputStream = System.in
) extends Runnable {
	
	private var currentSpace:RectangularSpace[SpaceClass] = currentState.board(0,0);
	private var selected:Option[TokenIndex] = None;
	private var continue:Boolean = true;
	var suspicions:Map[(Int, Int), TokenClassSuspicion] = Map.empty
	
	private val PressUp     = 'w';
	private val PressLeft   = 'a';
	private val PressRight  = 'd';
	private val PressDown   = 's';
	private val PressNextTurn = 'e';
	private val PressQuit   = 'q';
	private val PressSelect = 'x';
	private val PressTab    = 9;
	
	def run():Unit = {
		out print controlCursorToTop
		out print controlClearRest
		
		while (continue) {
			out print controlCursorToTop
			BoardPrinter.apply(out, currentState.tokens, currentState.board, player, Some(currentSpace), selected)
			out println ""
			out println controlClearRest
			
			SpaceInfoPrinter(currentSpace)
			out println ""
			
			val tokenOnSpace = currentState.tokens.aliveTokens.flatten.filter{_.currentSpace == currentSpace}.headOption
			val tokenIndex = tokenOnSpace.map{currentState.tokens.indexOf(_)}
			val tokenSuspicion = tokenIndex.map{suspicions(_)}.getOrElse(new TokenClassSuspicion)
			tokenOnSpace.foreach{TokenPrinter(_, tokenSuspicion)}
			out.println()
			// print info about current space
			
			
			
			val char = in.read(); 
			
			if (char == PressUp)     currentSpace = currentSpace.up.getOrElse(currentSpace).asInstanceOf[RectangularSpace[SpaceClass]]
			if (char == PressLeft)   currentSpace = currentSpace.left.getOrElse(currentSpace).asInstanceOf[RectangularSpace[SpaceClass]]
			if (char == PressDown)   currentSpace = currentSpace.down.getOrElse(currentSpace).asInstanceOf[RectangularSpace[SpaceClass]]
			if (char == PressRight)  currentSpace = currentSpace.right.getOrElse(currentSpace).asInstanceOf[RectangularSpace[SpaceClass]]
			if (char == PressNextTurn) setNextAction(GameState.EndOfTurn)
			if (char == PressQuit)   System.exit(0)
			if (char == PressSelect) { tokenOnSpace match {
				case None =>
					selected.map{(x) => currentState.tokens.tokens(x)}.foreach{(x) => setNextAction(GameState.TokenMove(x, currentSpace))}
				case Some(target:Token) => {
					val targetIndex = currentState.tokens.indexOf(target)
					
					if (Option(targetIndex._1) == player) {
						// if the target belongs to this character, select it
						this.selected = Option(targetIndex)
					} else {
						// otherwise, attack the target
						selected.foreach{(sel) => setNextAction(GameState.TokenAttackDamage(currentState.tokens.tokens(sel), target))}
					}
				}
			}}
		}
		
		System.out.println("End of game")
	}
	
	
}
