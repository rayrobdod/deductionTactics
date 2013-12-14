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
package ai

import com.rayrobdod.boardGame.{RectangularField => Field}

/**
 * A decorator for PlayerAIs. It adds a set of listeners to the tokens and
 * player that can help determine various things about the tokens.
 *
 * @author Raymond Dodge
 * @version a.5.2
 */
final class WithAutorecord(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** Forwards command to base */
	def buildTeam = base.buildTeam
	
	/** Forwards command to base, then adds listeners as needed */
	def initialize(player:Player, field:Field)
	{
		// set up interface
		base.initialize(player, field)
		
		// setup recorders
		player.tokens.tokens.flatten.foreach{(mine:Token) =>
			val attacks = new StandardObserveAttacks(mine, player.tokens)
			
			mine.beDamageAttackedReactions_+=(attacks)
			mine.beStatusAttackedReactions_+=(attacks)
		}
		player.tokens.otherTokens.flatten.foreach{(other:MirrorToken) =>
			val movement = new StandardObserveMovement(other)
			other.moveReactions_+=(movement)
			player.addStartTurnReaction(movement)
		}
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithAutorecord]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithAutorecord].canEquals(this) &&
				this.base == other.asInstanceOf[WithAutorecord].base
	}
	override def hashCode = base.hashCode * 13 + 19
	
	override def toString = base.toString + " with " + this.getClass.getName
}
