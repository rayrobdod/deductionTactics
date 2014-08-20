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

import BodyTypes.BodyType
import Directions.Direction
import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.{Status, Sleep}

import scala.util.Random
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{Space}
import com.rayrobdod.deductionTactics.LoggerInitializer.{
				observeMovementLogger => somLogger}
import scala.runtime.{AbstractFunction1 => Function1, AbstractFunction2 => AFunction2}

/**
 * @author Raymond Dodge
 * @version a.5.0
 */
package object ai
{
	/**
	 * Chooses a team of token classes by shuffling the list of all
	 * possible tokens and selecting the first howevermany.
	 * @param random the RNG to use.
	 * @version a.6.0
	 */
	def randomTeam(teamSize:Int, random:Random):Seq[TokenClass] =
	{
		random.shuffle(TokenClass.allKnown).take(teamSize)
	}
	
	/**
	 * Chooses a team of token classes by shuffling the list of all
	 * possible tokens and selecting the first howevermany
	 * @version a.6.0
	 */
	def randomTeam(teamSize:Int):Seq[TokenClass] = randomTeam(teamSize, Random)
	
	
	
	
	
	// TODO: other statuses can affect movement range or attack range
	
	/**
	 * determines the spaces a token can attack
	 * @version a.6.0
	 */
	def attackRangeOf(token:Token, list:ListOfTokens):Set[Space[SpaceClass]] =
	{
		if (token.currentStatus == Statuses.Blind) {
			Set.empty
		} else {
			
			val speedSpaces = moveRangeOf(token, list)
			val tokenRange = token.tokenClass.map{_.range}.getOrElse(0)
		
			speedSpaces.map{_.spacesWithin(
				tokenRange,
				new AttackCostFunction(token, list)
			)}.flatten
		}
	}

	/**
	 * determines the spaces a token can move to
	 * @version a.6.0
	 */
	def moveRangeOf(token:Token, list:ListOfTokens):Set[Space[SpaceClass]] =
	{
		def statusSpeedLimit = token.currentStatus match {
			case Statuses.Snake => 1
			case Statuses.Sleep => 0
			case _ => 1000
		}
		
		val startSpace = token.currentSpace
		val tokenSpeed = math.min(statusSpeedLimit,
				token.tokenClass.map{_.speed}.getOrElse(0)
		)
		
		startSpace.spacesWithin(
				tokenSpeed, 
				new MoveToCostFunction(token, list)
		).toSet
	}
	
	
	/** @version a.6.0 */
	trait Memo {
		def attacks:Seq[GameState.Result]
		def suspisions:Map[(Int, Int), TokenClassSuspision]
		def addAttack(r:GameState.Result):Memo
		def updateSuspision(key:(Int, Int), value:TokenClassSuspision):Memo
	}
	
	
	/** @version a.6.0 */
	final class SimpleMemo(
		val attacks:Seq[GameState.Result] = Nil,
		val suspisions:Map[(Int, Int), TokenClassSuspision] = Map.empty
	) extends Memo {
		def addAttack(r:GameState.Result):SimpleMemo =
				new SimpleMemo(r +: attacks, suspisions)
		def updateSuspision(key:(Int, Int), value:TokenClassSuspision):SimpleMemo =
				new SimpleMemo(attacks, suspisions + ((key, value)))
	}
	
	/** @version a.6.0 */
	case class TokenClassSuspision(
		val body:Option[BodyType] = None,
		val atkElement:Option[Element] = None,
		val atkWeapon:Option[Weaponkind] = None,
		val atkStatus:Option[Status] = None,
		val range:Option[Int] = None,
		val speed:Option[Int] = None,
		
		val weakDirection:Option[Direction] = None,
		val weakWeapon:Map[Weaponkind,Option[Float]] = Weaponkinds.values.map{((_, None))}.toMap,
		val weakStatus:Option[Status] = None
	)
}
