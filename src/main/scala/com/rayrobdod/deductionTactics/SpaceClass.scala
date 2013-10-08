package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.{SpaceClassConstructor, SpaceClass => BoardGameSpaceClass,
		Token => BoardGameToken, TypeOfCost}
import com.rayrobdod.boardGame.{NoLandOnAction, NoPassOverAction, UniformMovementCost}
import com.rayrobdod.boardGame.{TokenMovementCost, PhysicalStrikeCost, MagicalStrikeCost}
import scala.collection.immutable.{Seq => ISeq}

/*
 *        Class Name        | Move            | Attack
 * -------------------------+-----------------+---------
 *    PassibleSpaceClass    | Yes             |  Yes
 *    UnitAwareSpaceClass   | If not occupied |  Yes
 *   ImpassibleSpaceClass   | No              |  No
 * AttackableOnlySpaceClass | No              |  Yes
 *    NoStandOnSpaceClass   | If Flying       |  Yes
 * FireRestrictedSpaceClass | If Flying/Fire  |  Yes
 *    BurningSpaceClass     | IT BURNS!       |  Yes
 */


/**
 * @author Raymond Dodge
 * @version 13 Jan 2012 - destropying Space - replacing with SpaceClass
 * @version 14 Jul 2012 - renaming from SpaceClass to PassibleSpaceClass
 * @version 2013 Mar 04 - implementing equals, canEqual and hashCode
 */
class PassibleSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
		with UniformMovementCost
{
	override def hashCode = 5;
	def canEqual(x:Any) = {x.isInstanceOf[PassibleSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[PassibleSpaceClass].canEqual(this)
	}
}

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
 *
 * @note every Space will need its own instance of this class
 * @author Raymond Dodge
 * @version 20 Mar 2012
 * @version 05 Apr 2012 - #movementCost(Token) => #cost(Token, TypeOfCost). Also, only TokenMovementCost gets inflated value 
 * @version 2013 Mar 04 - implementing equals, canEqual and hashCode
 * @version 2013 Jun 03 - unimplementing equals, canEqual and hashCode; when all instances are treated as equal, the UnitAwareSpaceClass
			will only allow tokens on when no unit is on any UnitAwareSpaceClass
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
	
/*	override def hashCode = 4;
	def canEqual(x:Any) = {x.isInstanceOf[UnitAwareSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) &&
		x.asInstanceOf[UnitAwareSpaceClass].canEqual(this) &&
//		x.asInstanceOf[UnitAwareSpaceClass].tokens == this.tokens
		true
	}
*/
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
 * @version 2013 Mar 04 - implementing equals, canEqual and hashCode
 */
class ImpassibleSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		1000
	}
	
	override def hashCode = 3;
	def canEqual(x:Any) = {x.isInstanceOf[ImpassibleSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[ImpassibleSpaceClass].canEqual(this)
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
 * @version 2013 Mar 04 - implementing equals, canEqual and hashCode
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
	
	override def hashCode = 2;
	def canEqual(x:Any) = {x.isInstanceOf[AttackableOnlySpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[AttackableOnlySpaceClass].canEqual(this)
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
 * @version 2013 Mar 04 - implementing equals, canEqual and hashCode
 */
class NoStandOnSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	import NoStandOnSpaceClass.unitIsFlying
	
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		costType match {
			case TokenMovementCost => if (unitIsFlying(tokenMoving)) {1} else {1000}
			case _ => 1
		}
	}
	
	override def hashCode = 1;
	def canEqual(x:Any) = {x.isInstanceOf[NoStandOnSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[NoStandOnSpaceClass].canEqual(this)
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
	def unitIsFlying(token:BoardGameToken) = false;
	
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[NoStandOnSpaceClass]}
	def apply = new NoStandOnSpaceClass
}



/**
 * A SpaceClass that cannot be stood on except by a particular element
 * @author Raymond Dodge
 * @version 2013 Jul 13
 */
class ElementRestrictedSpaceClass(val element:Elements.Element) extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	private val someElement = Some(element)
	
	import NoStandOnSpaceClass.unitIsFlying
	
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		tokenMoving match {
			case x:Token => costType match {
				case TokenMovementCost => if (unitIsFlying(tokenMoving) || x.tokenClass.atkElement == someElement) {1} else {1000}
				case _ => 1
			}
			case _ => 1000
		}
	}
	
	override def hashCode = 100 + element.hashCode;
	def canEqual(x:Any) = {x.isInstanceOf[ElementRestrictedSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[ElementRestrictedSpaceClass].canEqual(this) &&
				x.asInstanceOf[ElementRestrictedSpaceClass].element == element
	}
}

/**
 * A constructor and deconstructor of SpaceClasses
 * @author Raymond Dodge
 * @version 2013 Jul 13
 */
object FireRestrictedSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[ElementRestrictedSpaceClass]}
	val apply = new ElementRestrictedSpaceClass(Elements.Fire)
}
