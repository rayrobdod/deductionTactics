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

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}

/**
 * @since a.6.0
 * @version a.6.1
 */
final case class GameState (
	val board:RectangularField[SpaceClass],
	val tokens:ListOfTokens
) {
	
	def tokenMove(player:Int, token:Token, space:RectangularSpace[SpaceClass]):GameState = {
		
		val indexOfToken:TokenIndex = {
			val a:Seq[Seq[Token]] = tokens.tokens
			val b:Seq[(Seq[(Token, Int)], Int)] = a.map{_.zipWithIndex}.zipWithIndex
			val c:Seq[(Token, Int, Int)] = b.flatMap({(st:Seq[(Token, Int)], i:Int) =>
				st.map({(t:Token, j:Int) => (( t, i, j ))
			}.tupled)}.tupled)
			val d:Option[(Token, Int, Int)] = c.find(_._1 == token)
			
			d.map{(a) => ((a._2, a._3))}.getOrElse{ throw new IllegalArgumentException("token not found") }
		}
		
		if (indexOfToken._1 != player) { throw new IllegalArgumentException("That's not your token!") }
		
		val distanceBetweenSpaces = token.currentSpace.distanceTo(space, new MoveToCostFunction(token, tokens))
		if (distanceBetweenSpaces > token.canMoveThisTurn) { throw new IllegalArgumentException("Space is too far away!") }
		
		// let's assume that the spaces don't need to be regened.
		
		
		val newToken = token.copy(
			currentSpace = space,
			canMoveThisTurn = token.canMoveThisTurn - distanceBetweenSpaces
		)
		
		val newTokens = new ListOfTokens(
			tokens.tokens.updated(indexOfToken._1,
				tokens.tokens(indexOfToken._1).updated(indexOfToken._2, newToken)
			)
		)
		
		GameState(board, newTokens)
	}
	
	def tokenAttackDamage(player:Int, attacker:Token, attackee:Token):GameState = {
		
		val indexOfAttacker = this.tokens.indexOf(attacker)
		val indexOfAttackee = this.tokens.indexOf(attackee)
		
		if (indexOfAttacker._1 != player) { throw new IllegalArgumentException("That's not your token!") }
		if (indexOfAttackee._1 == player) { throw new IllegalArgumentException("Player owns attackee") }
		
		val distance = attacker.currentSpace.distanceTo(
				attackee.currentSpace,
				new AttackCostFunction(attacker, tokens)
		)
		
		if (!attacker.canAttackThisTurn) { throw new IllegalArgumentException("Token has already attacked this turn") }
		if (distance > attacker.tokenClass.get.range) { throw new IllegalArgumentException("Tokens are too far away!") }
		
		
		val newAttacker = attacker.copy(
			canAttackThisTurn = false
		)
		val newAttackee = attackee.takeDamage(attacker, tokens)
		
		
		
		val newTokens = new ListOfTokens(
			tokens.tokens.updated(indexOfAttacker._1,
				tokens.tokens(indexOfAttacker._1).updated(indexOfAttacker._2, newAttacker)
			).updated(indexOfAttackee._1,
				tokens.tokens(indexOfAttackee._1).updated(indexOfAttackee._2, newAttackee)
			)
		)
		
		GameState(board, newTokens)
	}
	
	def tokenAttackStatus(player:Int, attacker:Token, attackee:Token):GameState = {
		
		val indexOfAttacker = this.tokens.indexOf(attacker)
		val indexOfAttackee = this.tokens.indexOf(attackee)
		
		if (indexOfAttacker._1 != player) { throw new IllegalArgumentException("That's not your token!") }
		if (indexOfAttackee._1 == player) { throw new IllegalArgumentException("Player owns attackee") }
		
		val distance = attacker.currentSpace.distanceTo(
				attackee.currentSpace,
				new AttackCostFunction(attacker, tokens)
		)
		
		if (!attacker.canAttackThisTurn) { throw new IllegalArgumentException("Token has already attacked this turn") }
		if (distance > attacker.tokenClass.get.range) { throw new IllegalArgumentException("Tokens are too far away!") }
		if (attackee.currentStatus != Statuses.Normal) { throw new IllegalArgumentException("Token already has a status") }
		
		
		val newAttacker = attacker.copy(
			canAttackThisTurn = false
		)
		val newAttackee = attackee.copy(
			currentStatus = attacker.tokenClass.get.atkStatus,
			currentStatusTurnsLeft = 3
		)
		
		
		
		val newTokens = new ListOfTokens(
			tokens.tokens.updated(indexOfAttacker._1,
				tokens.tokens(indexOfAttacker._1).updated(indexOfAttacker._2, newAttacker)
			).updated(indexOfAttackee._1,
				tokens.tokens(indexOfAttackee._1).updated(indexOfAttackee._2, newAttackee)
			)
		)
		
		GameState(board, newTokens)
	}
	
	/* @since a.6.1 */
	def spy(player:Int, spier:Token):GameState = {
		val indexOfSpier = this.tokens.indexOf(spier)
		
		if (indexOfSpier._1 != player) { throw new IllegalArgumentException("That's not your token!") }
		if (!spier.canAttackThisTurn) { throw new IllegalArgumentException("Token has acted this turn; cannot spy") }
		if (spier.canMoveThisTurn != spier.tokenClass.get.speed) { throw new IllegalArgumentException("Token has acted this turn; cannot spy") }
		
		val newSpier = spier.copy(
			canAttackThisTurn = false,
			canMoveThisTurn = 0
		)
		
		val newTokens = new ListOfTokens(
			tokens.tokens.updated(indexOfSpier._1,
				tokens.tokens(indexOfSpier._1).updated(indexOfSpier._2, newSpier)
			)
		)
		
		GameState(board, newTokens)
	}
}

/**
 * @since a.6.0
 */
object GameState {
	sealed trait Action
	
	case class TokenMove(token:Token, space:RectangularSpace[SpaceClass]) extends Action
	case class TokenAttackDamage(attacker:Token, attackee:Token) extends Action
	case class TokenAttackStatus(attacker:Token, attackee:Token) extends Action
	/* @since a.6.1 */
	case class Spy(spier:Token) extends Action
	object EndOfTurn extends Action with Result
	
	
	sealed trait Result {
		/* @since a.6.1 */
		def tellPlayer(player:Int) = true
	}
	
	case class TokenMoveResult(tokenIndex:TokenIndex, space:RectangularSpace[SpaceClass]) extends Result
	case class TokenAttackDamageResult(attackerIndex:TokenIndex, attackeeIndex:TokenIndex, elem:Element, kind:Weaponkind) extends Result
	case class TokenAttackStatusResult(attackerIndex:TokenIndex, attackeeIndex:TokenIndex, status:Status) extends Result
	/* @since a.6.1 */
	case class SpyResult(playerToTell:Int, tokenIndex:TokenIndex, information:ai.TokenClassSuspicion) extends Result {
		override def tellPlayer(player:Int) = {player == playerToTell}
	}
	
}
