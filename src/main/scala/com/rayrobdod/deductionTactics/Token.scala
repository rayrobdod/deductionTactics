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
 * @version a.5.0 - removing actors dependency
 */
trait Token extends BoardGameToken[SpaceClass]
{
	def currentHitpoints:Int
	def currentStatus:Option[Status]
	def currentStatusTurnsLeft:Int
	def tokenClass:Option[TokenClass]
	
	def canMoveThisTurn:Int
	def canAttackThisTurn:Boolean
	
	final val maximumHitpoints:Int = 256
	final val baseDamage:Int = 8
}
