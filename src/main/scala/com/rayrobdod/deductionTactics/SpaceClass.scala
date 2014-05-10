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
import com.rayrobdod.boardGame.{Space => BoardGameSpace}
import com.rayrobdod.boardGame.SpaceClassMatcher
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

/**
 * @version a.6.0
 */
final case class SpaceClass(
	val toString:String,
	val canEnter:SpaceClass.CostFunctionFactory,
	val canAttack:SpaceClass.CostFunctionFactory
)

/**
 * Things used in common by other space classes.
 * @since a.5.2
 * @version a.6.0
 */
object SpaceClass {
	val normalPassage = 1
	val impossiblePassage = 1000
	
	type CostFunction = BoardGameSpace.CostFunction[SpaceClass]
	type CostFunctionFactory = Function1[Token, CostFunction]
	
	final case class ConstantCostFunction(cost:Int) extends CostFunction {
		def apply(from:BoardGameSpace[SpaceClass], to:BoardGameSpace[SpaceClass]):Int = cost
	}
	final class SinglePassageCostFunction(tokens:ListOfTokens) extends CostFunction {
		def apply(from:BoardGameSpace[SpaceClass], to:BoardGameSpace[SpaceClass]):Int = {
			
			val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.find{_.currentSpace == to}
			tokenOnThis.map{(a) => impossiblePassage}.getOrElse{normalPassage}
		}
	}
	final case class MaxCostFunction(a:CostFunction, b:CostFunction) extends CostFunction {
		def apply(from:BoardGameSpace[SpaceClass], to:BoardGameSpace[SpaceClass]):Int = {
			return math.max(a(from, to), b(from, to))
		}
	}
	
	
	
	final case class ConstantCostFunctionFactory(f:CostFunction) extends CostFunctionFactory  {
		def apply(t:Token):CostFunction = f
	}
	
	final class FriendPassageCostFunctionFactory(tokens:ListOfTokens) extends CostFunctionFactory {
		def apply(myToken:Token):CostFunction = new CostFunction() {
			def apply(from:BoardGameSpace[SpaceClass], to:BoardGameSpace[SpaceClass]):Int = {
				val myTeam = tokens.tokens.zipWithIndex.find{_._1.contains(myToken)}.map{_._2}
				val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.find{
						_.currentSpace.typeOfSpace == to
				}
				val otherTeam = tokenOnThis.map{(other:Token) => 
					tokens.tokens.zipWithIndex.find{_._1.contains(other)}.map{_._2}
				}.flatten
				
				val canEnter = myTeam.isEmpty || otherTeam.isEmpty || otherTeam.head == myTeam.get
				
				if (canEnter) {normalPassage} else {impossiblePassage}
			}
		}
	}
	
	object IsFlyingCostFunctionFactory extends CostFunctionFactory {
		def isFlying(t:Token):Boolean = {
			t.tokenClass.map{(a) => a.body == BodyTypes.Avian}.getOrElse(false)
		}
		
		def apply(t:Token):CostFunction = new ConstantCostFunction(if (isFlying(t)) {normalPassage} else {impossiblePassage})
	}
	
	final case class IsElementCostFunctionFactory(val element:Elements.Element) extends CostFunctionFactory {
		def isElement(t:Token):Boolean = {
			t.tokenClass.map{(a) => a.atkElement == element}.getOrElse(false)
		}
		
		def apply(t:Token):CostFunction = new ConstantCostFunction(if (isElement(t)) {normalPassage} else {impossiblePassage})
	}
	
	final case class MaxCostFunctionFactory(a:CostFunctionFactory, b:CostFunctionFactory) extends CostFunctionFactory {
		def apply(t:Token):CostFunction = new MaxCostFunction(a(t), b(t))
	}

}

import SpaceClass._


/**
 * @since a.6.0
 */
class SpaceClassFactory(tokens:ListOfTokens) {
	
	def apply(reference:String):SpaceClass = reference match {
		case " " => UniPassageSpaceClass(tokens)
		case "s" => SlowPassageSpaceClass(tokens)
		case "|" => ImpassibleSpaceClass(tokens)
		case ":" => AttackOnlySpaceClass(tokens)
		case "." => FlyingPassageSpaceClass(tokens)
		case "f" => FirePassageSpaceClass(tokens)
		case _ => SpaceClass(
				"Unknown",
				MaxCostFunctionFactory(
					ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens)),
					ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens))
				),
				ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
		)
	}
}

/**
 * @since a.6.0
 */
object SpaceClassMatcherFactory extends com.rayrobdod.boardGame.swingView.SpaceClassMatcherFactory[SpaceClass] {
	
