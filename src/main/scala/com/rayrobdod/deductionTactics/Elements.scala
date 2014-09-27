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
import com.rayrobdod.deductionTactics.LoggerInitializer.{elementsLogger => logger}

/**
 * An enumeration of elements
 * @author Raymond Dodge
 */
object Elements {
	
	/** @version a.6.0 */
	final class Element(val id:Int, val name:String) {
		def damageModifier(other:Element):Float = {
			((((other.id - this.id) % 5) + 5) % 5) match {
				case 0 => 1f
				case 1 => 2f
				case 2 => 1.5f
				case 3 => .75f
				case 4 => .5f
			}
		}
		
		override def toString = "com.rayrobdod.deductionTactics.Elements." + name
	}
	
	val Light:Element    = new Element(0, "Light"   )
	val Electric:Element = new Element(1, "Electric")
	val Fire:Element     = new Element(2, "Fire"    )
	val Frost:Element    = new Element(3, "Frost"   )
	val Sound:Element    = new Element(4, "Sound"   )
	
	def values = Seq[Element](Light, Electric, Fire, Frost, Sound)
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