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
	object attackRangeOf 
	{
		def apply(token:Token, list:ListOfTokens) =
		{
			val speedSpaces = moveRangeOf(token, list)
			
			val tokenRange = token.tokenClass.map{_.range}.getOrElse(0)
			val rangeSpaces = speedSpaces.map{_.spacesWithin(
					tokenRange,
					new AttackCostFunction(token, list)
			)}.flatten
			
			rangeSpaces
		}
	}

	/**
	 * determines the spaces a token can move to
	 * @version a.6.0
	 */
	object moveRangeOf 
	{
		def apply(token:Token, list:ListOfTokens) =
		{
			val startSpace = token.currentSpace
			
			val tokenSpeed = token.tokenClass.map{_.speed}.filter{(x:Int) => token.currentStatus.exists{_ == Sleep}}.getOrElse(0)
			System.out.println(tokenSpeed)
			val speedSpaces = startSpace.spacesWithin(
					tokenSpeed, 
					new MoveToCostFunction(token, list)
			).toSet
			
			speedSpaces
		}
	}
	
	
	/** @version a.6.0 */
	case class Blackboard(
		val attacks:Seq[GameState.Result],
		val finalConclusions:Map[(Int, Int), TokenClassSuspision]
	)
	
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
