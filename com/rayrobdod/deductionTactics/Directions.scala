package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.{RectangularSpace, Space => BoardGameSpace}
import scala.collection.immutable.{Seq, Set}
import javax.swing.Icon
import com.rayrobdod.swing.NameAndIcon

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
		
		override def toString = "com.rayrobdod.deductionTactics.Directions." + name
	}
	
	val Left  = new Direction(0, "Left",  (th:RectangularSpace) => { th.left  })
	val Up    = new Direction(1, "Up",    (th:RectangularSpace) => { th.up    })
	val Right = new Direction(2, "Right", (th:RectangularSpace) => { th.right })
	val Down  = new Direction(3, "Down",  (th:RectangularSpace) => { th.down  })
	
	def values = Seq[Direction](Left, Up, Right, Down)
	def withName(s:String) = values.find{_.name equalsIgnoreCase s}.get
	def apply(x:Int) = values.find{_.id == x}.get
}
