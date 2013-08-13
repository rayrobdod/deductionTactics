package com.rayrobdod.boardGame

import scala.parallel.Future

/**
 * A constructor of Spaces.
 * 
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.model to net.verizon.rayrobdod.boardGame
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 * @deprecated 29 Sept 2011 - SpaceClass makes this unneeded.
 */
trait RectangularSpaceConstructor
{
	def unapply(a:Object):Boolean
	def apply(leftFuture:Future[Option[RectangularSpace]],
		upFuture:Future[Option[RectangularSpace]],
		rightFuture:Future[Option[RectangularSpace]],
		downFuture:Future[Option[RectangularSpace]]):RectangularSpace
}
