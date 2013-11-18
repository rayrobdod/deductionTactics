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
// http://en.wikipedia.org/wiki/Blackboard_system

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.{Status, Sleep}

import scala.util.Random
import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.PlayerAI.teamSize
import com.rayrobdod.boardGame.{PhysicalStrikeCost,
			Space, TokenMovementCost}
import com.rayrobdod.deductionTactics.LoggerInitializer.{
				observeMovementLogger => somLogger}
import scala.runtime.{AbstractFunction1 => Function1}

/**
 * @author Raymond Dodge
 * @version 24 Jan 2012
 * @version 25 Mar 2012 - moved StandardObserveAttacks from com.rayrobdod.deductionTactics.PlayerAI
 * @version 08 Apr 2012 - adding StandardObserveMovement
 * @version 08 Apr 2012 - StandardObserveAttacks now is one-per-token and uses the Token as a parameter rather than an entire Token List
 * @version 08 Apr 2012 - StandardObserveAttacks now observes range too.
 * @version 30 May 2012 - moved speedRangeOf and attackRangeOf from SleepAbuserAI
 * @version 2013 Jan 18 - adding logging to StandardObserveMovement
 * @version 2013 Jan 18 - speedRangeOf and attackRangeOf now contains no overlap in functionality
 * @version 2013 Aug 07 - ripples from rewriting Player
 */
package object ai
{
	/**
	 * Chooses a team of token classes by shuffling the list of all
	 * possible tokens and selecting the first howevermany.
	 * @param random the RNG to use.
	 */
	def randomTeam(random:Random):Seq[CannonicalTokenClass] =
	{
		random.shuffle(CannonicalTokenClass.allKnown).take(teamSize)
	}
	
	/**
	 * Chooses a team of token classes by shuffling the list of all
	 * possible tokens and selecting the first howevermany
	 */
	def randomTeam():Seq[CannonicalTokenClass] = randomTeam(Random)
	
	/**
	 * A standard attack observer that will update a SuspiciounsTokenClass when a token attacks
	 * 
	 * Might only need one per player. Add to enemy mirror tokens.
	 */
	class StandardObserveAttacks(target:MirrorToken, allTokens:ListOfTokens)
			extends Token.StatusAttackedReactionType with Token.DamageAttackedReactionType
	{
		def apply(status:Status, attackerSpace:Space) = {
			val targetSpace = target.currentSpace
			val attacker = allTokens.tokens.flatten.find{_.currentSpace == attackerSpace}
			
			attacker match {
				case Some(attacker2:MirrorToken) => {
					val range = attackerSpace.distanceTo(targetSpace, attacker2, PhysicalStrikeCost)
					val attackerClass = attacker2.tokenClass
					
					attackerClass.atkStatus = Some(status)
					
					if (range > attackerClass.range.getOrElse(0)) {
						attackerClass.range = Some(range)
					}
				}
				case _ => {}
			}
		}
		
		def apply(element:Element, kind:Weaponkind, attackerSpace:Space) = {
			val targetSpace = target.currentSpace
			val attacker = allTokens.tokens.flatten.find{_.currentSpace == attackerSpace}
			
			attacker match {
				case Some(attacker2:MirrorToken) => {
					val range = attackerSpace.distanceTo(targetSpace, attacker2, PhysicalStrikeCost)
					val attackerClass = attacker2.tokenClass
					
					attackerClass.atkElement = Some(element)
					attackerClass.atkWeapon = Some(kind)
					
					if (range > attackerClass.range.getOrElse(0)) {
						attackerClass.range = Some(range)
					}
				}
				case _ => {}
			}
		}
	}
	
	/**
	 * A standard movement observer that will update a SuspiciounsTokenClass when a token moves
	 * 
	 * One per enemy token. Parameter is that token. Add to that token and at least one player.
	 */
	class StandardObserveMovement(token:MirrorToken)
				extends Function2[Space, Boolean, Unit] with Function0[Unit]
	{
		private var countThisTurn = 0;
		
		override def apply():Unit = {
				countThisTurn = countThisTurn + 1
				somLogger.finer("Incremented token's movement");
		}
		
		override def apply(e:Space, b:Boolean) = {
				if (countThisTurn > token.tokenClass.speed.getOrElse(0))
				{
					token.tokenClass.speed = Some(countThisTurn)
					somLogger.fine("Recording token's movement: " + countThisTurn);
				}
				countThisTurn = 0;
		}
	}

	
	
	
	
	// TODO: other statuses can affect movement range or attack range
	
	/** determines the spaces a token can attack */
	object attackRangeOf extends Function1[Token, Set[Space]] 
	{
		def apply(token:Token) =
		{
			val speedSpaces = moveRangeOf(token)
			
			val tokenRange = token.tokenClass.range.getOrElse(0)
			val rangeSpaces = speedSpaces.map{_.spacesWithin(tokenRange, token, PhysicalStrikeCost)}.flatten
			
			rangeSpaces
		}
	}

	/** determines the spaces a token can move to */
	object moveRangeOf extends Function1[Token, Set[Space]] 
	{
		def apply(token:Token) =
		{
			val startSpace = token.currentSpace
			
			val tokenSpeed = token.tokenClass.speed.filter{(x:Int) => token.currentStatus.exists{_ == Sleep}}.getOrElse(0)
			val speedSpaces = token.currentSpace.spacesWithin(tokenSpeed, token, TokenMovementCost).toSet
			
			speedSpaces
		}
	}
}
