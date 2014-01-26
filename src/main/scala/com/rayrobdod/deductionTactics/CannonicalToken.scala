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

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.BodyType
import Directions.Direction
import com.rayrobdod.boardGame.{Space => BoardGameSpace,
		Token => BoardGameToken, RectangularSpace,
		PhysicalStrikeCost, TokenMovementCost}
import java.util.concurrent.ThreadLocalRandom.{current => Random}
import LoggerInitializer.{cannonicalTokenLogger => Logger,
			cannonicalTokenMovementLogger => MovementLogger}
import java.util.logging.Level
import scala.collection.mutable.Buffer

/**
 * A game token.
 * @author Raymond Dodge
 * @version a.5.0 - no longer relies on actors
 */
final class CannonicalToken(val tokenClass:CannonicalTokenClass) extends Token
{
	private var _currentHitpoints:Int = maximumHitpoints
	def currentHitpoints:Int = _currentHitpoints
	
	private var _currentStatus:Option[Status] = None
	def currentStatus:Option[Status] = _currentStatus
	
	private var _currentStatusTurnsLeft:Int = -1
	def currentStatusTurnsLeft:Int = _currentStatusTurnsLeft
	
	private var _canMoveThisTurn:Int = 0
	def canMoveThisTurn = _canMoveThisTurn
	
	private var _canAttackThisTurn:Boolean = false
	def canAttackThisTurn = _canAttackThisTurn
	
	def requestMoveTo(movedTo:BoardGameSpace):Unit = {
		if (this.currentSpace == null) {
			this.currentSpace_=(movedTo, true)
		} else if (canMoveThisTurn > 0) {
			if (currentSpace == movedTo) {
				// nothing to do
			} else if (currentSpace.adjacentSpaces.exists{_ == movedTo}) {
				val movementCost = movedTo.typeOfSpace.cost(CannonicalToken.this, TokenMovementCost)
				
				if (movementCost <= _canMoveThisTurn) {
					this._canMoveThisTurn = this._canMoveThisTurn - movementCost 
					this.currentSpace_=(movedTo, true)
				}
			} else {
				val path = currentSpace.pathTo(movedTo, CannonicalToken.this, TokenMovementCost)
				val adjacentMoveTo = path(1)
				val movementCost = adjacentMoveTo.typeOfSpace.cost(CannonicalToken.this, TokenMovementCost)
				
				if (movementCost <= _canMoveThisTurn) {
					_canMoveThisTurn = _canMoveThisTurn - movementCost
					this.currentSpace_=(adjacentMoveTo, false)
					
					this.requestMoveTo(movedTo)
				} else {
					// can't move to desired space, so give up
				}
			}
		} else {
			// also done
		}
	}
	
	
	override def toString() = "CannonicalToken{tokenClass:" + tokenClass + ";}"
	
	
	/* * * * * * * Player Event Listeners * * * * * * */
	
	/** add to owner Player */
	object TurnStartReaction extends Player.StartTurnReactionType {
		def apply():Unit = {
			_currentStatusTurnsLeft = _currentStatusTurnsLeft - 1
			if (_currentStatusTurnsLeft <= 0) {_currentStatus = None}
			
			if (_currentHitpoints > 0) {
				_canMoveThisTurn = tokenClass.speed.get
				_canAttackThisTurn = true
			}
		}
	}
	/** add to owner Player */
	object TurnEndReaction extends Player.EndTurnReactionType {
		def apply():Unit = {
			_canMoveThisTurn = 0
			_canAttackThisTurn = false
		}
	}
	/** add to owner Player */
	class StatusAct(allTokens:ListOfTokens) extends Player.StartTurnReactionType {
		val moveToRandomSpace = {(s:BoardGameSpace, i:Int) =>
			val possibleNext = s.adjacentSpaces.toSeq
			val next = possibleNext(Random.nextInt(possibleNext.size))
			
			val tokenOnNext = allTokens.aliveTokens.flatten.find{_.currentSpace == next}
			
			CannonicalToken.this.requestMoveTo(next)
			tokenOnNext.foreach{(x:Token) =>
				CannonicalToken.this.tryAttackDamage(x)
			}
			
			next
		}
		
