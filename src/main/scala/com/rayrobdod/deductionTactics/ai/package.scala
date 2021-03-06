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
// http://en.wikipedia.org/wiki/Blackboard_system

import BodyTypes.BodyType
import Directions.Direction
import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status

import scala.util.Random
import scala.collection.immutable.{Seq, Map}
import com.rayrobdod.boardGame.RectangularSpace

/**
 * @author Raymond Dodge
 * @version a.6.0
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
	def attackRangeOf(token:Token, list:ListOfTokens, susp:TokenClassSuspicion = new TokenClassSuspicion()):Set[RectangularSpace[SpaceClass]] =
	{
		if (token.currentStatus == Statuses.Blind) {
			Set.empty
		} else {
			
			val speedSpaces = moveRangeOf(token, list, susp)
			val tokenRange = token.tokenClass.map{_.range}.getOrElse(susp.range.getOrElse(0))
		
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
	def moveRangeOf(token:Token, list:ListOfTokens, susp:TokenClassSuspicion = new TokenClassSuspicion()):Set[RectangularSpace[SpaceClass]] =
	{
		val statusSpeedLimit = token.currentStatus match {
			case Statuses.Snake => 1
			case Statuses.Sleep => 0
			case _ => 1000
		}
		
		val startSpace = token.currentSpace
		val tokenSpeed = math.min(statusSpeedLimit,
				token.tokenClass.map{_.speed}.getOrElse(susp.speed.getOrElse(0))
		)
		
		startSpace.spacesWithin(
				tokenSpeed, 
				new MoveToCostFunction(token, list)
		).toSet
	}
}

package ai {
	
	
	/** @version a.6.0 */
	trait Memo {
		def attacks:Seq[GameState.Result]
		def suspicions:Map[(Int, Int), TokenClassSuspicion]
		def addAttack(r:GameState.Result):Memo
		def updateSuspicion(key:(Int, Int), value:TokenClassSuspicion):Memo
	}
	
	
	/** @version a.6.0 */
	final case class SimpleMemo(
		val attacks:Seq[GameState.Result] = Nil,
		val suspicions:Map[(Int, Int), TokenClassSuspicion] = Map.empty.withDefaultValue(new TokenClassSuspicion)
	) extends Memo {
		def addAttack(r:GameState.Result):SimpleMemo =
				new SimpleMemo(r +: attacks, suspicions)
		def updateSuspicion(key:(Int, Int), value:TokenClassSuspicion):SimpleMemo =
				new SimpleMemo(attacks, suspicions + ((key, value)))
	}
	
	/** @version a.6.0 */
	case class TokenClassSuspicion(
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
	
	/** @since a.6.0 */
	class DecoratorPlayerAI(base:PlayerAI) extends PlayerAI {
		override def selectTokenClasses(maxResultSize:Int):Seq[TokenClass] = base.selectTokenClasses(maxResultSize)
		
		override def narrowTokenClasses(
				otherPlayersSelectedClasses:Seq[Seq[TokenClass]],
				maxResultSize:Int,
				myIndex:Int
		):Seq[TokenClass] = base.narrowTokenClasses(
				otherPlayersSelectedClasses,
				maxResultSize,
				myIndex
		)
		
		override def takeTurn(
				player:Int,
				gameState:GameState,
				memo:ai.Memo
		):Seq[GameState.Action] = base.takeTurn(player, gameState, memo)
		
		override def notifyTurn(
			player:Int,
			action:GameState.Result,
			beforeState:GameState,
			afterState:GameState,
			memo:ai.Memo
		):ai.Memo = base.notifyTurn(
			player,
			action,
			beforeState,
			afterState,
			memo
		)
		
		override def initialize(player:Int, initialState:GameState):ai.Memo = base.initialize(player, initialState)
	}
}
