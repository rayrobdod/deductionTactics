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

import com.rayrobdod.boardGame.{Space, TokenMovementCost}
import com.rayrobdod.boardGame.{RectangularField => Field}


/**
 * An AI that blatantly cheats. This needs to not work.
 * 
 * @author Raymond Dodge
 * @version 2013 Aug 07
 */
class RepeatAttackCheater extends PlayerAI
{
	/** [[com.rayrobdod.deductionTactics.ai.randomTeam]] */
	def buildTeam = randomTeam()
	
	def takeTurn(player:Player):Any = {
		val otherTokens = player.tokens.otherTokens.flatten
		
		otherTokens.foreach{(x:MirrorToken) =>
			while (x.currentHitpoints > 0) {
				x.beAttacked(Elements.Fire, Weaponkinds.Bluntkind, aSpace)
			}
		}
	}
	
	var aSpace:Space = null
	def initialize(player:Player, field:Field) = {
		aSpace = field.space(0,0);
	}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[RepeatAttackCheater]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[RepeatAttackCheater].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
