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

import com.rayrobdod.boardGame.{Space, RectangularSpace}
import com.rayrobdod.boardGame.{RectangularField => Field}
import com.rayrobdod.deductionTactics.Statuses.Sleep
import scala.collection.immutable.{Seq, Set, Map}
import LoggerInitializer.{sleepAbuserAILogger => Logger}
import scala.math.Ordering
import scala.util.Random

/**
 * An AI that will use the sleep status to its advantage
 *
 * The basic idea behind this strategy is
 *   1. Approach cautiously
 *   2. Strike with a Sleep status when given the opportunity
 *   3. Keep out of enemy's range, and attack each turn until sleep wears off
 *   *. Repeat
 *
 * @author Raymond Dodge
 * @version a.5.0
 */
class SleepAbuserAI extends PlayerAI
{
	/**
	 * @pre all my tokens have the sleep atkStatus
	 * @pre my alive tokens outnumber or equal to his alive tokens
	 */
	def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
		val tokens = gameState.tokens
		val aliveEnemies = tokens.aliveNotPlayerTokens(player).flatten
		val enemyAttackRange = aliveEnemies.map{x => attackRangeOf(x, tokens)}
		
		Logger.finer("Enemy Range: " + enemyAttackRange.size)
		
		tokens.alivePlayerTokens(player).flatMap{(myToken:Token) =>
			
			val myRange = attackRangeOf(myToken, tokens)
			val myReach = moveRangeOf(myToken, tokens)
			val attackableOtherTokens = aliveEnemies.filter{(x:Token) => myRange contains x.currentSpace}
			
			if (attackableOtherTokens.isEmpty)
			{	// wants to be out of enemy range
				retreatFromEnemy(player, myToken, tokens, enemyAttackRange.flatten)
			}
			else
			{	// wants to attack enemy
				moveToAndStrikeEnemy(myToken, attackableOtherTokens, tokens) ++:
				retreatFromEnemy(player, myToken, tokens, enemyAttackRange.flatten)
			}
		} :+ GameState.EndOfTurn
	}
	
	def buildTeam(teamSize:Int) = {
		val allWithSleep = TokenClass.allKnown.filter{_.atkStatus == Sleep}
		
		Random.shuffle(allWithSleep ++ allWithSleep).take(teamSize)
	}
	
	override def initialize(player:Int, initialState:GameState):Memo = new SimpleMemo()
	
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = memo
	
	override def toString = this.getClass.getName
	
	
	
	
	
	def fleeSpace(currentSpace:Space[SpaceClass], fleeFrom:Space[SpaceClass]):Space[SpaceClass] =
	{
		currentSpace match {
			case rs:RectangularSpace[_] => {
				if (fleeFrom == rs.down) {rs.up.get}
				else if (fleeFrom == rs.up) {rs.down.get}
				else if (fleeFrom == rs.right) {rs.left.get}
				else if (fleeFrom == rs.left) {rs.right.get}
				// default option, if somehow an adjacent space isn't adjacent
				else {rs.adjacentSpaces.head}
			}
		}
	}
	
	/** returns a sequence of actions that will cause a token to retreat to a safe space */
	def retreatFromEnemy(player:Int, myToken:Token, tokens:ListOfTokens, enemyRange:Seq[Space[SpaceClass]]):Seq[GameState.Action] = 
	{
		Logger.entering("com.rayrobdod.deductionTactics.ai.SleepAbuserAI",
				"retreatFromEnemy", Seq(myToken.tokenClass.get.name, "tokens{}", "enemyRange"))
		
		val myMoveRange = moveRangeOf(myToken, tokens)
		val safeZone = myMoveRange -- enemyRange
		
		Logger.finer("myMoveRange: " + myMoveRange.size)
		
		{
			val targetableByToSpace = myMoveRange.groupBy{(rangeSpace:Space[SpaceClass]) =>
				tokens.aliveNotPlayerTokens(player).flatten.count{attackRangeOf(_, tokens) contains rangeSpace}
			}
			Logger.finer("Zones: " + targetableByToSpace)
		}
		
		val moveTo = if (safeZone.isEmpty) {
			// is in as few token's ranges as possible
			val targetableByToSpace = myMoveRange.groupBy{(rangeSpace:Space[SpaceClass]) => 
				tokens.aliveNotPlayerTokens(player).flatten.count{attackRangeOf(_, tokens) contains rangeSpace}
			}
			Logger.finer("Zones: " + targetableByToSpace)
			val safestZone = targetableByToSpace.minBy{_._1}._2
			Logger.finer("Safest Zone Number: " + targetableByToSpace.minBy{_._1}._1)
			
			
			safestZone.minBy{(mySpace:Space[SpaceClass]) =>
				val closestToken = tokens.aliveNotPlayerTokens(player).flatten.minBy{(hisToken:Token) =>
					mySpace.distanceTo(hisToken.currentSpace, new MoveToCostFunction(myToken, tokens))
				}
				
				mySpace.distanceTo(closestToken.currentSpace, new MoveToCostFunction(myToken, tokens))
			}
			
		} else {
			// safezone space closest to any enemy token
			Logger.finer("Safe Zone")
			
			safeZone.minBy{(mySpace:Space[SpaceClass]) =>
				val closestToken = tokens.aliveNotPlayerTokens(player).flatten.minBy{(hisToken:Token) =>
					mySpace.distanceTo(hisToken.currentSpace, new MoveToCostFunction(myToken, tokens))
				}
				
				mySpace.distanceTo(closestToken.currentSpace, new MoveToCostFunction(myToken, tokens))
			}
		}
		
		Seq( GameState.TokenMove(myToken, moveTo) )
	}
	
	/** returns a sequence of actions that will cause a token to attack an enemy token */
	def moveToAndStrikeEnemy(myToken:Token, attackableOtherTokens:Seq[Token], allTokens:ListOfTokens):Seq[GameState.Action] = 
	{
		val awakeAttackableOtherTokens = attackableOtherTokens.filter{_.currentStatus != Sleep}
		Logger.finer("Attackable: " + attackableOtherTokens)
		Logger.finer("AwakeAttackable: " + awakeAttackableOtherTokens)
			
		val targetChoices = if (!awakeAttackableOtherTokens.isEmpty) {awakeAttackableOtherTokens} else {attackableOtherTokens}
		val target = targetChoices.minBy{(x:Token) => myToken.currentSpace.distanceTo(x.currentSpace, new MoveToCostFunction(myToken, allTokens))}
		Logger.finer("target: " + target)
		
		val moveToChoices = moveRangeOf(myToken, allTokens).filter{(x:Space[SpaceClass]) =>
			x.spacesWithin(myToken.tokenClass.get.range, new AttackCostFunction(myToken, allTokens)) contains target.currentSpace
		} // choose item closest to me while furthest from target.
		val moveTo = moveToChoices.maxBy{(x:Space[SpaceClass]) =>
			x.distanceTo(target.currentSpace, new AttackCostFunction(myToken, allTokens)) -
			myToken.currentSpace.distanceTo(x, new AttackCostFunction(myToken, allTokens))
		}
		Logger.finer("moveTo: " + moveTo + "; distance to enemy: " + 
				moveTo.distanceTo(target.currentSpace, new AttackCostFunction(myToken, allTokens)) +
				"; distance from self: " +
				myToken.currentSpace.distanceTo(moveTo, new MoveToCostFunction(myToken, allTokens)))
		
		
		Seq( 
			GameState.TokenMove(myToken, moveTo),
			if (target.currentStatus == Statuses.Normal) {
				GameState.TokenAttackStatus(myToken, target)
			} else {
				GameState.TokenAttackDamage(myToken, target)
			}
		)
	}
}
