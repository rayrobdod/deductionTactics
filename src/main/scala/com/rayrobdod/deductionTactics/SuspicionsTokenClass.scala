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
import scala.collection.immutable.Map

/**
 * A class that mirrors [[com.rayrobdod.deductionTactics.CannonicalTokenClass]] but with all methods
 * being mutable and the slight possiblity of any of the items being [[scala.None]]
 * 
 * @author Raymond Dodge
 * @version a.5.0
 * @todo Make observable
 * 
 */
class SuspicionsTokenClass extends TokenClass
{
	var body:Option[BodyType] = None
	
	var atkElement:Option[Element] = None
	var atkWeapon:Option[Weaponkind] = None
	var atkStatus:Option[Status] = None
	var range:Option[Int] = None
	var speed:Option[Int] = None
	
	var weakWeapon:Map[Weaponkind,Option[Float]] = Weaponkinds.values.map{((_, None))}.toMap
	var weakStatus:Option[Status] = None
	var weakDirection:Option[Direction] = None
	
	var name = "???"
	
	override def hashCode:Int = {
		body.hashCode +
		atkElement.hashCode +
		atkWeapon.hashCode +
		atkStatus.hashCode +
		range.hashCode +
		speed.hashCode +
		weakWeapon.hashCode +
		weakStatus.hashCode +
		weakDirection.hashCode +
		name.hashCode
	}
	def canEquals(other:Any) = {other.isInstanceOf[SuspicionsTokenClass]}
	override def equals(other:Any) = other match {
		case other2:SuspicionsTokenClass => {
			this.canEquals(other2) && other2.canEquals(this) &&
			this.body == other2.body &&
			this.atkElement == other2.atkElement &&
			this.atkWeapon == other2.atkWeapon &&
			this.atkStatus == other2.atkStatus &&
			this.range == other2.range &&
			this.speed == other2.speed &&
			this.weakWeapon == other2.weakWeapon &&
			this.weakStatus == other2.weakStatus &&
			this.weakDirection == other2.weakDirection &&
			this.name == other2.name
		}
		case _ => false
	}
}
