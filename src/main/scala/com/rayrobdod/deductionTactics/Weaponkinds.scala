/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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
 * An enumeration of weaponkinds.
 * @author Raymond Dodge
 */
object Weaponkinds
{
	final class Weaponkind(val id:Int, val name:String, val classType:String)
	{
		override def toString:String = "com.rayrobdod.deductionTactics.Weaponkinds." + name
	}
	
	val Bladekind = new Weaponkind(0, "Bladekind", "swordsman")
	val Bluntkind = new Weaponkind(1, "Bluntkind", "clubman")
	val Spearkind = new Weaponkind(2, "Spearkind", "pikeman")
	val Whipkind  = new Weaponkind(3, "Whipkind", "whipman")
	val Powderkind= new Weaponkind(4, "Powderkind", "powderman")
	
	def values:Seq[Weaponkind] = Seq[Weaponkind](Bladekind, Bluntkind, Spearkind, Whipkind, Powderkind)
	def apply(x:Int):Weaponkind = values(x) //.find{_.id == x}.get
	
	def withName(s:String):Weaponkind = {
		try {
			values.find{_.name.equalsIgnoreCase(s + "kind")}.get
		} catch {
			case x:NoSuchElementException => 
				val y = new NoSuchElementException("No element with name: " + s)
				y.initCause(x)
				throw y
		}
	}
}
