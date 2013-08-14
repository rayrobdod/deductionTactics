package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{Space, RectangularSpace,
		PhysicalStrikeCost, TokenMovementCost}
import com.rayrobdod.boardGame.{RectangularField => Field}
import com.rayrobdod.deductionTactics.Statuses.Sleep
import com.rayrobdod.deductionTactics.PlayerAI.teamSize
import scala.collection.immutable.Set
import LoggerInitializer.{sleepAbuserAILogger => Logger}
import scala.math.Ordering
import scala.util.Random

/**
 * An AI that will use the sleep status to its advantage
 *
 * @author Raymond Dodge
 * @version 29 Feb 2012
 * @version 21 Mar 2012 - modified reactions for new event model
 * @version 05 Apr 2012 - adding TokenCosts to Space and SpaceClass methods that now require them
 * @version 08 Apr 2012 - changed initialize to use new StandardObserveAttacks and StandardObserveMovement
 * @version 28 Apr 2012 - gutting in hopes of complete rewrite
 * @version 28 Apr 2012 - adding speedRangeOf and attackRangeOf
 * @version 30 May 2012 - moved speedRangeOf and attackRangeOf to the package object
 * @version 2013 Aug 07 - ripples from rewriting Player
 */
class SleepAbuserAI extends PlayerAI
{
	/**
	 * @pre all my tokens have the sleep atkStatus
	 * @pre my alive tokens outnumber or equal to his alive tokens
	 */
	def takeTurn(player:Player):Any = 
	{
		val tokens = player.tokens
		val aliveEnemies = tokens.aliveOtherTokens.flatten
		
		val enemyAttackRange = aliveEnemies.map(attackRangeOf).flatten
		
		tokens.aliveMyTokens.foreach{(myToken:CannonicalToken) =>
			
			val myRange = attackRangeOf(myToken)
			val myReach = moveRangeOf(myToken)
			val attackableOtherTokens = aliveEnemies.filter{myRange contains _.currentSpace}
			
			if (attackableOtherTokens.isEmpty)
			{	// wants to be out of enemy range
				retreatFromEnemy(player, myToken, enemyAttackRange)
			}
			else
			{	// wants to attack enemy
				moveToAndStrikeEnemy(player, myToken, attackableOtherTokens)
				retreatFromEnemy(player, myToken, enemyAttackRange)
			}
		}
	}
	
	def buildTeam = {
		val allWithSleep = CannonicalTokenClass.allKnown.filter{_.atkStatus.get == Sleep}
		
		Random.shuffle(allWithSleep ++ allWithSleep).take(teamSize)
	}
	
	def initialize(player:Player, field:Field) = {
		player.tokens.otherTokens.flatten.foreach{(token:MirrorToken) =>
			token.addDamageAttackedReaction(new StandardObserveAttacks(token, player.tokens))
			token.addStatusAttackedReaction(new StandardObserveAttacks(token, player.tokens))
			
			val movement = new StandardObserveMovement(token)
			token.addMoveReaction(movement)
			player.addStartTurnReaction(movement)
		}
	}
	
	override def toString = this.getClass.getName
	
	
	
	
	
	def fleeSpace(currentSpace:Space, fleeFrom:Space):Space =
	{
		currentSpace match {
			case rs:RectangularSpace => {
				if (fleeFrom == rs.down) {rs.up.get}
				else if (fleeFrom == rs.up) {rs.down.get}
				else if (fleeFrom == rs.right) {rs.left.get}
				else if (fleeFrom == rs.left) {rs.right.get}
				// default option, if somehow an adjacent space isn't adjacent
				else {rs.adjacentSpaces.head}
			}
		}
	}
	
	/**  a subroutine of the takeTurn method */
	def retreatFromEnemy(player:Player, myToken:CannonicalToken, enemyRange:Seq[Space])
	{
		Logger.entering("com.rayrobdod.deductionTactics.ai.SleepAbuserAI",
				"retreatFromEnemy", Seq(player, myToken, "enemyRange"))
		
		val myMoveRange = moveRangeOf(myToken)
		val safeZone = myMoveRange -- enemyRange
		
		{
			val targetableByToSpace = myMoveRange.groupBy{(rangeSpace:Space) => 
				player.tokens.aliveOtherTokens.flatten.count{attackRangeOf(_) contains rangeSpace}
			}
			Logger.finer("Zones: " + targetableByToSpace)
		}
		
		val moveTo = if (safeZone.isEmpty) {
			// is in as few token's ranges as possible
			val targetableByToSpace = myMoveRange.groupBy{(rangeSpace:Space) => 
				player.tokens.aliveOtherTokens.flatten.count{attackRangeOf(_) contains rangeSpace}
			}
			Logger.finer("Zones: " + targetableByToSpace)
			val safestZone = targetableByToSpace.minBy{_._1}._2
			Logger.finer("Safest Zone Number: " + targetableByToSpace.minBy{_._1}._1)
			
			
			safestZone.minBy{(mySpace:Space) =>
				val closestToken = player.tokens.aliveOtherTokens.flatten.minBy{(hisToken:Token) =>
					mySpace.distanceTo(hisToken.currentSpace, hisToken, TokenMovementCost)
				}
				
				mySpace.distanceTo(closestToken.currentSpace, closestToken, TokenMovementCost)
			}
			
		} else {
			// safezone space closest to any enemy token
			Logger.finer("Safe Zone")
			
			safeZone.minBy{(mySpace:Space) =>
				val closestToken = player.tokens.aliveOtherTokens.flatten.minBy{(hisToken:Token) =>
					mySpace.distanceTo(hisToken.currentSpace, hisToken, TokenMovementCost)
				}
				
				mySpace.distanceTo(closestToken.currentSpace, closestToken, TokenMovementCost)
			}
		}
		
		myToken.requestMoveTo(moveTo)
		
	}
	
	/**  a subroutine of the takeTurn method */
	def moveToAndStrikeEnemy(player:Player, myToken:CannonicalToken, attackableOtherTokens:Seq[Token])
	{
		val awakeAttackableOtherTokens = attackableOtherTokens.filter{_.currentStatus != Some(Sleep)}
		Logger.finer("Attackable: " + attackableOtherTokens)
		Logger.finer("AwakeAttackable: " + awakeAttackableOtherTokens)
			
		val targetChoices = if (!awakeAttackableOtherTokens.isEmpty) {awakeAttackableOtherTokens} else {attackableOtherTokens}
		val target = targetChoices.minBy{(x:Token) => myToken.currentSpace.distanceTo(x.currentSpace, myToken, TokenMovementCost)}
		Logger.finer("target: " + target)
		
		val moveToChoices = moveRangeOf(myToken).filter{(x:Space) => 
			x.spacesWithin(myToken.tokenClass.range.get, myToken, PhysicalStrikeCost) contains target.currentSpace
		} // choose item closest to me while furthest from target.
		val moveTo = moveToChoices.maxBy{(x:Space) =>
			x.distanceTo(target.currentSpace, myToken, PhysicalStrikeCost) -
			myToken.currentSpace.distanceTo(x, myToken, TokenMovementCost)
		}
		Logger.finer("moveTo: " + moveTo + "; distance to enemy: " + 
				moveTo.distanceTo(target.currentSpace, myToken, PhysicalStrikeCost) +
				"; distance from self: " +
				myToken.currentSpace.distanceTo(moveTo, myToken, TokenMovementCost))
		
		myToken.requestMoveTo(moveTo)
		if (target.currentStatus == None)
			myToken.tryAttackStatus(target)
		else
			myToken.tryAttackDamage(target)
	}
}