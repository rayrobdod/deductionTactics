package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction
import com.rayrobdod.boardGame.{Token => BoardGameToken, Space}

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
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
 */
final class MirrorToken(parent:Token) extends Token
{
	def currentHitpoints = parent.currentHitpoints
	def currentStatus = parent.currentStatus
	def currentStatusTurnsLeft = parent.currentStatusTurnsLeft
	def canMoveThisTurn = parent.canMoveThisTurn
	def canAttackThisTurn = parent.canAttackThisTurn
	
	private val tokenSuspicions = new SuspicionsTokenClass
	def tokenClass = tokenSuspicions
	
	parent.addMoveReaction(MirrorMoveReaction)
	private object MirrorMoveReaction extends BoardGameToken.MoveReactionType {
		def apply(s:Space, b:Boolean):Unit = {
			MirrorToken.this.currentSpace_=(s, b)
		}
	}
	
	parent.addDiedReaction(MirrorDiedReaction)
	private object MirrorDiedReaction extends Function0[Unit] {
		def apply():Unit = MirrorToken.this.triggerDiedReactions
	}
	
	parent.addUpdateReaction(MirrorUpdateReaction)
	private object MirrorUpdateReaction extends Function0[Unit] {
		def apply():Unit = MirrorToken.this.triggerUpdateReactions
	}
	
	parent.addDamageAttackedReaction(MirrorDamageAttackReaction)
	private object MirrorDamageAttackReaction extends Token.DamageAttackedReactionType {
		def apply(a:Element, b:Weaponkind, c:Space):Unit = MirrorToken.this.triggerDamageAttackedReactions(a,b,c)
	}
	
	parent.addStatusAttackedReaction(MirrorStatusAttackReaction)
	private object MirrorStatusAttackReaction extends Token.StatusAttackedReactionType {
		def apply(a:Status, b:Space):Unit = MirrorToken.this.triggerStatusAttackedReactions(a,b)
	}
	
	
	
	def beAttacked(elem:Element, kind:Weaponkind, from:Space) = {
		parent.beAttacked(elem, kind, from)
	}
	def beAttacked(status:Status, from:Space) = {
		parent.beAttacked(status, from)
	}
}
