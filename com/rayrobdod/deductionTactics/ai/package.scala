package com.rayrobdod.deductionTactics

import scala.util.Random
import scala.collection.immutable.Seq
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import com.rayrobdod.deductionTactics.PlayerAI.teamSize
import com.rayrobdod.boardGame.{Moved, StartOfTurn,
		PhysicalStrikeCost, Space, TokenMovementCost}
import com.rayrobdod.deductionTactics.Statuses.Sleep

/**
 * @author Raymond Dodge
 * @version 24 Jan 2012
 * @version 25 Mar 2012 - moved StandardObserveAttacks from com.rayrobdod.deductionTactics.PlayerAI
 * @version 08 Apr 2012 - adding StandardObserveMovement
 * @version 08 Apr 2012 - StandardObserveAttacks now is one-per-token and uses the Token as a parameter rather than an entire Token List
 * @version 08 Apr 2012 - StandardObserveAttacks now observes range too.
 * @version 30 May 2012 - moved speedRangeOf and attackRangeOf from SleepAbuserAI
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
	class StandardObserveAttacks(attacker:MirrorToken) extends Reaction
	{
		attacker.reactions += this;
		
		override def apply(e:Event) = {
			val attackerSpace = e match {
				case damage:AttackForDamage => damage.from
				case status:AttackForStatus => status.from
			}
			val target = e match {
				case damage:AttackForDamage => damage.target
				case status:AttackForStatus => status.target
			}
			val targetSpace = target.currentSpace
			
			val range = attackerSpace.distanceTo(targetSpace, attacker, PhysicalStrikeCost)
			
			val attackerClass = attacker.tokenClass
			
			e match {
				case damage:AttackForDamage => {
					attackerClass.atkElement = Some(damage.element)
					attackerClass.atkWeapon = Some(damage.kind)
				}
				case status:AttackForStatus => {
					attackerClass.atkStatus = Some(status.status)
				}
			}
			if (range > attackerClass.range.getOrElse(0))
			{
				attackerClass.range = Some(range)
			}
		}
		
		override def isDefinedAt(e:Event) = {e match {
			case x:AttackForDamage => true
			case x:AttackForStatus => true
			case _ => false
		}}
	}
	
	/**
	 * A standard movement observer that will update a SuspiciounsTokenClass when a token moves
	 * 
	 * One per enemy token. Parameter is that token. Add to that token and at least one player.
	 */
	class StandardObserveMovement(token:MirrorToken) extends Reaction
	{
		private var countThisTurn = 0;
		
		override def apply(e:Event) = {e match {
			case x:Moved => {
				countThisTurn = countThisTurn + 1
			}
			case StartOfTurn => {
				if (countThisTurn > token.tokenClass.speed.getOrElse(0))
				{
					token.tokenClass.speed = Some(countThisTurn)
				}
				countThisTurn = 0;
			}
		}}
		
		override def isDefinedAt(e:Event) = {e match {
			case x:Moved => true
			case StartOfTurn => true
			case _ => false
		}}
	}

	
	
	
	
	// TODO: other statuses can affect movement range or attack range
	
	/** determines the spaces a token can attack */
	object attackRangeOf extends Function1[Token, Set[Space]] 
	{
		def apply(token:Token) =
		{
			val startSpace = token.currentSpace
			
			val tokenSpeed = if (token.currentStatus != Some(Sleep)) {token.tokenClass.speed.getOrElse(0)} else {0}
			val tokenRange = token.tokenClass.range.getOrElse(0)
			
			val speedSpaces = token.currentSpace.spacesWithin(tokenSpeed, token, TokenMovementCost).toSet
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
			
			val tokenSpeed = if (token.currentStatus != Some(Sleep)) {token.tokenClass.speed.getOrElse(0)} else {0}
			val tokenRange = token.tokenClass.range.getOrElse(0)
			
			val speedSpaces = token.currentSpace.spacesWithin(tokenSpeed, token, TokenMovementCost).toSet
			//val rangeSpaces = speedSpaces.map{_.spacesWithin(tokenRange, token, PhysicalStrikeCost)}.flatten
			
			speedSpaces
		}
	}
}
