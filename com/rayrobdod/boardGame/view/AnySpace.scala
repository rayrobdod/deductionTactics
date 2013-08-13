package com.rayrobdod.boardGame.view

import com.rayrobdod.boardGame.SpaceClassConstructor
import com.rayrobdod.boardGame.{SpaceClass, UniformMovementCost, NoPassOverAction, NoLandOnAction}

/**
 * A SpaceConstructor that matches any object and constructs an unspecified space if used.
 * 
 * @author Raymond Dodge
 * @version 02 Aug 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 29 Sept 2011 - complete rewrite to construct SpaceClass instead of RectangularSpaces
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
object AnySpace extends SpaceClassConstructor
{
	/** 
	 * a SpaceClass with all three standard mixins.
	 */
	private object GenericSpace extends SpaceClass with UniformMovementCost
			with NoPassOverAction with NoLandOnAction
	
	/** @return true */
	def unapply(a:SpaceClass) = true
	//** @return a SpaceClass with all three standard mixins. */
	def apply():SpaceClass = GenericSpace
}
