package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction
import scala.swing.event.Event
import scala.swing.Reactions.Reaction
import com.rayrobdod.boardGame.Moved

/**
 * @author Raymond Dodge
 * @version 19 Jan 2012
 * @version 12 Feb 2012 - made the canmove/attackthisturn items visible
 * @version 15 Feb 2012 - now forwards TakeStatus events
 * @version 20 Mar 2012 - modified reactions for new event model
 * @version 06 Apr 2012 - since a Mirrortoken shouldn't be getting a AttackFor* event,
 			SendDamageToParentReaction no longer responds to that.
 * @version 06 Apr 2012 - renamed SendDamageToParentReaction to MirrorParentMove
 * @version 08 Apr 2012 - adding response to AttackFor* back to MirrorParentMove. StandardObserveAttacks did use it.
 * @version 08 Apr 2012 - adding response to Died to MirrorParentMove.
 */
class MirrorToken(parent:Token) extends Token
{
	def currentHitpoints = parent.currentHitpoints
	def currentStatus = parent.currentStatus
	def currentStatusTurnsLeft = parent.currentStatusTurnsLeft
	def canMoveThisTurn = parent.canMoveThisTurn
	def canAttackThisTurn = parent.canAttackThisTurn
	
	private val tokenSuspicions = new SuspicionsTokenClass
	def tokenClass = tokenSuspicions
	
	
	parent.reactions += this.MirrorParentMove
	/** add to parent CannonicalToken */
	object MirrorParentMove extends Reaction
	{
		override def apply(e:Event) = {MirrorToken.this ! e}
		
		override def isDefinedAt(e:Event) = {e match {
			case e:Moved => true
			case e:AttackForStatus => true
			case e:AttackForDamage => true
			case e:Died => true
			case _  => false
		}}
	}
	
	
}
