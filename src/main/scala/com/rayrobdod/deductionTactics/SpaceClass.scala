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

import com.rayrobdod.boardGame.{Token => BoardGameToken}
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
 *   FirePassageSpaceClass  | If Fire         |  Yes
 *     BurningSpaceClass    | IT BURNS!       |  Yes
 */

trait SpaceClass {
	def canEnter:SpaceClass.CanEnterType
	def canAttack:SpaceClass.CanEnterType
}

/**
 * Things used in common by other space classes.
 * @since a.5.2
 */
object SpaceClass {
	val normalPassage = 1
	val impossiblePassage = 1000
	
	
	type CanEnterType = Function2[SpaceClass, BoardGameToken[SpaceClass], Boolean]
	private type CanEnterType2 = scala.runtime.AbstractFunction2[SpaceClass, BoardGameToken[SpaceClass], Boolean]
	
	// change to the actual list of tokens
	// I can guarentee that this is a memory leak
	// move to the main method(?)
	var tokens:MutableListOfTokens = new MutableListOfTokens()
	
	
	final class FriendPassageEntry(tokens:ListOfTokens) extends CanEnterType2 {
		def apply(space:SpaceClass, myToken:BoardGameToken[SpaceClass]):Boolean = {
			val myTeam = tokens.tokens.zipWithIndex.find{_._1.contains(myToken)}.map{_._2}
			val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.find{
					_.currentSpace.typeOfSpace == space
			}
			val otherTeam = tokenOnThis.map{(other:Token) => 
				tokens.tokens.zipWithIndex.find{_._1.contains(other)}.map{_._2}
			}.flatten
			
			myTeam.isEmpty || otherTeam.isEmpty || otherTeam.head == myTeam.get
		}
	}
	
	final class SinglePassageEntry(tokens:ListOfTokens) extends CanEnterType2 {
		def apply(space:SpaceClass[A], myToken:BoardGameToken[SpaceClass]):Boolean = {
			
			val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.
					find{_.currentSpace.typeOfSpace == space}
			
			tokenOnThis.isEmpty
		}
	}
	
	object IsFlying extends CanEnterType2 {
		def apply(space:SpaceClass[A], myToken:BoardGameToken[SpaceClass]):Boolean = {
			myToken match {
				case x:Token => x.tokenClass.body.map{_ == BodyTypes.Avian}.getOrElse(false)
				case _ => false
			}
		}
	}
	
	case class IsElement(val element:Elements.Element) extends CanEnterType2 {
		def apply(space:SpaceClass, myToken:BoardGameToken[SpaceClass]):Boolean = {
			myToken match {
				case x:Token => x.tokenClass.atkElement.map{_ == element}.getOrElse(false)
				case _ => false
			}
		}
	}
	
	final case class CanEnterAnd(a:CanEnterType, b:CanEnterType) extends CanEnterType2 {
		def apply(space:SpaceClass, myToken:BoardGameToken[SpaceClass]):Boolean = {
			a(space, myToken) && b(space, myToken);
		}
	}
	
	val funFalse:CanEnterType = {(a,b) => false}
	val funTrue:CanEnterType  = {(a,b) => true }
}

import SpaceClass._



/**
 * A SpaceClass where entry, of either a token or an attack,
 * is either normal or impossible.
 * @since a.5.2
 */
case class BooleanSpaceClass(canEnter:CanEnterType, canAttack:Boolean)
{
	override def cost(tokenMoving:BoardGameToken[SpaceClass], costType:TypeOfCost) = {
		costType match {
			case TokenMovementCost => {
				if (canEnter(this, tokenMoving)) {normalPassage} else {impossiblePassage}
			}
			case PhysicalStrikeCost => if (canAttack) {normalPassage} else {impossiblePassage}
			case MagicalStrikeCost  => if (canAttack) {normalPassage} else {impossiblePassage}
			case _ => normalPassage
		}
	}
}



/**
 * constructs and deconstructs a spaceclass that allows units through,
 * even if a unit is already standing on this space
 * @author Raymond Dodge
 * @version a.5.2
 */
object FreePassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:SpaceClass) = a match {
		case BooleanSpaceClass(a, true) => a == funTrue
		case _ => false
	}
	val apply = new BooleanSpaceClass(funTrue, true)
}

/**
 * constructs and deconstructs a spaceclass that allows units through,
 * even if a friendly unit is already standing on this space
 * @author Raymond Dodge
 * @version a.5.2
 */
object AllyPassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:SpaceClass) = a match {
		case BooleanSpaceClass(a, true) => a.isInstanceOf[FriendPassageEntry]
		case _ => false
	}
	def apply = new BooleanSpaceClass(new FriendPassageEntry(SpaceClass.tokens), true)
}

/**
 * constructs and deconstructs a spaceclass that allows any through
 * @author Raymond Dodge
 * @version a.5.2
 */
object UniPassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:SpaceClass) = a match {
		case BooleanSpaceClass(a, true) => a.isInstanceOf[SinglePassageEntry]
		case _ => false
	}
	def apply = new BooleanSpaceClass(new SinglePassageEntry(SpaceClass.tokens), true)
}

/**
 * constructs and deconstructs a spaceclass that allows nothing through
 * @author Raymond Dodge
 * @version a.5.2
 */
object ImpassibleSpaceClass extends SpaceClassConstructor
{
	def unapply(a:SpaceClass) = a match {
		case BooleanSpaceClass(a, false) => a == funFalse
		case _ => false
	}
	val apply = new BooleanSpaceClass(funFalse, false)
}

/**
 * constructs and deconstructs a spaceclass that can be attacked,
 * but not stood on
 * @author Raymond Dodge
 * @version a.5.2
 */
object AttackOnlySpaceClass extends SpaceClassConstructor
{
	def unapply(a:SpaceClass) = a match {
		case BooleanSpaceClass(a, true) => a == funFalse
		case _ => false
	}
	val apply = new BooleanSpaceClass(funFalse, true)
}

/**
 * constructs and deconstructs a spaceclass that allows avian-bodied units through
 * @author Raymond Dodge
 * @version a.5.2
 */
object FlyingPassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:SpaceClass) = a match {
		case BooleanSpaceClass(CanEnterAnd(a,b), true) => a.isInstanceOf[SinglePassageEntry] && b == IsFlying
		case _ => false
	}
	def apply = new BooleanSpaceClass(CanEnterAnd(
			new SinglePassageEntry(SpaceClass.tokens),
			IsFlying
	), true)
}

/**
 * constructs and deconstructs a spaceclass that allows fire elementals through
 * @author Raymond Dodge
 * @version a.5.2
 */
object FirePassageSpaceClass extends SpaceClassConstructor
{
	def unapply(a:SpaceClass) = a match {
		case BooleanSpaceClass(CanEnterAnd(a,IsElement(b)), true) => a.isInstanceOf[SinglePassageEntry] && b == Elements.Fire
		case _ => false
	}
	def apply = new BooleanSpaceClass(CanEnterAnd(
			new SinglePassageEntry(SpaceClass.tokens),
			IsElement(Elements.Fire)
	), true)
}

/**
 * constructs and deconstructs a spaceclass that allows anything through,
 * but not as quickly as usual
 * @author Raymond Dodge
 * @since a.5.2
 */
object SlowPassageSpaceClass extends SpaceClassConstructor
{
	private class MySpaceClass(tokens:MutableListOfTokens) extends SpaceClass with NoLandOnAction with NoPassOverAction
	{
		val passageEntry = new SinglePassageEntry(tokens);
		
		override def cost(tokenMoving:BoardGameToken[SpaceClass], costType:TypeOfCost) = {
			costType match {
				case TokenMovementCost => {
					if (passageEntry(this, tokenMoving)) {normalPassage * 2} else {impossiblePassage}
				}
				case _ => normalPassage
			}
		}
	}
	
	def unapply(a:SpaceClass) = a match {
		case x:MySpaceClass => true
		case _ => false
	}
	def apply:SpaceClass = new MySpaceClass(SpaceClass.tokens)
	
}
