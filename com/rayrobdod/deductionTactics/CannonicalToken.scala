package com.rayrobdod.deductionTactics

import scala.swing.event.Event
import scala.swing.Reactions.Reaction
import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction
import com.rayrobdod.boardGame.{Space => BoardGameSpace,
		Token => BoardGameToken, RectangularSpace, StartOfTurn,
		EndOfTurn, Moved, PhysicalStrikeCost, TokenMovementCost}
import java.util.concurrent.ThreadLocalRandom.{current => Random}
import LoggerInitializer.{cannonicalTokenLogger => Logger,
			cannonicalTokenMovementLogger => MovementLogger}
import java.util.logging.Level

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
 */
class CannonicalToken(val tokenClass:CannonicalTokenClass) extends Token
{
	type Space = RectangularSpace
	
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
	
	
	override def toString() = "CannonicalToken{tokenClass:" + tokenClass + ";}"
	
	/** add to all enemy tokens */
	class BeAttackedReaction(myMirror:Token) extends Reaction
	{
		override def apply(e:Event) = {e match {
			// first item must be "this" if Reaction contract is being upheld
			case AttackForDamage(_, elem:Element, kind:Weaponkind, from:BoardGameSpace) =>
			{
				Logger.entering("com.rayrobdod.deductionTactics.CannonicalToken.BeAttackedReacion",
						"apply", e)
				
				val multiplier = tokenClass.weakWeapon.get(kind) *
					(if (currentStatus == tokenClass.weakStatus) {2} else {1}) *
					(directionMultiplier(currentSpace, from)) *
					(tokenClass.atkElement.get.damageModifier(elem));
					
				val damageDone = (if (multiplier > 8) {320} else {baseDamage * multiplier}).intValue
				
				_currentHitpoints = _currentHitpoints - damageDone
				
				if (_currentHitpoints <= 0) {CannonicalToken.this ! Died()}
			}
			case AttackForStatus(_, stat:Status, from:BoardGameSpace) =>
			{
				if (_currentStatus == None)
				{
					_currentStatus = Some(stat)
					_currentStatusTurnsLeft = 3
				}
			}
		}}
		
		override def isDefinedAt(e:Event) = {e match {
			case AttackForDamage(target:Token ,_,_,_) => (target == myMirror /* || target == CannonicalToken.this */)
			case AttackForStatus(target:Token ,_,_) => (target == myMirror /* || target == CannonicalToken.this */)
			case _ => false
		}}
		
		override def toString = CannonicalToken.this.toString + ".BeAttackedReaction"
		
		def directionMultiplier(hisSpace:BoardGameSpace, mySpace:BoardGameSpace) = {
			val path:Seq[BoardGameSpace] = hisSpace.pathTo(mySpace, null, PhysicalStrikeCost)
			
			val directionOfPath = path.zip(path.head +: path).map({(next:BoardGameSpace, curr:BoardGameSpace) =>
				curr match {
					case currRect:RectangularSpace => {
						val candidates = Directions.values.map{
								_.function(currRect)}.zip(Directions.values).toMap
						
						candidates.getOrElse(Some(next), null)
					}
					case _ => null
				}
			}.tupled)
			
			Logger.finer(directionOfPath.toString)
			
			val weakDir = CannonicalToken.this.tokenClass.weakDirection.get
			val strngDir = Directions((weakDir.id + 2) % 4)
			val othogDir1 = Directions((weakDir.id + 1) % 4)
			val othogDir2 = Directions((weakDir.id + 3) % 4)
			
			val parelCount = directionOfPath.count(_ == weakDir) -
					directionOfPath.count(_ == strngDir)
			val orthoCount = directionOfPath.count(_ == othogDir1) -
					directionOfPath.count(_ == othogDir2)
			
			import java.lang.Math.{atan2, PI, abs}		
			val theta = abs(atan2(orthoCount, parelCount))
			
			Logger.finer("(" + directionOfPath.count(_ == weakDir) + " - " + directionOfPath.count(_ == strngDir)
					+ "," + directionOfPath.count(_ == othogDir1) + " + " + directionOfPath.count(_ == othogDir2) + ")")
			Logger.finer(weakDir + ": (" + parelCount + "," + orthoCount
					+ ") => theta=" + theta)
			;
			val scaler = {(x:Double) => (1 * x * x ) + (.5 *  x) + .5}
			scaler(1 - (theta / PI))
		}
	}
	
