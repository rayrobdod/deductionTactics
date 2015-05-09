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
 * 
 * @version a.6.0
 * 
 * @constructor
 * @param toString a name for this space class
 * @param canEnter  a cost determining how hard it is to attack this
 *        space. Higher is more difficult.
 * @param canAttack a cost determining how hard it is to attack this
 *        space. Higher is more difficult.
 */
final case class SpaceClass(
	override val toString:String,
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
	type CostFunctionFactory = Function2[Token, ListOfTokens, CostFunction]
	
	final case class ConstantCostFunction(cost:Int) extends CostFunction {
		override def apply(from:BoardGameSpace[_ <: SpaceClass], to:BoardGameSpace[_ <: SpaceClass]):Int = cost
	}
	final class SinglePassageCostFunction(tokens:ListOfTokens) extends CostFunction {
		override def apply(from:BoardGameSpace[_ <: SpaceClass], to:BoardGameSpace[_ <: SpaceClass]):Int = {
			
			val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.find{_.currentSpace == to}
			tokenOnThis.map{(a) => impossiblePassage}.getOrElse{normalPassage}
		}
	}
	final case class MaxCostFunction(a:CostFunction, b:CostFunction) extends CostFunction {
		override def apply(from:BoardGameSpace[_ <: SpaceClass], to:BoardGameSpace[_ <: SpaceClass]):Int = {
			return math.max(a(from, to), b(from, to))
		}
	}
	
	
	
	final case class ConstantCostFunctionFactory(f:CostFunction) extends CostFunctionFactory  {
		override def apply(t:Token, l:ListOfTokens):CostFunction = f
	}
	
	object FriendPassageCostFunctionFactory extends CostFunctionFactory {
		override def apply(myToken:Token, tokens:ListOfTokens):CostFunction = new CostFunction() {
			def apply(from:BoardGameSpace[_ <: SpaceClass], to:BoardGameSpace[_ <: SpaceClass]):Int = {
				val myTeam = tokens.tokens.zipWithIndex.find{_._1.contains(myToken)}.map{_._2}
				val tokenOnThis:Option[Token] = tokens.aliveTokens.flatten.find{
						_.currentSpace == to
				}
				val otherTeam = tokenOnThis.map{(other:Token) => 
					tokens.tokens.zipWithIndex.find{_._1.contains(other)}.map{_._2}
				}.flatten
				
				val canEnter = myTeam.isEmpty || otherTeam.isEmpty || otherTeam.head == myTeam.get
				
				if (canEnter) {normalPassage} else {impossiblePassage}
			}
		}
	}
	
	object SinglePassageCostFunctionFactory extends CostFunctionFactory {
		override def apply(myToken:Token, tokens:ListOfTokens):CostFunction =
			new SinglePassageCostFunction(tokens)
	}
	
	object IsFlyingCostFunctionFactory extends CostFunctionFactory {
		def isFlying(t:Token):Boolean = {
			t.tokenClass.map{(a) => a.body == BodyTypes.Avian}.getOrElse(false)
		}
		
		override def apply(t:Token, ts:ListOfTokens):CostFunction = new ConstantCostFunction(if (isFlying(t)) {normalPassage} else {impossiblePassage})
	}
	
	final case class IsElementCostFunctionFactory(val element:Elements.Element) extends CostFunctionFactory {
		def isElement(t:Token):Boolean = {
			t.tokenClass.map{(a) => a.atkElement == element}.getOrElse(false)
		}
		
		override def apply(t:Token, ts:ListOfTokens):CostFunction = new ConstantCostFunction(if (isElement(t)) {normalPassage} else {impossiblePassage})
	}
	
	final case class MaxCostFunctionFactory(a:CostFunctionFactory, b:CostFunctionFactory) extends CostFunctionFactory {
		override def apply(t:Token, l:ListOfTokens):CostFunction = new MaxCostFunction(a(t,l), b(t,l))
	}

}

import SpaceClass._


/**
 * @since a.6.0
 */
object SpaceClassFactory {
	
	def apply(reference:String):SpaceClass = reference match {
		case " " => UniPassageSpaceClass.apply
		case "s" => SlowPassageSpaceClass.apply
		case "|" => ImpassibleSpaceClass.apply
		case ":" => AttackOnlySpaceClass.apply
		case "." => FlyingPassageSpaceClass.apply
		case "f" => FirePassageSpaceClass.apply
		case _ => SpaceClass(
				"Unknown",
				MaxCostFunctionFactory(
					SinglePassageCostFunctionFactory,
					SinglePassageCostFunctionFactory
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
	def apply = SpaceClass("Free Passage",
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
	def apply = SpaceClass("Ally Passage",
		FriendPassageCostFunctionFactory,
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Ally Passage",
			FriendPassageCostFunctionFactory,
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
	def apply = SpaceClass("Single Passage",
		SinglePassageCostFunctionFactory,
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Single Passage",
			SinglePassageCostFunctionFactory,
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
	def apply = SpaceClass("Impassible",
		MaxCostFunctionFactory(
			ConstantCostFunctionFactory(ConstantCostFunction(impossiblePassage)),
			SinglePassageCostFunctionFactory
		),
		ConstantCostFunctionFactory(ConstantCostFunction(impossiblePassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Impassible",
			MaxCostFunctionFactory(
				ConstantCostFunctionFactory(ConstantCostFunction(moveCost)),
				SinglePassageCostFunctionFactory
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
	def apply = SpaceClass("Attack-only",
		MaxCostFunctionFactory(
			ConstantCostFunctionFactory(ConstantCostFunction(impossiblePassage)),
			SinglePassageCostFunctionFactory
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Attack-only",
			MaxCostFunctionFactory(
				ConstantCostFunctionFactory(ConstantCostFunction(moveCost)),
				SinglePassageCostFunctionFactory
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
	def apply = SpaceClass("Flying Passage",
		MaxCostFunctionFactory(
			IsFlyingCostFunctionFactory,
			SinglePassageCostFunctionFactory
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Flying Passage",
			MaxCostFunctionFactory(
				IsFlyingCostFunctionFactory,
				SinglePassageCostFunctionFactory
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
	def apply = SpaceClass("Fire Passage",
		MaxCostFunctionFactory(
			IsElementCostFunctionFactory(Elements.Fire),
			SinglePassageCostFunctionFactory
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Fire Passage",
			MaxCostFunctionFactory(
				IsElementCostFunctionFactory(Elements.Fire),
				SinglePassageCostFunctionFactory
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
	def apply = SpaceClass("Slow Passage",
		MaxCostFunctionFactory(
			ConstantCostFunctionFactory(ConstantCostFunction(normalPassage * 2)),
			SinglePassageCostFunctionFactory
		),
		ConstantCostFunctionFactory(ConstantCostFunction(normalPassage))
	)
	def unapply(a:SpaceClass) = a match {
		case SpaceClass("Slow Passage",
			MaxCostFunctionFactory(
				ConstantCostFunctionFactory(ConstantCostFunction(moveCost)),
				SinglePassageCostFunctionFactory
			),
			ConstantCostFunctionFactory(ConstantCostFunction(atkCost))
		) => (moveCost == normalPassage * 2) && (atkCost == normalPassage)
		case _ => false
	}
}
