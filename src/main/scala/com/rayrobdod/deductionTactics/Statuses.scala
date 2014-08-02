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

import scala.collection.immutable.{Seq, Set}

/**
 * An enumeration of statuses
 * 
 * May in the future contain what the status does
 * @author Raymond Dodge
 * @todo move status effect effects from [[Token.beAfflictedByStatus()]] to here
 */
object Statuses {
	/** Might as well be an Enumeration value at this point */
	final class Status(val id:Int, val name:String) {
		override def toString = "com.rayrobdod.deductionTactics.Statuses." + name
	}
	
	/** no move */
	val Sleep = new Status(0, "Sleep")
	/** damage */
	val Burn = new Status(1, "Burn")
	/** can't attack */
	val Blind = new Status(2, "Blind")
	/** move three space or attack before player given control */
	val Confuse = new Status(3, "Confuse")
	/** damage + move a space or attack before player given control */
	val Neuro = new Status(4, "Neuro")
	/** damage + move one space per turn max */
	val Snake = new Status(5, "Snake")
	/** undamage (given that you can't attack partners...) */
	val Heal = new Status(6, "Heal")
	/** do nothing (so that this doesn't have to be an option) */
	val Normal = new Status(7, "Normal")
	
	def values = Seq[Status](Sleep, Burn, Blind, Confuse, Neuro, Snake, Heal, Normal)
	def apply(x:Int) = values(x) //.find{_.id == x}.get
	
	def withName(s:String) = {
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
