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
		StrictRectangularSpace,
		Token => BoardGameToken}

/**
 * 
 * @author Raymond Dodge
 * @version a.6.0
 */
final case class Token (
	override val currentSpace:Space[SpaceClass],
	val currentHitpoints:Int = Token.maximumHitpoints,
	val currentStatus:Status = Statuses.Normal,
	val currentStatusTurnsLeft:Int = 0,
	val tokenClass:Option[TokenClass] = None,
	
	val canMoveThisTurn:Int = 0,
	val canAttackThisTurn:Boolean = false
) extends BoardGameToken[SpaceClass](currentSpace) {
	
	final def startOfTurn():Token = {
		val newStatus = if (currentStatusTurnsLeft > 1) {currentStatus} else {Statuses.Normal}
		
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
	
	final def takeDamage(attacker:Token, ts:ListOfTokens):Token = {
		val attackeeClass = this.tokenClass.get
		val attackerClass = attacker.tokenClass.get
		
		
		val path = Directions.pathDirections(attacker.currentSpace.asInstanceOf[StrictRectangularSpace[SpaceClass]], this.currentSpace.asInstanceOf[StrictRectangularSpace[SpaceClass]], attacker, ts)
		val weakDir = this.tokenClass.get.weakDirection
		val directionMultiplier = weakDir.weaknessMultiplier(path)
		
		val multiplier = attackeeClass.weakWeapon(attackerClass.atkWeapon) *
				(if (currentStatus == attackeeClass.weakStatus) {2} else {1}) *
				(directionMultiplier) *
				(attackeeClass.atkElement.damageModifier(attackerClass.atkElement));
				
		val damageDone = (if (multiplier > 8) {320} else {Token.baseDamage * multiplier}).intValue
		
		this.copy(currentHitpoints = currentHitpoints - damageDone)
	}
	
	
}


object Token {
	final val maximumHitpoints:Int = 256
	final val baseDamage:Int = 8
}
