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

import com.rayrobdod.boardGame.{StrictRectangularSpace}
import scala.collection.immutable.{Seq, Set}
import LoggerInitializer.{cannonicalTokenLogger => Logger}

/**
 * An enumeration of directions
 * @author Raymond Dodge
 */
object Directions
{
	final class Direction(val id:Int, val name:String,
			val function:Function1[StrictRectangularSpace[SpaceClass],Option[StrictRectangularSpace[SpaceClass]]])
	{
		def spaceIs(th:StrictRectangularSpace[SpaceClass], other:StrictRectangularSpace[SpaceClass]):Boolean =
		{
			// can't use match here, since "case other" matches everything
			// instead of acting like "== other"
			function(th).map[Boolean]{(mid:StrictRectangularSpace[SpaceClass]) =>
				if (mid == other) {
					true
				} else {
					spaceIs(th, mid)
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
			
			import java.lang.Math.{atan2, PI, abs}		
			val theta = abs(atan2(orthoCount, parelCount))
			
			Logger.finer("(" + pathDirections.count(_ == weakDir) + " - " + pathDirections.count(_ == strngDir)
					+ "," + pathDirections.count(_ == othogDir1) + " + " + pathDirections.count(_ == othogDir2) + ")")
			Logger.finer(weakDir + ": (" + parelCount + "," + orthoCount
					+ ") => theta=" + theta)
			;
			val scaler = {(x:Double) => (1 * x * x ) + (.5 *  x) + .5}
			scaler(1 - (theta / PI)).floatValue
		}
		
		override def toString = "com.rayrobdod.deductionTactics.Directions." + name
	}
	
	val Left  = new Direction(0, "Left",  (th:StrictRectangularSpace[SpaceClass]) => { th.left  })
	val Up    = new Direction(1, "Up",    (th:StrictRectangularSpace[SpaceClass]) => { th.up    })
	val Right = new Direction(2, "Right", (th:StrictRectangularSpace[SpaceClass]) => { th.right })
	val Down  = new Direction(3, "Down",  (th:StrictRectangularSpace[SpaceClass]) => { th.down  })
	
	def values = Seq[Direction](Left, Up, Right, Down)
	def apply(x:Int) = values(x) // values.find{_.id == x}.get
	
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
	
	/**
	 * The directions one would move to go from fromSpace to toSpace
	 */
	def pathDirections(fromSpace:StrictRectangularSpace[SpaceClass], toSpace:StrictRectangularSpace[SpaceClass]):Seq[Direction] =
	{
		import com.rayrobdod.boardGame.Space
		
		val path2:Seq[Space[SpaceClass]] = fromSpace.pathTo(toSpace, SpaceClass.attackSpaceCost)
		val path:Seq[StrictRectangularSpace[SpaceClass]] = path2.map{_.asInstanceOf[StrictRectangularSpace[SpaceClass]]}
		
		val pathDirections = path.zip(path.head +: path).map({(next:StrictRectangularSpace[SpaceClass], curr:StrictRectangularSpace[SpaceClass]) =>
			val candidates = Directions.values.map{
				_.function(curr)}.zip(Directions.values).toMap
			
			candidates.getOrElse(Some(next), null)
		}.tupled)
		
		Logger.finer(pathDirections.toString)
		
		return pathDirections;
	}
}
