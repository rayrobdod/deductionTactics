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
package main

import scala.collection.immutable.{Seq, Set, BitSet}
import LoggerInitializer.{turnCyclerLogger => Logger}
import java.util.logging.Level
import PlayerTurnCycler._
import com.rayrobdod.boardGame.{Space, StrictRectangularSpace}

/**
 * Iterates through the players, handing out turns to each in turn.
 * 
 * This is a Runnable and does not create its own thread.
 * @param players the players of the game
 * @param timeBetweenTurns a delay between turns incase the turns are moving too quickly.
 * 
 * @author Raymond Dodge
 * @version a.6.0
 */
final class PlayerTurnCycler(
		val players:Seq[PlayerAI],
		val initialState:GameState,
		var timeBetweenTurns:Int = 500
) extends Runnable {
	
	def run():Unit = { 
		try {
			var currentState:GameState = initialState
			def gameEnded:Boolean = {remainingPlayers(currentState).size == 1}
			var playerOfCurrentTurn:Int = 0
			var memos = players.zipWithIndex.map({(p:PlayerAI,i:Int) => p.initialize(i, initialState.copy(tokens = initialState.tokens.hideTokenClasses(i)))}.tupled)
			
			// start first player's turn
			currentState = startTurn(currentState, playerOfCurrentTurn)
			
			while(!gameEnded) {
				val playerSeenState = asSeenByPlayer(currentState, playerOfCurrentTurn)
				
				
				val actionSeq = {
					val a = players(playerOfCurrentTurn)
							.takeTurn(playerOfCurrentTurn, playerSeenState, memos(playerOfCurrentTurn))
					if (a.contains(GameState.EndOfTurn)) {
						 a.take(a.indexOf(GameState.EndOfTurn) + 1)
					} else {
						a
					}
				}
				
				Logger.finer(actionSeq.map{_.getClass.getName}.toString)
				
				//
				// You'd think scala would allow
				// {{{ a = try { calcResult() } catch { case _ => None} }}}
				// but apparently not.
				actionSeq.foreach{(action) =>
					try {
						val newState:Option[GameState] = action match {
							case GameState.TokenMove(t, s) =>
								Logger.finer("Token Move")
								Some(currentState.tokenMove(playerOfCurrentTurn, t, s))
							case GameState.TokenAttackDamage(a, d) =>
								val dIndex = playerSeenState.tokens.indexOf(d)
								val d2 = currentState.tokens.tokens(dIndex)
								
								Logger.finer("Token Attack for Damage")
								Some(currentState.tokenAttackDamage(playerOfCurrentTurn, a, d2))
							case GameState.TokenAttackStatus(a, d) =>
								val dIndex = playerSeenState.tokens.indexOf(d)
								val d2 = currentState.tokens.tokens(dIndex)
								
								Logger.finer("Token Attack for Damage")
								Some(currentState.tokenAttackStatus(playerOfCurrentTurn, a, d2))
							case GameState.EndOfTurn =>
								val a = endTurn(currentState)
								playerOfCurrentTurn = (playerOfCurrentTurn + 1) % currentState.tokens.tokens.size
								Logger.finer("End of Turn")
								Some(startTurn(endTurn(currentState), playerOfCurrentTurn))
						}
						val result = action match {
							case GameState.TokenMove(t, s) =>
								GameState.TokenMoveResult(
									playerSeenState.tokens.indexOf(t),
									s
								)
							case GameState.TokenAttackDamage(a, d) =>
								GameState.TokenAttackDamageResult(
									playerSeenState.tokens.indexOf(a),
									playerSeenState.tokens.indexOf(d),
									a.tokenClass.get.atkElement,
									a.tokenClass.get.atkWeapon
								)
							case GameState.TokenAttackStatus(a, d) =>
								GameState.TokenAttackStatusResult(
									playerSeenState.tokens.indexOf(a),
									playerSeenState.tokens.indexOf(d),
									a.tokenClass.get.atkStatus
								)
							case GameState.EndOfTurn =>
								GameState.EndOfTurn
						}
						
						newState.foreach{(a:GameState) =>
							players.zipWithIndex.foreach({(p:PlayerAI, i:Int) =>
								Logger.finer("Notifying Player " + i)
								
								val beforeView = asSeenByPlayer(currentState, i)
								val afterView  = asSeenByPlayer(a, i)
								
								memos = memos.updated(i, p.notifyTurn(i, result, beforeView, afterView, memos(i)))
							}.tupled)
						}
						currentState = newState.getOrElse(currentState)
						
					} catch {
						case e:IllegalArgumentException => e.printStackTrace()
					}
				}
			}
		} catch {
			case e:Throwable => e.printStackTrace()
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
	
	def startTurn(gs:GameState, playerNumber:Int):GameState = {
		val a = GameState(
			gs.board,
			new ListOfTokens(gs.tokens.tokens.zipWithIndex.map{(x) =>
				if (x._2 == playerNumber) {
					x._1.map{(t) => t.startOfTurn}.map{(t) => t.currentStatus.affect(t)}
				} else {
					x._1
				}
			})
		)
		
		a.tokens.tokens(playerNumber).zipWithIndex.foldLeft(a){(newGs, token_index) =>
			(0 until token_index._1.currentStatus.randMovesPerTurn).foldLeft(newGs){(newGs2, move) =>
				performConfusionMove(playerNumber, newGs2.tokens.tokens(playerNumber, token_index._2), newGs2)
			}
		}
	}
	
	def endTurn(gs:GameState):GameState = GameState(
		gs.board,
		new ListOfTokens(gs.tokens.tokens.map{_.map{_.endOfTurn}})
	)
	
	private def performConfusionMove(player:Int, t:Token, gs:GameState):GameState = {
		val possibleDestinations:Seq[Space[SpaceClass]] = scala.util.Random.shuffle(t.currentSpace.adjacentSpaces.to[Seq])
		val a = possibleDestinations.collect{case x:StrictRectangularSpace[SpaceClass] => x}
		val b:Seq[(StrictRectangularSpace[SpaceClass], Option[Token], Int)] = a.map{s => Tuple3(
			s,
			gs.tokens.aliveTokens.flatten.find(t => t.currentSpace == s),
			t.currentSpace.distanceTo(s, new MoveToCostFunction(t, gs.tokens))
		)}
		val c = b.filter{x => (x._2 != None && gs.tokens.indexOf(x._2.get)._1 != player && t.canAttackThisTurn) || x._3 <= t.canMoveThisTurn}
		val d = c.map{_ match {
			case (_, Some(x), _) => gs.tokenAttackDamage(player, t, x)
			case (s, _, _) => gs.tokenMove(player, t, s)
		}}
		d.headOption.getOrElse(gs)
	}
}
