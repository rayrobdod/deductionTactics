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

import com.rayrobdod.boardGame.Space
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.collection.immutable.Seq
import scala.collection.mutable.PriorityQueue
import LoggerInitializer.{blindAttackAILogger => Logger}
import java.util.logging.Level


/**
 * An AI that, for each of it's tokens, will have the token move towards
 * and attack the closest enemy tokens.
 * 
 * @author Raymond Dodge
 * @version a.6.0
 */
final class BlindAttackAI extends PlayerAI
{
	/** [[com.rayrobdod.deductionTactics.ai.randomTeam]] */
	override def buildTeam(size:Int) = randomTeam(size)
	
	override def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
		
		implicit object TokenPairOrdering extends Ordering[(Token, Token)] {
			def distance(a:(Token, Token)):Int = distance(a._1, a._2)
			def distance(a:Token, b:Token):Int = a.currentSpace.distanceTo(
					b.currentSpace,
					new MoveToCostFunction(a, gameState.tokens)
			)
			
			def compare(a:(Token, Token), b:(Token, Token)) =
			{
				-(distance(a) compareTo distance(b))
			}
		}
		
		val queue = new PriorityQueue ++= gameState.tokens.alivePlayerTokens(player).flatMap{(myToken:Token) =>
			gameState.tokens.aliveNotPlayerTokens(player).flatten.map{(hisToken:Token) => {
				(myToken, hisToken)
			}}
		}
		
		val actions:Seq[GameState.Action] = Seq.empty ++ (
			queue.flatMap({(myToken:Token, hisToken:Token) =>
				val path = myToken.currentSpace.pathTo(
					hisToken.currentSpace,
					new MoveToCostFunction(myToken, gameState.tokens)
				)
				
				val moves = path.map{(s) => GameState.TokenMove(myToken, s)}
				val attack = GameState.TokenAttackDamage(myToken, hisToken)
				
				moves :+ attack
			}.tupled)
				
		)
		
		// return the first legal move
		
		Seq(
			actions.filter{_ match {
				case GameState.TokenMove(t:Token, s:Space[_]) =>
					val distance = t.currentSpace.distanceTo(s, new MoveToCostFunction(t, gameState.tokens))
					
					Logger.finer( ((distance, t.canMoveThisTurn)).toString )
					
					0 < distance && distance <= t.canMoveThisTurn
				case GameState.TokenAttackDamage(m:Token, o:Token) =>
					val distance = m.currentSpace.distanceTo(o.currentSpace, new AttackCostFunction(m, gameState.tokens))
					
					Logger.finer( ((m.canAttackThisTurn, distance, m.tokenClass.map{_.range}.getOrElse(-1))).toString )
					
					m.canAttackThisTurn && (distance <= m.tokenClass.map{_.range}.getOrElse(-1))
				case _ => false
			}}.headOption.getOrElse(GameState.EndOfTurn)
		)
	}
	
	override def initialize(player:Int, initialState:GameState):Memo = new SimpleMemo
	
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = memo
	
	def canEquals(other:Any) = {other.isInstanceOf[BlindAttackAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[BlindAttackAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
