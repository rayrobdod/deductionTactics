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

/**
 * An enumeration of Bodytypes. Partially determines what spaces
 * a token may enter.
 * @author Raymond Dodge
 * @version a.6.0
 */
object BodyTypes {
	final class BodyType(val id:Int, val name:String)
	
	val Humanoid = new BodyType(0, "Human")
	val Avian    = new BodyType(1, "Avian")
	val Gerbil   = new BodyType(2, "Gerbillinae")
	
	def values:Seq[BodyType] = Seq[BodyType](Humanoid, Avian, Gerbil)
	def apply(x:Int):BodyType = values(x) //.find{_.id == x}.get
	
	def withName(s:String):BodyType = {
		try {
			values.find{_.name.equalsIgnoreCase(s)}.get
		} catch {
			case x:NoSuchElementException => 
				val y = new NoSuchElementException("No element with name: " + s)
				y.initCause(x)
				throw y
		}
	}
}