	/** add to owner Player */
	object TurnStartReaction extends Reaction
	{
		override def apply(e:Event) = {e match {
			case StartOfTurn => {
				_currentStatusTurnsLeft = _currentStatusTurnsLeft - 1
				if (_currentStatusTurnsLeft <= 0) {_currentStatus = None}
				
				_canMoveThisTurn = tokenClass.speed.get
				_canAttackThisTurn = true
			}
			case EndOfTurn => {
				_canMoveThisTurn = 0
				_canAttackThisTurn = false
			}
			case _ => {}
		}}
		override def isDefinedAt(e:Event) = {e match {
			case StartOfTurn => true
			case EndOfTurn => true
			case _ => false
		}}
		override def toString = CannonicalToken.this.toString + ".TurnStartReaction"
	}
	
	/** add to owner player */
	object AttackReaction extends Reaction
	{
		override def apply(e:Event) = {
			// attacker is this, due to isDefinedAt
			val target = e match {
				case RequestAttackForDamage(_, target:Token) => {target}
				case RequestAttackForStatus(_, target:Token) => {target}
			}
			
			if (canAttackThisTurn)
			if (CannonicalToken.this.currentSpace.spacesWithin(tokenClass.range.get, CannonicalToken.this, PhysicalStrikeCost).contains(target.currentSpace))
			{
				CannonicalToken.this ! (e match {
					case RequestAttackForDamage(_, _) => {
						AttackForDamage(target, tokenClass.atkElement.get, tokenClass.atkWeapon.get, currentSpace)
					}
					case RequestAttackForStatus(_, _) => {
						AttackForStatus(target, tokenClass.atkStatus.get, currentSpace)
					}
				})
				
				_canAttackThisTurn = false
			}
		}
		
		override def isDefinedAt(e:Event) = {e match {
			case RequestAttackForDamage(attacker:CannonicalToken, _) => (attacker == CannonicalToken.this)
			case RequestAttackForStatus(attacker:CannonicalToken, _) => (attacker == CannonicalToken.this)
			case _ => false
		}}
		override def toString = CannonicalToken.this.toString + ".AttackReaction"
	}
	
	/* ???
	reactions.+=(this.DieReaction)
	object DieReaction extends Reaction
	{
		override def apply(e:Event) = {e match {
			case Died() =>
			{
				reactions -= TurnStartReaction
				reactions -= AttackReaction
				// other stuff, maybe
				}
			}
			case _ => {}
		}}
		
		override def isDefinedAt(e:Event) = {e match {
			case Died(x) => x == CannonicalToken.this
			case _ => false
		}}
	} */
	
	/**
	 * This is an object that is expected to be waited on. After a move
	 * request, this will be notified when the token things the current
	 * space should be stable.
	 */
	val movementEndedLock:Object = new Object(){}
	/** add to Player */
	object MoveReaction extends Reaction
	{
		def apply(event:Event) { event match {
			case RequestMove(_, movedTo:Space) => 
				processAMoveRequest(movedTo)
		}}
		
		override def isDefinedAt(e:Event) = {e match {
			case RequestMove(attacker:CannonicalToken, _) => (attacker == CannonicalToken.this)
			case _ => false
		}}
		
		override def toString = CannonicalToken.this.toString + ".MoveReaction"
	}
	
	this.reactions += RecursiveMoveReaction
	/** add to self */
	private object RecursiveMoveReaction extends Reaction
	{
		def apply(event:Event) { event match {
			case RecursiveRequestMove(movedTo:Space) => 
				processAMoveRequest(movedTo)
		}}
		