	def apply(reference:String):com.rayrobdod.boardGame.SpaceClassMatcher[SpaceClass] = reference match {
		case " " => UniPassageSpaceClass
		case "s" => SlowPassageSpaceClass
		case "|" => ImpassibleSpaceClass
		case ":" => AttackOnlySpaceClass
		case "." => FlyingPassageSpaceClass
		case "f" => FirePassageSpaceClass
		case _ => new SpaceClassMatcher[SpaceClass]{ def unapply(sc:SpaceClass) = false }
	}
}


/**
 * constructs and deconstructs a spaceclass that allows units through,
 * even if a unit is already standing on this space
 * @author Raymond Dodge
 * @version a.6.0
 */
object FreePassageSpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Free Passage",
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage)),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Free Passage",
			ConstantCostFunctionFactory(ConstantCostFunction(moveCost)),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) =>  (moveCost == normalPassage) && (atkCost == normalPassage)
		case _ => false
	}
}

/**
 * constructs and deconstructs a spaceclass that allows units through,
 * even if a friendly unit is already standing on this space
 * @author Raymond Dodge
 * @version a.6.0
 */
object AllyPassageSpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Ally Passage",
		new FriendPassageCostFunctionFactory(tokens),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Ally Passage",
			_:FriendPassageCostFunctionFactory,
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (atkCost == normalPassage)
		case _ => false
	}
}

/**
 * constructs and deconstructs a spaceclass that allows any through
 * @author Raymond Dodge
 * @version a.6.0
 */
object UniPassageSpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Single Passage",
		ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens)),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Single Passage",
			ConstantCostFunctionFactory(_:SinglePassageCostFunction),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (atkCost == normalPassage)
		case _ => false
	}
}

/**
 * constructs and deconstructs a spaceclass that allows nothing through
 * @author Raymond Dodge
 * @version a.6.0
 */
object ImpassibleSpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Impassible",
		MaxCostFunctionFactory(
			ConstantCostFunctionFactory(ConstantCostFunction(impossiblePassage)),
			ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens))
		),
		ConstantCostFunctionFactory(ConstantCostFunction(impossiblePassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Impassible",
			MaxCostFunctionFactory(
				ConstantCostFunctionFactory(ConstantCostFunction(moveCost)),
				ConstantCostFunctionFactory(_:SinglePassageCostFunction)
			),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (moveCost == impossiblePassage) && (atkCost == impossiblePassage)
		case _ => false
	}
}

/**
 * constructs and deconstructs a spaceclass that can be attacked,
 * but not stood on
 * @author Raymond Dodge
 * @version a.6.0
 */
object AttackOnlySpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Attack-only",
		MaxCostFunctionFactory(
			ConstantCostFunctionFactory(ConstantCostFunction(impossiblePassage)),
			ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens))
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Attack-only",
			MaxCostFunctionFactory(
				ConstantCostFunctionFactory(ConstantCostFunction(moveCost)),
				ConstantCostFunctionFactory(_:SinglePassageCostFunction)
			),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (moveCost == impossiblePassage) && (atkCost == normalPassage)
		case _ => false
	}
}

/**
 * constructs and deconstructs a spaceclass that allows avian-bodied units through
 * @author Raymond Dodge
 * @version a.6.0
 */
object FlyingPassageSpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Flying Passage",
		MaxCostFunctionFactory(
			IsFlyingCostFunctionFactory,
			ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens))
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Flying Passage",
			MaxCostFunctionFactory(
				IsFlyingCostFunctionFactory,
				ConstantCostFunctionFactory(_:SinglePassageCostFunction)
			),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (atkCost == normalPassage)
		case _ => false
	}
}

/**
 * constructs and deconstructs a spaceclass that allows fire elementals through
 * @author Raymond Dodge
 * @version a.6.0
 */
object FirePassageSpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Fire Passage",
		MaxCostFunctionFactory(
			IsElementCostFunctionFactory(Elements.Fire),
			ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens))
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Fire Passage",
			MaxCostFunctionFactory(
				IsElementCostFunctionFactory(Elements.Fire),
				ConstantCostFunctionFactory(_:SinglePassageCostFunction)
			),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (atkCost == normalPassage)
		case _ => false
	}
}

/**
 * constructs and deconstructs a spaceclass that allows anything through,
 * but not as quickly as usual
 * @author Raymond Dodge
 * @version a.6.0
 */
object SlowPassageSpaceClass extends SpaceClassMatcher[SpaceClass] {
	def apply(tokens:ListOfTokens) = SpaceClass("Slow Passage",
		MaxCostFunctionFactory(
			ConstantCostFunctionFactory(ConstantCostFunction(normalPassage * 2)),
			ConstantCostFunctionFactory(new SinglePassageCostFunction(tokens))
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Slow Passage",
			MaxCostFunctionFactory(
				ConstantCostFunctionFactory(ConstantCostFunction(moveCost)),
				ConstantCostFunctionFactory(_:SinglePassageCostFunction)
			),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (moveCost == normalPassage * 2) && (atkCost == normalPassage)
		case _ => false
	}
}
