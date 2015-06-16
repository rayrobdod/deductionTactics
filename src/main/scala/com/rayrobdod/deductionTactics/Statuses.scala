/*
	Deduction Tactics
	Copyright (C) 2012-2014  Raymond Dodge

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

import scala.collection.immutable.{Seq, Set}

/**
 * An enumeration of statuses
 * 
 * May in the future contain what the status does
 * @verion a.6.0
 */
object Statuses {
	abstract class Status(val id:Int, val name:String) {
		override def toString:String = "com.rayrobdod.deductionTactics.Statuses." + name
		
		/**
		 * Number of moves
		 * @since a.6.0
		 */
		def randMovesPerTurn:Int = 0
		/**
		 * Returns a token that has gone through this status's effect,
		 * not counting the random movement thing.
		 * @since a.6.0
		 */
		def affect(t:Token):Token
	}
	
	/** no move */
	val Sleep:Status = new Status(0, "Sleep") {
		override def affect(t:Token):Token = t.copy(canMoveThisTurn = 0)
	}
	/** major damage */
	val Burn:Status = new Status(1, "Burn") {
		override def affect(t:Token):Token =
			t.copy(currentHitpoints = t.currentHitpoints - 2 * Token.baseDamage)
	}
	/** can't attack */
	val Blind:Status = new Status(2, "Blind") {
		override def affect(t:Token):Token = t.copy(canAttackThisTurn = false)
	}
	/** move three space or attack before player given control */
	val Confuse:Status = new Status(3, "Confuse") {
		override def randMovesPerTurn:Int = 3
		override def affect(t:Token):Token = t
	}
	/** damage + move a space or attack before player given control */
	val Neuro:Status = new Status(4, "Neuro"){
		override def randMovesPerTurn:Int = 1
		override def affect(t:Token):Token =
			t.copy(currentHitpoints = t.currentHitpoints - Token.baseDamage)
	}
	/** damage + move one space per turn max */
	val Snake:Status = new Status(5, "Snake") {
		override def affect(t:Token):Token =
			t.copy(canMoveThisTurn = 1, currentHitpoints = t.currentHitpoints - Token.baseDamage)
	}
	/** undamage (given that you can't attack partners...) */
	val Heal:Status = new Status(6, "Heal") {
		override def affect(t:Token):Token =
			t.copy(currentHitpoints = t.currentHitpoints + Token.baseDamage)
	}
	/** do nothing (so that currentStatus doesn't have to be an option)
	 * In other words, a null object.
	 * @since a.6.0
	 */
	val Normal:Status = new Status(7, "Normal") {
		override def affect(t:Token):Token = t 	 
	}
	
	def values:Seq[Status] = Seq[Status](Sleep, Burn, Blind, Confuse, Neuro, Snake, Heal, Normal)
	def apply(x:Int):Status = values(x) //.find{_.id == x}.get
	
	def withName(s:String):Status = {
		try {
			values.find{_.name equalsIgnoreCase s}.get
		} catch {
			case x:NoSuchElementException => 
				val y = new NoSuchElementException("No element with name: "+ s)
				y.initCause(x)
				throw y
		}
	}
}
