package com.rayrobdod.deductionTactics

import com.rayrobdod.boardGame.{SpaceClassConstructor, SpaceClass => BoardGameSpaceClass,
		NoLandOnAction, NoPassOverAction, RectangularSpaceConstructor,
		UniformMovementCost, Token => BoardGameToken, TypeOfCost, TokenMovementCost}
import scala.collection.immutable.{Seq => ISeq}

/**
 * @author Raymond Dodge
 * @version 13 Jan 2012 - destropying Space - replacing with SpaceClass
 */
class SpaceClass extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
		with UniformMovementCost

/**
 * A constructor and deconstructor of SpaceClasses
 * @author Raymond Dodge
 * @version 13 Jan 2012
 */
object SpaceClass extends SpaceClassConstructor
{
	def unapply(a:BoardGameSpaceClass) = {a.isInstanceOf[SpaceClass]}
	val apply = new SpaceClass()
}

/**
 * A Space that is easy to get onto if no other unit shares the space,
 * but impossible to get onto if another unit is on the space
 * @author Raymond Dodge
 * @version 20 Mar 2012
 * @version 05 Apr 2012 - #movementCost(Token) => #cost(Token, TypeOfCost). Also, only TokenMovmentCost gets inflated value 
 */
class UnitAwareSpaceClass(tokens:ListOfTokens) extends BoardGameSpaceClass
		with NoLandOnAction
		with NoPassOverAction
{
	override def cost(tokenMoving:BoardGameToken, costType:TypeOfCost) = {
		costType match {
			case TokenMovementCost => {
			
				val tokenOnThis:Option[Token] = tokens.
						aliveTokens.
						flatten.
						find{_.currentSpace.typeOfSpace == this}
				
				if (tokenOnThis == None) { 1 }
			//	else if (tokenOnThis == Some(tokenMoving)) { 1 }
				else { 1000 }
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
