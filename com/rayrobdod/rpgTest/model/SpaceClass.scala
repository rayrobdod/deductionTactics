package com.rayrobdod.rpgTest.model

import com.rayrobdod.boardGame.{SpaceClassConstructor, SpaceClass => BoardGameSpaceClass, Token => BoardGameToken,
		RectangularSpace, NoLandOnAction, NoPassOverAction, RectangularSpaceConstructor, TypeOfCost}
import scala.parallel.Future

/**
 * A Space that has no code
 * 
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 03 Oct 2011 - changing with boardGame.Space, including appending Class to the name
 * @version 15 Dec 2011 - moved from net.verizon.rayrobdod.rpgTest.model to com.rayrobdod.rpgTest.model
 */
abstract class SpaceClass extends BoardGameSpaceClass
		with NoLandOnAction with NoPassOverAction
{
	// ???
}

/**
 * Holds a constant used by the Spaces
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 03 Oct 2011 - appending Class to the name
 * @version 15 Dec 2011 - moved from net.verizon.rayrobdod.rpgTest.model to com.rayrobdod.rpgTest.model
 */
object SpaceClass
{
	val baseMovementCost = 10
	val cannotWalkOn = 1000
}

/**
 * A space that reports that it cannot be walked on.
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 03 Oct 2011 - changing with boardGame.Space, including appending Class to the name
 * @version 15 Dec 2011 - moved from net.verizon.rayrobdod.rpgTest.model to com.rayrobdod.rpgTest.model
 * @version 05 Apr 2012 - (x:{@link SpaceClass}).movementCost(x:Token) → (x:SpaceClass).cost(x:Token, TokenMovementCost); this changed to match
 */
final class WaterSpaceClass extends SpaceClass
{
	override def cost(token:BoardGameToken, costType:TypeOfCost) = SpaceClass.cannotWalkOn
}

/**
 * A SpaceConstructor that constructs and deconstructs WaterSpace
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 03 Oct 2011 - changing with boardGame.Space, including appending Class to the name
 * @version 04 Oct 2011 - changing apply from a def to a val, to reduce memory usage
 * @version 15 Dec 2011 - moved from net.verizon.rayrobdod.rpgTest.model to com.rayrobdod.rpgTest.model
 */
object WaterSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[WaterSpaceClass]}
	val apply = new WaterSpaceClass()
}

/**
 * A space that reports that it can be walked on
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 03 Oct 2011 - changing with boardGame.Space, including appending Class to the name
 * @version 15 Dec 2011 - moved from net.verizon.rayrobdod.rpgTest.model to com.rayrobdod.rpgTest.model
 * @version 05 Apr 2012 - (x:{@link SpaceClass}).movementCost(x:Token) → (x:SpaceClass).cost(x:Token, TokenMovementCost); this changed to match
 */
final class GrassSpaceClass extends SpaceClass
{
	override def cost(token:BoardGameToken, costType:TypeOfCost) = SpaceClass.baseMovementCost
}

/**
 * A SpaceConstructor that constructs and deconstructs GrassSpaces
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 03 Oct 2011 - changing with boardGame.Space, including appending Class to the name
 * @version 04 Oct 2011 - changing apply from a def to a val, to reduce memory usage
 * @version 15 Dec 2011 - moved from net.verizon.rayrobdod.rpgTest.model to com.rayrobdod.rpgTest.model
 */
object GrassSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[GrassSpaceClass]}
	val apply = new GrassSpaceClass
}
