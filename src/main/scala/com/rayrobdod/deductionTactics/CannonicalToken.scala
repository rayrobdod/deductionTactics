package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
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
 * A token on the board
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 19 Jan 2012 - renaming to CanonicalToken
 * @version 19 Jan 2012 - removing permissions checks on methods
 * @version 25 Jan 2012 - modified death - now outsiders can read it. Fixed off by one error in death forwarding
 * @version 12 Feb 2012 - made the canmove/attackthisturn items visible
 * @version 12 Feb 2012 - RequestMove is now a thing
 * @version 14 Feb 2012 - created a StatusReaction, but not sure how to initalize it; also doesn't do damage yet
 * @version 27 Feb 2012 - AttackReaction now handles StatusAttack too.
 * @version 20 Mar 2012 - modified reactions for new event model
 * @version 24 Mar 2012 - Added toString to CannoincalToken that uses only the tokenClass. 
 * @version 24 Mar 2012 - Added toString to all sub-class reactions
 * @version 05 Apr 2012 - {@link BoardGameSpaceClass}.movementCost -> BoardGameSpaceClass.cost and adding {@link TypeOfCost}s to those that now need it in {@link BoardGameSpace}
 * @version 24 Apr 2012 - trying to implement the damage direction multiplier
 * @version 04 Jun 2012 - adding an effect for Status.Heal to the StatusAct object
 * @version 27 Jun 2012 - moving the majority of CannonicalToken.BeAttackedReaction.directionMultiplier's
			implementation to Directions.pathDirections and Directions.Direction.weaknessMultiplier 
 * @version 01 Aug 2012 - Status Act will now send a Died event if applicable
 * @version 01 Aug 2012 - TurnStartReaction will no longer respond if unit is supposed to be dead
 * @version 2013 Aug 07 - complete rewrite to get rid of Actor stuff
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
			
			_canMoveThisTurn = tokenClass.speed.get
			_canAttackThisTurn = true
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
		
		this.triggerDamageAttackedReactions(elem,kind,from)
		this.triggerUpdateReactions
		if (_currentHitpoints <= 0) {CannonicalToken.this.triggerDiedReactions()}
	}
	def beAttacked(status:Status, from:BoardGameSpace) = {
		if (_currentStatus == None) {
			_currentStatus = Some(status)
			_currentStatusTurnsLeft = 3
		}
		this.triggerStatusAttackedReactions(status,from)
		this.triggerUpdateReactions
	}
	
	def tryAttackDamage(target:Token) {
		if (this.currentSpace.distanceTo(target.currentSpace, this, PhysicalStrikeCost) <= this.tokenClass.range.get) {
			target.beAttacked(this.tokenClass.atkElement.get, this.tokenClass.atkWeapon.get, this.currentSpace)
			this.triggerUpdateReactions
			this._canAttackThisTurn = false
			tryDamageAttackedReactions.foreach{c => c(target)}
		}
	}
	def tryAttackStatus(target:Token) {
		if (this.currentSpace.distanceTo(target.currentSpace, this, PhysicalStrikeCost) <= this.tokenClass.range.get) {
			target.beAttacked(this.tokenClass.atkStatus.get, this.currentSpace)
			this.triggerUpdateReactions
			this._canAttackThisTurn = false
			tryStatusAttackedReactions.foreach{c => c(target)}
		}
	}
	
	private val tryDamageAttackedReactions:Buffer[CannonicalToken.RequestAttackType] = Buffer.empty;
	def addTryDamageAttackedReaction(a:CannonicalToken.RequestAttackType) = {tryDamageAttackedReactions += a}

	private val tryStatusAttackedReactions:Buffer[CannonicalToken.RequestAttackType] = Buffer.empty;
	def addTryStatusAttackedReaction(a:CannonicalToken.RequestAttackType) = {tryStatusAttackedReactions += a}

	
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