		override def isDefinedAt(e:Event) = {e match {
			case RecursiveRequestMove(_) => true
			case _ => false
		}}
		
		override def toString = CannonicalToken.this.toString + ".RecursiveMoveReaction"
	}
	
	//
	private case class RecursiveRequestMove(movedTo:Space) extends Event
	
	/**
	 * MoveReaction and RecursiveMoveReaction are sent to different places and respond to different things,
	 * both act in the exact same way.
	 */
	private def processAMoveRequest(movedTo:Space)
	{
		val messageLevel = Level.FINE
		MovementLogger.entering("com.rayrobdod.deductionTactics.CannonicalToken",
				"processAMoveRequest", movedTo)
		
		var headLogMessage =
		if (MovementLogger.isLoggable(messageLevel))
			this + ": " + canMoveThisTurn
		else
			""
				
		if (currentSpace == null)
		{
			MovementLogger.log(messageLevel, headLogMessage + "; initial move")
			
			CannonicalToken.this ! Moved(movedTo, true)
			movementEndedLock.synchronized {movementEndedLock.notifyAll}
		}
		else if (canMoveThisTurn > 0)
		{
			if (currentSpace == movedTo)
			{
				MovementLogger.log(messageLevel, headLogMessage + "; is at movedTo")
				movementEndedLock.synchronized {movementEndedLock.notifyAll}
			}
			else if (currentSpace.adjacentSpaces.exists{_ == movedTo})
			{
				val movementCost = movedTo.typeOfSpace.cost(CannonicalToken.this, TokenMovementCost)
				
				MovementLogger.log(messageLevel, headLogMessage + " - " + movementCost + "; adjacent movedTo")
				
				if (movementCost <= _canMoveThisTurn)
				{
					CannonicalToken.this ! Moved(movedTo, true)
					_canMoveThisTurn = _canMoveThisTurn - movementCost
				}
				movementEndedLock.synchronized {movementEndedLock.notifyAll}
			}
			else
			{
				val path = currentSpace.pathTo(movedTo, CannonicalToken.this, TokenMovementCost)
				val adjacentMoveTo = path(1)
				val movementCost = adjacentMoveTo.typeOfSpace.cost(CannonicalToken.this, TokenMovementCost)
				
				MovementLogger.log(messageLevel, headLogMessage + " - " + movementCost + "; non-adjacent movedTo")
				MovementLogger.log(Level.FINER, path.toString)
				
				if (movementCost <= _canMoveThisTurn)
				{
					CannonicalToken.this ! Moved(adjacentMoveTo, true)
					_canMoveThisTurn = _canMoveThisTurn - movementCost
					
					CannonicalToken.this ! RecursiveRequestMove(movedTo)
				}
				else {
					// can't move to desired space, so give up
					movementEndedLock.synchronized {movementEndedLock.notifyAll}
				}
			}
		}
		else
		{
			MovementLogger.log(messageLevel, headLogMessage + "; can't move")
			movementEndedLock.synchronized {movementEndedLock.notifyAll}
		}
	}
	
	/** Add to player */
	class StatusAct(allTokens:ListOfTokens) extends Reaction
	{
		val moveToRandomSpace = {(s:BoardGameSpace, i:Int) =>
			val possibleNext = s.adjacentSpaces.toSeq
			val next = possibleNext(Random.nextInt(possibleNext.size))
			
			val tokenOnNext = allTokens.aliveTokens.flatten.find{_.currentSpace == next}
			
			CannonicalToken.this.MoveReaction(RequestMove(CannonicalToken.this, next))
			tokenOnNext.foreach{(x:Token) =>
				CannonicalToken.this.AttackReaction(RequestAttackForDamage(CannonicalToken.this, x))
			}
			
			next
		}
		
		override def apply(e:Event) = {currentStatus match {
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
		}}
		
		override def isDefinedAt(e:Event) = {e match {
			case StartOfTurn => true
			case _ => false
		}}
		override def toString = CannonicalToken.this.toString + ".StatusReaction"
	}
}
