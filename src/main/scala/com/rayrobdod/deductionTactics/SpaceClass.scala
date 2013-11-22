package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.{SpaceClassConstructor, SpaceClass => BoardGameSpaceClass,
		Token => BoardGameToken, TypeOfCost}
import com.rayrobdod.boardGame.{NoLandOnAction, NoPassOverAction, UniformMovementCost}
import com.rayrobdod.boardGame.{TokenMovementCost, PhysicalStrikeCost, MagicalStrikeCost}
import scala.collection.immutable.{Seq => ISeq}

/*
 *        Class Name        | Move            | Attack
 * -------------------------+-----------------+---------
 *  FreePassageSpaceClass   | Yes             |  Yes
 *   AllyPassageSpaceClass  | If no opponent  |  Yes
 *    UniPassageSpaceClass  | If not occupied |  Yes
 *   ImpassableSpaceClass   | No              |  No
 *   AttackOnlySpaceClass   | No              |  Yes
 *  FlyingPassageSpaceClass | If Flying       |  Yes
 *   FirePassageSpaceClass  | If Flying/Fire  |  Yes
 *     BurningSpaceClass    | IT BURNS!       |  Yes
 */


/**
 * @author Raymond Dodge
 * @version a.5.2
 */
class FreePassageSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
		with UniformMovementCost
{
	override def hashCode = 5;
	def canEqual(x:Any) = {x.isInstanceOf[FreePassageSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[FreePassageSpaceClass].canEqual(this)
	}
}

/**
 * A constructor and deconstructor of SpaceClasses
 * @author Raymond Dodge
 * @version a.5.2
 */
object FreePassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[FreePassageSpaceClass]}
	val apply = new FreePassageSpaceClass()
}

/**
 * A Space that is easy to get onto if no other unit shares the space,
 * but impossible to get onto if another unit is on the space
 *
 * @note every Space will need its own instance of this class
 * @author Raymond Dodge
 * @version a.5.2
 */
class AllyPassageSpaceClass(tokens:ListOfTokens) extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	private def isOccupied(myToken:BoardGameToken):Boolean = {
		val myTeam = tokens.tokens.zipWithIndex.find{_._1.contains(myToken)}.map{_._2}
		val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.find{
				_.currentSpace.typeOfSpace == this
		}
		val otherTeam = tokenOnThis.map{(other:Token) => 
			tokens.tokens.zipWithIndex.find{_._1.contains(other)}.map{_._2}
		}.flatten
		
		!myTeam.isEmpty && !otherTeam.isEmpty && otherTeam.head != myTeam.get
	}
	
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		costType match {
			case TokenMovementCost => {
				if (this.isOccupied(tokenMoving)) {1000} else {1}
			}
			case _ => 1
		}
	}
	
/*	override def hashCode = 4;
	def canEqual(x:Any) = {x.isInstanceOf[AllyPassageSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) &&
		x.asInstanceOf[AllyPassageSpaceClass].canEqual(this) &&
//		x.asInstanceOf[AllyPassageSpaceClass].tokens == this.tokens
		true
	}
*/
}

/**
 * A constructor and deconstructor for AllyPassageSpaceClass
 * @author Raymond Dodge
 * @version a.5.2
 */
object AllyPassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[AllyPassageSpaceClass]}
	def apply = new AllyPassageSpaceClass(UniPassageSpaceClass.tokens)
}

/**
 * A Space that is easy to get onto if no other unit shares the space,
 * but impossible to get onto if another unit is on the space
 *
 * @note every Space will need its own instance of this class
 * @author Raymond Dodge
 * @version a.5.2
 */
class UniPassageSpaceClass(tokens:ListOfTokens) extends BoardGameSpaceClass
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
	def canEqual(x:Any) = {x.isInstanceOf[UniPassageSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) &&
		x.asInstanceOf[UniPassageSpaceClass].canEqual(this) &&
//		x.asInstanceOf[UniPassageSpaceClass].tokens == this.tokens
		true
	}
*/
}

/**
 * A constructor and deconstructor for UniPassageSpaceClasses
 * @author Raymond Dodge
 * @version a.5.2
 */
object UniPassageSpaceClass extends SpaceClassConstructor
{
	/* change to the actual list of tokens */
	var tokens:MutableListOfTokens = new MutableListOfTokens()
	
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[UniPassageSpaceClass]}
	def apply = new UniPassageSpaceClass(tokens)
}

/**
 * A SpaceClass that is impossible to move to or attack through
 * @author Raymond Dodge
 * @version a.5.2
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
 * @version a.5.2
 */
object ImpassibleSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[ImpassibleSpaceClass]}
	def apply = new ImpassibleSpaceClass
}

/**
 * A SpaceClass that is impossible to move through but is possible to attack through
 * @author Raymond Dodge
 * @version a.5.2
 */
class AttackOnlySpaceClass extends BoardGameSpaceClass
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
	def canEqual(x:Any) = {x.isInstanceOf[AttackOnlySpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[AttackOnlySpaceClass].canEqual(this)
	}
}

/**
 * A constructor and deconstructor for AttackOnlySpaceClass
 * @author Raymond Dodge
 * @version a.5.2
 */
object AttackOnlySpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[AttackOnlySpaceClass]}
	def apply = new AttackOnlySpaceClass
}

/**
 * A SpaceClass that cannot be stood on
 * @author Raymond Dodge
 * @version a.5.2
 */
class FlyingPassageSpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	import FlyingPassageSpaceClass.unitIsFlying
	
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		costType match {
			case TokenMovementCost => if (unitIsFlying(tokenMoving)) {1} else {1000}
			case _ => 1
		}
	}
	
	override def hashCode = 1;
	def canEqual(x:Any) = {x.isInstanceOf[FlyingPassageSpaceClass]}
	override def equals(x:Any) = {
		this.canEqual(x) && x.asInstanceOf[FlyingPassageSpaceClass].canEqual(this)
	}
}

/**
 * A constructor and deconstructor for FlyingPassageSpaceClass
 * @author Raymond Dodge
 * @version a.5.2
 */
object FlyingPassageSpaceClass extends SpaceClassConstructor
{
	def unitIsFlying(token:BoardGameToken) = false;
	
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[FlyingPassageSpaceClass]}
	def apply = new FlyingPassageSpaceClass
}



/**
 * A SpaceClass that cannot be stood on except by a particular element
 * @author Raymond Dodge
 * @version a.5.2
 */
class ElementRestrictedSpaceClass(val element:Elements.Element) extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	private val someElement = Some(element)
	
	import FlyingPassageSpaceClass.unitIsFlying
	
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
 * @version a.5.2
 */
object FirePassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[ElementRestrictedSpaceClass]}
	val apply = new ElementRestrictedSpaceClass(Elements.Fire)
}