		override def apply() =
		{
			currentStatus match {
				case None => {}
				case Some(Statuses.Sleep) => {
					_canMoveThisTurn = 0
				}
				case Some(Statuses.Snake) => {
					_canMoveThisTurn = 1
					_currentHitpoints = _currentHitpoints - baseDamage
				}
				case Some(Statuses.Blind) => {
					_canAttackThisTurn = false
				}
				case Some(Statuses.Burn) => {
					_currentHitpoints = _currentHitpoints - (2 * baseDamage)
				}
				case Some(Statuses.Confuse) => {
					(1 to 3).foldLeft(currentSpace)(moveToRandomSpace)
				}
				case Some(Statuses.Neuro) => {
					(1 to 1).foldLeft(currentSpace)(moveToRandomSpace)
					_currentHitpoints = _currentHitpoints - baseDamage
				}
				case Some(Statuses.Heal) => {
					_currentHitpoints = _currentHitpoints + (baseDamage)
				}
				case _ => {
					throw new IllegalStateException("I do not know how to respond to a " + currentStatus)
				}
			}
			
			if (_currentHitpoints <= 0) {CannonicalToken.this.triggerDiedReactions()}
		}
		
		override def toString = CannonicalToken.this.toString + ".StatusReaction"
	}
	
	
	/* * * * * * * ATTACKS * * * * * * */
	
	def beAttacked(elem:Element, kind:Weaponkind, from:BoardGameSpace) = {
		Logger.entering("com.rayrobdod.deductionTactics.CannonicalToken",
						"beAttacked", Array(elem, kind, from))
		
		val multiplier = tokenClass.weakWeapon(kind).get *
				(if (currentStatus == tokenClass.weakStatus) {2} else {1}) *
				(directionMultiplier(currentSpace, from)) *
				(tokenClass.atkElement.get.damageModifier(elem));
				
		val damageDone = (if (multiplier > 8) {320} else {baseDamage * multiplier}).intValue
		
		_currentHitpoints = _currentHitpoints - damageDone
		
		this.triggerBeDamageAttackedReactions(elem,kind,damageDone,from)
		this.triggerUpdateReactions
		if (_currentHitpoints <= 0) {CannonicalToken.this.triggerDiedReactions()}
	}
	def beAttacked(status:Status, from:BoardGameSpace) = {
		if (_currentStatus == None) {
			_currentStatus = Some(status)
			_currentStatusTurnsLeft = 3
		}
		this.triggerBeStatusAttackedReactions(status,from)
		this.triggerUpdateReactions
	}
	
	def tryAttackDamage(target:Token) {
		if (this.canAttackThisTurn && this.currentSpace.distanceTo(target.currentSpace, this, PhysicalStrikeCost) <= this.tokenClass.range.get) {
			target.beAttacked(this.tokenClass.atkElement.get, this.tokenClass.atkWeapon.get, this.currentSpace)
			this.triggerUpdateReactions
			this._canAttackThisTurn = false
			tryDamageAttackedReactions.foreach{c => c(target)}
		}
	}
	def tryAttackStatus(target:Token) {
		if (this.canAttackThisTurn && this.currentSpace.distanceTo(target.currentSpace, this, PhysicalStrikeCost) <= this.tokenClass.range.get) {
			target.beAttacked(this.tokenClass.atkStatus.get, this.currentSpace)
			this.triggerUpdateReactions
			this._canAttackThisTurn = false
			tryStatusAttackedReactions.foreach{c => c(target)}
		}
	}
	
	private val tryDamageAttackedReactions:Buffer[CannonicalToken.RequestAttackType] = Buffer.empty;
	def tryDamageAttackedReactions_+=(a:CannonicalToken.RequestAttackType) = {tryDamageAttackedReactions += a}
	def tryDamageAttackedReactions_-=(a:CannonicalToken.RequestAttackType) = {tryDamageAttackedReactions -= a}
	
	private val tryStatusAttackedReactions:Buffer[CannonicalToken.RequestAttackType] = Buffer.empty;
	def tryStatusAttackedReactions_+=(a:CannonicalToken.RequestAttackType) = {tryStatusAttackedReactions += a}
	def tryStatusAttackedReactions_-=(a:CannonicalToken.RequestAttackType) = {tryStatusAttackedReactions -= a}

	
	private def directionMultiplier(hisSpace:BoardGameSpace, mySpace:BoardGameSpace) = {
		import Directions.pathDirections
		
		val path = pathDirections(hisSpace, mySpace)
		val weakDir = CannonicalToken.this.tokenClass.weakDirection.get
		
		weakDir.weaknessMultiplier(path)
	}
}


object CannonicalToken {
	type RequestAttackType = Function1[Token, Unit]
}
