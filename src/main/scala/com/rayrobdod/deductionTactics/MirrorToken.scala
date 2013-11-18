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
	
	parent.moveReactions_+=(MirrorMoveReaction)
	private object MirrorMoveReaction extends BoardGameToken.MoveReactionType {
		def apply(s:Space, b:Boolean):Unit = {
			MirrorToken.this.currentSpace_=(s, b)
		}
	}
	
	parent.diedReactions_+=(MirrorDiedReaction)
	private object MirrorDiedReaction extends Function0[Unit] {
		def apply():Unit = MirrorToken.this.triggerDiedReactions
	}
	
	parent.updateReactions_+=(MirrorUpdateReaction)
	private object MirrorUpdateReaction extends Function0[Unit] {
		def apply():Unit = MirrorToken.this.triggerUpdateReactions
	}
	
	parent.beDamageAttackedReactions_+=(MirrorDamageAttackReaction)
	private object MirrorDamageAttackReaction extends Token.DamageAttackedReactionType {
		def apply(a:Element, b:Weaponkind, c:Space):Unit = MirrorToken.this.triggerBeDamageAttackedReactions(a,b,c)
	}
	
	parent.beStatusAttackedReactions_+=(MirrorStatusAttackReaction)
	private object MirrorStatusAttackReaction extends Token.StatusAttackedReactionType {
		def apply(a:Status, b:Space):Unit = MirrorToken.this.triggerBeStatusAttackedReactions(a,b)
	}
	
	
	
	def beAttacked(elem:Element, kind:Weaponkind, from:Space) = {
		parent.beAttacked(elem, kind, from)
	}
	def beAttacked(status:Status, from:Space) = {
		parent.beAttacked(status, from)
	}
}
