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

import com.rayrobdod.boardGame.RectangularSpace
import scala.collection.immutable.Seq

/**
 * An enumeration of directions
 * @version a.6.0
 */
object Directions
{
	/**
	 * A direction is a relation between two spaces. An attack's direction is 
	 * determined by how two tokens are located relative to each other. The attack's
	 * direction is independent of the token's class. The defensive properties of a token
	 * are determined by the token's class.
	 */
	final class Direction(val id:Int, val name:String,
			val function:Function1[RectangularSpace[SpaceClass],Option[RectangularSpace[SpaceClass]]])
	{
		/**
		 * True if other is in the specified direction of self
		 * @todo better name (?)
		 */
		def spaceIs(self:RectangularSpace[SpaceClass], other:RectangularSpace[SpaceClass]):Boolean =
		{
			// can't use match here, since "case other" matches everything
			// instead of acting like "== other"
			function(self).map[Boolean]{(mid:RectangularSpace[SpaceClass]) =>
				if (mid == other) {
					true
				} else {
					spaceIs(mid, other)
				}
			}.getOrElse(false)
		}
		
		/**
		 * The multiplier when being attacked from the pathDirections when
		 * a unit is weak to this direction
		 */
		def weaknessMultiplier(pathDirections:Seq[Direction]):Float = {
			val weakDir = this
			val strngDir = Directions((weakDir.id + 2) % 4)
			val othogDir1 = Directions((weakDir.id + 1) % 4)
			val othogDir2 = Directions((weakDir.id + 3) % 4)
			
			val parelCount = pathDirections.count(_ == weakDir) -
					pathDirections.count(_ == strngDir)
			val orthoCount = pathDirections.count(_ == othogDir1) -
					pathDirections.count(_ == othogDir2)
			
			import scala.math.{max, abs}
			val MAX_PRESCALE_VALUE = 16
			val DIAGONAL_VALUE = 16 / 4
			
			val dividend = max(1, max(abs(parelCount), abs(orthoCount * DIAGONAL_VALUE)))
			val prescale = parelCount * MAX_PRESCALE_VALUE / dividend
			def scale(x:Int) = if (x >= 0) {
				((-1.0 / 192) * x + (7.0 / 48)) * x + 1
			} else {
				((1.0 / 384) * x + (7.0 / 96)) * x + 1
			}
			scale(prescale).floatValue
		}
		
		override def toString:String = "com.rayrobdod.deductionTactics.Directions." + name
	}
	
	val Left  = new Direction(0, "Left",  (th:RectangularSpace[SpaceClass]) => { th.west })
	val Up    = new Direction(1, "Up",    (th:RectangularSpace[SpaceClass]) => { th.north})
	val Right = new Direction(2, "Right", (th:RectangularSpace[SpaceClass]) => { th.east })
	val Down  = new Direction(3, "Down",  (th:RectangularSpace[SpaceClass]) => { th.south})
	
	def values:Seq[Direction] = Seq[Direction](Left, Up, Right, Down)
	def apply(x:Int):Direction = values(x) // values.find{_.id == x}.get
	
	def withName(s:String):Direction = {
		try {
			values.find{_.name equalsIgnoreCase s}.get
		} catch {
			case x:NoSuchElementException => 
				val y = new NoSuchElementException("No element with name: " + s)
				y.initCause(x)
				throw y
		}
	}
	
	/**
	 * The directions one would move to go from fromSpace to toSpace
	 */
	def pathDirections(
			fromSpace:RectangularSpace[SpaceClass],
			toSpace:RectangularSpace[SpaceClass],
			movingToken:Token,
			listOfTokens:ListOfTokens
	):Seq[Direction] = {
		val path:Seq[RectangularSpace[SpaceClass]] = fromSpace.pathTo(toSpace, new AttackCostFunction(movingToken, listOfTokens))
		
		val pathDirections = path.tail.zip(path).map({(next:RectangularSpace[SpaceClass], curr:RectangularSpace[SpaceClass]) =>
			val candidates = Directions.values.map{
				_.function(curr)}.zip(Directions.values).toMap
			
			candidates.getOrElse(Some(next), null)
		}.tupled)
		
		pathDirections
	}
	
	
	
	/** `spaceA is Left of spaceB` */
	implicit class SpaceWithIs(s:RectangularSpace[SpaceClass]) {
		def is(d:Direction):SpaceWithDirection = new SpaceWithDirection(s,d)
	}
	/** `spaceA is Left of spaceB` */
	class SpaceWithDirection(s:RectangularSpace[SpaceClass], d:Direction) {
		def of(o:RectangularSpace[SpaceClass]):Boolean = d.spaceIs(o, s)
	}
}
