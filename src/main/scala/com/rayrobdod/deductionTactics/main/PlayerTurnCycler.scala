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
package main

import scala.collection.immutable.{Seq, Set, BitSet}
import PlayerTurnCycler._

/**
 * Iterates through the players, handing out turns to each in turn.
 * 
 * This is a Runnable and does not create its own thread.
 * @param players the players of the game
 * @param timeBetweenTurns a delay between turns incase the turns are moving too quickly.
 * 
 * @author Raymond Dodge
 * @version a.5.0
 */
final class PlayerTurnCycler(
		val players:Seq[PlayerAI],
		val initialState:GameState,
		var timeBetweenTurns:Int = 500
) extends Runnable {
	
	def run() {
		var currentState:GameState = initialState
		def gameEnded = {remainingPlayers(currentState).size == 1}
		var playerOfCurrentTurn:Int = 0
		var memos = players.zipWithIndex.map({(p:PlayerAI,i:Int) => p.initialize(i, initialState.copy(tokens = initialState.tokens.hideTokenClasses(i)))}.tupled)
		
		// tart first player's turn
		currentState = startTurn(currentState, playerOfCurrentTurn)
		
		while(!gameEnded) {
			val playerSeenState = asSeenByPlayer(currentState, playerOfCurrentTurn)
			
			
			val action = players(playerOfCurrentTurn)
					.takeTurn(playerOfCurrentTurn, playerSeenState, memos)
			
			System.out.println(action)
			
			//
			// You'd think scala would allow
			// {{{ a = try { calcResult() } catch { case _ => None} }}}
			// but apparently not.
			try {
				val newState = action match {
					case GameState.TokenMove(t, s) =>
						Some(currentState.tokenMove(playerOfCurrentTurn, t, s))
					case GameState.TokenAttackDamage(a, d) =>
						// TODO
						None
					case GameState.TokenAttackStatus(a, d) =>
						// TODO
						None
					case GameState.EndOfTurn =>
						val a = endTurn(currentState)
						playerOfCurrentTurn = (playerOfCurrentTurn + 1) % currentState.tokens.tokens.size
						Some(startTurn(currentState, playerOfCurrentTurn))
				}
				newState.foreach{(a:GameState) =>
					players.zipWithIndex.foreach({(p:PlayerAI, i:Int) =>
						
						val beforeView = asSeenByPlayer(currentState, i)
						val afterView  = asSeenByPlayer(a, i)
						
						memos = memos.updated(i, p.notifyTurn(i, action, beforeView, afterView, memos(i)))
					}.tupled)
				}
				currentState = newState.getOrElse(currentState)
				
			} catch {
				case e:IllegalArgumentException => {}
			}
		}
	}
	
	def remainingPlayers(currentState:GameState):Set[Int] = {
		val t:ListOfTokens = currentState.tokens
		
		BitSet.empty ++ t.aliveTokens.zipWithIndex.filter{_._1.length != 0}.map{_._2}
	}
}


object PlayerTurnCycler {
	
	def asSeenByPlayer(gs:GameState, player:Int):GameState = {
		GameState(gs.board, gs.tokens.hideTokenClasses(player))
	}
	
	def startTurn(gs:GameState, playerNumber:Int):GameState = GameState(
		gs.board,
		new ListOfTokens(gs.tokens.tokens.zipWithIndex.map{(x) =>
			if (x._2 == playerNumber) {
				x._1.map{(t) => t.startOfTurn}
			} else {
				x._1
			}
		})
	)
	def endTurn(gs:GameState):GameState = GameState(
		gs.board,
		new ListOfTokens(gs.tokens.tokens.map{_.map{_.endOfTurn}})
	)
	
}
