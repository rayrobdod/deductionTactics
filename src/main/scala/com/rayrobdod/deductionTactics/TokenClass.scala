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

import scala.collection.immutable.{Map}

/**
 * A description of the attributes of a unit.
 * 
 * A [[com.rayrobdod.deductionTactics.CannonicalTokenClass]] is immutable and represents what the game
 * knows a unit to be like, while a [[com.rayrobdod.deductionTactics.SuspicionsTokenClass]] is a
 * player's guess at what a unit is like.
 * 
 * @author Raymond Dodge
 * @version a.5.0 - removing icons
 */
trait TokenClass
{
	/** A class's name */
	def name:String
	
	/** A class's bodytype. */
	def body:Option[BodyType]
	
	/** The element a unit attacks with. Also determines it's defenses against elements. */
	def atkElement:Option[Element]
	/** The weapon a unit attacks with. */
	def atkWeapon:Option[Weaponkind]
	/** The status a unit attacks with. */
	def atkStatus:Option[Status]
	/** How far away from itself a unit can attack. */
	def range:Option[Int]
	/** How far a unit can move in one turn. */
	def speed:Option[Int]
	
	/** When a unit is attacked from this direction, the attack is strongest */
	def weakDirection:Option[Direction]
	/** The weaknesses when a unit is attacked form a type of weapon */
	def weakWeapon:Map[Weaponkind,Option[Float]]
	/** When a unit is attacked while suffering this status, the attack is strongest */
	def weakStatus:Option[Status]
	
	override def toString = {
		this.getClass.getName + " " +
			"{ name: " + name +
			"; body: " + body +
			"; atkElement: " + atkElement +
			"; atkWeapon: " + atkWeapon +
			"; atkStatus: " + atkStatus +
			"; range: " + range +
			"; speed: " + speed +
			"; weakDirection: " + weakDirection +
			"; weakWeapon: " + weakWeapon +
			"; weakStatus: " + weakStatus +
			" }";
	}
}
