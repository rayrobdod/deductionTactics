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
import scala.collection.mutable.Buffer
import com.rayrobdod.boardGame.{Space,
		Token => BoardGameToken}

/**
 * 
 * @author Raymond Dodge
 * @version a.6.0
 */
final case class Token (
	override val currentSpace:Space[SpaceClass],
	val currentHitpoints:Int = 256,
	val currentStatus:Option[Status] = None,
	val currentStatusTurnsLeft:Int = 0,
	val tokenClass:Option[TokenClass] = None,
	
	val canMoveThisTurn:Int = 0,
	val canAttackThisTurn:Boolean = false
) extends BoardGameToken[SpaceClass](currentSpace) {
	final val maximumHitpoints:Int = 256
	final val baseDamage:Int = 8
	
	final def startOfTurn():Token = {
		val newStatus = currentStatus.filter{(a) => currentStatusTurnsLeft >= 0}
		
		new Token(
			currentSpace,
			currentHitpoints,
			newStatus,
			currentStatusTurnsLeft - 1,
			tokenClass,
			tokenClass.map{_.speed}.getOrElse{0},
			true
		)
	}
	
	final def endOfTurn():Token = {
		new Token(
			currentSpace,
			currentHitpoints,
			currentStatus,
			currentStatusTurnsLeft,
			tokenClass,
			0,
			false
		)
	}
}
