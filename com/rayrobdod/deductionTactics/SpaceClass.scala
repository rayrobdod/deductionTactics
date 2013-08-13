package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.{SpaceClassConstructor, SpaceClass => BoardGameSpaceClass,
		Token => BoardGameToken, TypeOfCost}
import com.rayrobdod.boardGame.{NoLandOnAction, NoPassOverAction, UniformMovementCost}
import com.rayrobdod.boardGame.{TokenMovementCost, PhysicalStrikeCost, MagicalStrikeCost}
import scala.collection.immutable.{Seq => ISeq}

/*
 *        Class Name        | Move            | Attack
 * -------------------------+-----------------+---------
 *    PassibleSpaceClass    | Yes             | Yes
 *    UnitAwareSpaceClass   | If not occupied | Yes
 *   ImpassibleSpaceClass   | No              | No
 * AttackableOnlySpaceClass | No              | Yes
 *    NoStandOnSpaceClass   | If Flying       | Yes
 */


/**
 * @author Raymond Dodge
 * @version 13 Jan 2012 - destropying Space - replacing with SpaceClass
 * @version 14 Jul 2012 - renaming from SpaceClass to PassibleSpaceClass
 */
class PassibleSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
		with UniformMovementCost

/**
 * A constructor and deconstructor of SpaceClasses
 * @author Raymond Dodge
 * @version 13 Jan 2012
 * @version 14 Jul 2012 - renaming from SpaceClass to PassibleSpaceClass
 * @version 19 Jul 2012 - fixing lack of name update
 */
object PassibleSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[PassibleSpaceClass]}
	val apply = new PassibleSpaceClass()
}

/**
 * A Space that is easy to get onto if no other unit shares the space,
 * but impossible to get onto if another unit is on the space
 * @author Raymond Dodge
 * @version 20 Mar 2012
 * @version 05 Apr 2012 - #movementCost(Token) => #cost(Token, TypeOfCost). Also, only TokenMovementCost gets inflated value 
 */
class UnitAwareSpaceClass(tokens:ListOfTokens) extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	private def isOccupied:Boolean = {
		val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.
				find{_.currentSpace.typeOfSpace == this}
		
		!tokenOnThis.isEmpty
	}
	
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		costType match {
			case TokenMovementCost => {
				if (this.isOccupied) {1000} else {1}
			}
			case _ => 1
		}
	}
}

/**
 * A constructor and deconstructor for UnitAwareSpaceClasses
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
object UnitAwareSpaceClass extends SpaceClassConstructor
{
	/* change to the actual list of tokens */
	var tokens:MutableListOfTokens = new MutableListOfTokens()
	
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[UnitAwareSpaceClass]}
	def apply = new UnitAwareSpaceClass(tokens)
}

/**
 * A SpaceClass that is impossible to move to or attack through
 * @author Raymond Dodge
 * @version 11 Jun 2012
 */
class ImpassibleSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		1000
	}
}

/**
 * A constructor and deconstructor for ImpassibleSpaceClass
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
object ImpassibleSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[ImpassibleSpaceClass]}
	def apply = new ImpassibleSpaceClass
}

/**
 * A SpaceClass that is impossible to move through but is possible to attack through
 * @author Raymond Dodge
 * @version 14 Jul 2012
 */
class AttackableOnlySpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		costType match {
			case PhysicalStrikeCost => 1
			case MagicalStrikeCost => 1
			case _ => 1000
		}
	}
}

/**
 * A constructor and deconstructor for AttackableOnlySpaceClass
 * @author Raymond Dodge
 * @version 14 Jul 2012
 * @version 18 Nov 2012 - unapply now checks for AttackableOnlySpaceClass, not ImpassibleSpaceClass
 * @version 19 Nov 2012 - apply now creates AttackableOnlySpaceClass, not ImpassibleSpaceClass
 */
object AttackableOnlySpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[AttackableOnlySpaceClass]}
	def apply = new AttackableOnlySpaceClass
}

/**
 * A SpaceClass that cannot be stood on
 * @author Raymond Dodge
 * @version 14 Jul 2012
 */
class NoStandOnSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	def unitIsFlying(token:BoardGameToken) = false;
	
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		costType match {
			case TokenMovementCost => if (unitIsFlying(tokenMoving)) {1} else {1000}
			case _ => 1
		}
	}
}

/**
 * A constructor and deconstructor for NoStandOnSpaceClass
 * @author Raymond Dodge
 * @version 14 Jul 2012
 * @version 18 Nov 2012 - unapply now checks for NoStandOnSpaceClass, not ImpassibleSpaceClass 
 * @version 19 Nov 2012 - unapply now creates NoStandOnSpaceClass, not ImpassibleSpaceClass 
 */
object NoStandOnSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[NoStandOnSpaceClass]}
	def apply = new NoStandOnSpaceClass
}
