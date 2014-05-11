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
final class PlayerTurnCycler(val players:Seq[PlayerAI], val initialState:GameState, var timeBetweenTurns:Int = 500) extends Runnable
{
	def run() = {
		var currentState:GameState = initialState
		def gameEnded = {remainingPlayers(currentState).size == 1}
		var playerOfCurrentTurn:Int = 0
		var memos = players.zipWithIndex.map({(p:PlayerAI,i:Int) => p.initialize(i, initialState.copy(tokens = initialState.tokens.hideTokenClasses(i)))}.tupled)
		
		while(!gameEnded) {
			val playerSeenState = GameState(
					currentState.board,
					currentState.tokens.hideTokenClasses(playerOfCurrentTurn)
			)
			
			val actions = players(playerOfCurrentTurn)
					.takeTurn(playerOfCurrentTurn, playerSeenState, memos)
			
			currentState = actions.foldLeft(currentState){(state, a) =>
				try {
					a match {
						case GameState.TokenMove(t, s) => state.tokenMove(playerOfCurrentTurn, t, s)
						case _ => state
					}
				} catch {
					case e:IllegalArgumentException => state
				}
			}
						
		}
	}
	
	def remainingPlayers(currentState:GameState):Set[Int] = {
		val t:ListOfTokens = currentState.tokens
		
		BitSet.empty ++ t.aliveTokens.zipWithIndex.filter{_._1.length != 0}.map{_._2}
	}
}
