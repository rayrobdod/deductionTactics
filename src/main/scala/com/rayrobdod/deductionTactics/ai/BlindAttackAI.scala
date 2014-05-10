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
	def buildTeam(size:Int) = randomTeam(size)
	
	def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
		
		implicit object TokenPairOrdering extends Ordering[(Token, Token)] {
			def distance(a:(Token, Token)):Int = distance(a._1, a._2)
			def distance(a:Token, b:Token):Int = a.currentSpace.distanceTo(
					b.currentSpace,
					a.currentSpace.typeOfSpace.canEnter(a, gameState.tokens)
			)
			
			def compare(a:(Token, Token), b:(Token, Token)) =
			{
				-(distance(a) compareTo distance(b))
			}
		}
		
		val queue = new PriorityQueue ++= gameState.tokens.tokens(player).flatMap{(myToken:Token) =>
			gameState.tokens.tokens.flatten.map{(hisToken:Token) =>
			{
				(myToken, hisToken)
			}}
		}
		
		Seq.empty ++ queue.flatMap({(myToken:Token, hisToken:Token) => Seq(
			GameState.TokenMove(myToken, hisToken.currentSpace),
			GameState.TokenAttackDamage(myToken, hisToken)
		)}.tupled)
	}
	
	def initialize(player:Int, initialState:GameState):Memo = ""
	
	def notifyTurn(player:Int, actions:Seq[GameState.Action], memo:Memo):Memo = memo
	
	def canEquals(other:Any) = {other.isInstanceOf[BlindAttackAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[BlindAttackAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
