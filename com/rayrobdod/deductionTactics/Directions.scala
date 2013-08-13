package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.{RectangularSpace, Space => BoardGameSpace,
			PhysicalStrikeCost}
import scala.collection.immutable.{Seq, Set}
import javax.swing.Icon
import com.rayrobdod.swing.NameAndIcon
import LoggerInitializer.{cannonicalTokenLogger => Logger}

/**
 * An enumeration of directions
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 02 Feb 2012 - changed "filter{}.head" to "find{}.get"
 * @version 02 Feb 2012 - subtrait Direction now extends NameAndIcon
 * @version 15 Apr 2012 - moving icons
 * @version 24 Apr 2012 - implementing Directions.Direction.toString
 * @version 27 Jun 2012 - moving the majority of CannonicalToken.BeAttackedReaction.directionMultiplier's
			implementation to Directions.pathDirections and Directions.Direction.weaknessMultiplier
 * @version 10 Jul 2012 - replacing apply(x) = values.find{_.id == x}.get with  apply(x) = values(x)
 */
object Directions
{
	class Direction(val id:Int, val name:String,
			val function:Function1[RectangularSpace,Option[BoardGameSpace]])
			extends NameAndIcon
	{
		def spaceIs(th:RectangularSpace, other:BoardGameSpace):Boolean =
		{
			// can't use match here, since "case other" matches everything
			// instead of acting like "== other"
			function(th).map[Boolean]{(mid:BoardGameSpace) =>
				if (mid == other) true
				else if (mid.isInstanceOf[RectangularSpace])
							spaceIs(th, mid.asInstanceOf[RectangularSpace])
				else false
			}.getOrElse(false)
		}
		
		lazy val icon:Icon = {
			loadIcon(this.getClass().getResource("/com/rayrobdod/glyphs/direction/" + name.toLowerCase + ".svg"))
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
			import com.rayrobdod.scalaParser.IsEqualTo
			val orthoCount2 = pathDirections.count(new IsEqualTo(othogDir1)) -
					pathDirections.count(new IsEqualTo(othogDir2))
			
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
	
	val Left  = new Direction(0, "Left",  (th:RectangularSpace) => { th.left  })
	val Up    = new Direction(1, "Up",    (th:RectangularSpace) => { th.up    })
	val Right = new Direction(2, "Right", (th:RectangularSpace) => { th.right })
	val Down  = new Direction(3, "Down",  (th:RectangularSpace) => { th.down  })
	
	def values = Seq[Direction](Left, Up, Right, Down)
	def withName(s:String) = values.find{_.name equalsIgnoreCase s}.get
	def apply(x:Int) = values(x) // values.find{_.id == x}.get
	
	/**
	 * The directions one would move to go from fromSpace to toSpace
	 */
	def pathDirections(fromSpace:BoardGameSpace, toSpace:BoardGameSpace):Seq[Direction] =
	{
		val path:Seq[BoardGameSpace] = fromSpace.pathTo(toSpace, null, PhysicalStrikeCost)
		
		val pathDirections = path.zip(path.head +: path).map({(next:BoardGameSpace, curr:BoardGameSpace) =>
			curr match {
				case currRect:RectangularSpace => {
					val candidates = Directions.values.map{
							_.function(currRect)}.zip(Directions.values).toMap
					
					candidates.getOrElse(Some(next), null)
				}
				case _ => null
			}
		}.tupled)
		
		Logger.finer(pathDirections.toString)
		
		return pathDirections;
	}
}
