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
import com.rayrobdod.boardGame.{Token => BoardGameToken, Space}
import scala.runtime.{AbstractFunction2 => AFunction2, AbstractFunction1 => AFunction1}

/**
 * A token which mirrors the actions of the parent, but doesn't reveal
 * any information about the Token's class.
 * 
 * @author Raymond Dodge
 * @version a.5.0 - no longer relies on actors
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
	private object MirrorMoveReaction extends AFunction2[Space, Boolean, Unit] with BoardGameToken.MoveReactionType {
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
		def apply(a:Element, b:Weaponkind, d:Int, c:Space):Unit = MirrorToken.this.triggerBeDamageAttackedReactions(a,b,d,c)
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
