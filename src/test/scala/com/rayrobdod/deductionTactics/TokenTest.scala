/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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

import org.scalatest.FunSpec
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}

class TokenTest extends FunSpec {
	describe ("Token") {
		describe ("endOfTurn") {
			val src = new Token(
				currentSpace = isolatedSpace,
				canMoveThisTurn = 3,
				canAttackThisTurn = true
			)
			val res = src.endOfTurn
			
			it ("reduces 'canAttackThisTurn' to 0"){
				assertResult(0)(res.canMoveThisTurn)
			}
			it ("reduces 'canMoveThisTurn' to false"){
				assertResult(false)(res.canAttackThisTurn)
			}
			it ("does not change other attributes"){
				assertResult(src.currentSpace){res.currentSpace}
			}
		}
		describe ("startOfTurn") {
			describe ("(3 move; normal status)") {
				val src = new Token(
					currentSpace = isolatedSpace,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val res = src.startOfTurn
				
				it ("should keep current space the same") {
					assertResult(src.currentSpace){res.currentSpace}
				}
				it ("should increase canMove to match token class speed") {
					assertResult(3){res.canMoveThisTurn}
				}
				it ("should increase canAttack to true") {
					assertResult(true){res.canAttackThisTurn}
				}
				it ("Does not decrement the already-zero status duration") {
					assertResult(0){res.currentStatusTurnsLeft}
				}
				it ("Status remains Normal") {
					assertResult(Statuses.Normal){res.currentStatus}
				}
			}
			describe ("(5 move; normal status)") {
				val src = new Token(
					currentSpace = isolatedSpace,
					tokenClass = Some(sample5SpeedTokenClass)
				)
				val res = src.startOfTurn
				
				it ("should keep current space the same") {
					assertResult(src.currentSpace){res.currentSpace}
				}
				it ("should increase canMove to match token class speed") {
					assertResult(5){res.canMoveThisTurn}
				}
				it ("should increase canAttack to true") {
					assertResult(true){res.canAttackThisTurn}
				}
				it ("Does not decrement the already-zero status duration") {
					assertResult(0){res.currentStatusTurnsLeft}
				}
				it ("Status remains Normal") {
					assertResult(Statuses.Normal){res.currentStatus}
				}
			}
			describe ("(Blind 1 turn)") {
				val src = new Token(
					currentSpace = isolatedSpace,
					tokenClass = Some(sample3SpeedTokenClass),
					currentStatus = Statuses.Blind,
					currentStatusTurnsLeft = 1
				)
				val res = src.startOfTurn
				
				it ("should keep current space the same") {
					assertResult(src.currentSpace){res.currentSpace}
				}
				it ("should increase canMove to match token class speed") {
					assertResult(3){res.canMoveThisTurn}
				}
				it ("should increase canAttack to true") {
					assertResult(true){res.canAttackThisTurn}
				}
				it ("Decrements status duration to 0") {
					assertResult(0){res.currentStatusTurnsLeft}
				}
				it ("Status becomes Normal") {
					assertResult(Statuses.Normal){res.currentStatus}
				}
			}
			describe ("(Blind 3 turn)") {
				val src = new Token(
					currentSpace = isolatedSpace,
					tokenClass = Some(sample3SpeedTokenClass),
					currentStatus = Statuses.Blind,
					currentStatusTurnsLeft = 3
				)
				val res = src.startOfTurn
				
				it ("should keep current space the same") {
					assertResult(src.currentSpace){res.currentSpace}
				}
				it ("should increase canMove to match token class speed") {
					assertResult(3){res.canMoveThisTurn}
				}
				it ("should increase canAttack to true") {
					assertResult(true){res.canAttackThisTurn}
				}
				it ("Decrements status duration to 2") {
					assertResult(2){res.currentStatusTurnsLeft}
				}
				it ("Status remains Blind") {
					assertResult(Statuses.Blind){res.currentStatus}
				}
			}
		}
		describe ("takeDamage") {
			val field = RectangularField(Seq(
				Seq(AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply),
				Seq(AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply),
				Seq(AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply)
			))
			
			
			describe ("in neutral conditions") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.north.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 8 damage") {
					assertResult(defender.currentHitpoints - 8){res.currentHitpoints}
				}
			}
			describe ("with the attacker having a directional advantage") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.west.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 16 damage") {
					assertResult(defender.currentHitpoints - 16){res.currentHitpoints}
				}
			}
			describe ("with the attacker having a directional disadvantage") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.east.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 4 damage") {
					assertResult(defender.currentHitpoints - 4){res.currentHitpoints}
				}
			}
			describe ("with the attacker having a weaponkind advantage") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.north.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(weaponWeakTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 16 damage") {
					assertResult(defender.currentHitpoints - 16){res.currentHitpoints}
				}
			}
			describe ("with the attacker having a weaponkind disadvantage") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.north.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(weaponResistTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 4 damage") {
					assertResult(defender.currentHitpoints - 4){res.currentHitpoints}
				}
			}
			describe ("with status weakness") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.north.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					currentStatus = sample3SpeedTokenClass.weakStatus,
					currentStatusTurnsLeft = 100,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 16 damage") {
					assertResult(defender.currentHitpoints - 16){res.currentHitpoints}
				}
			}
			describe ("with element weakness") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.north.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(elementWeakTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 16 damage") {
					assertResult(defender.currentHitpoints - 16){res.currentHitpoints}
				}
			}
			describe ("with element resistance") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.north.get,
					tokenClass = Some(elementWeakTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 4 damage") {
					assertResult(defender.currentHitpoints - 4){res.currentHitpoints}
				}
			}
			describe ("with all weaknesses") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.west.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					currentStatus = sample3SpeedTokenClass.weakStatus,
					currentStatusTurnsLeft = 100,
					tokenClass = Some(multipleWeakTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 128 damage") {
					assertResult(defender.currentHitpoints - 128){res.currentHitpoints}
				}
			}
			describe ("with all resistances") {
				val attacker = new Token(
					currentSpace = field.space((1,1)).get.east.get,
					tokenClass = Some(sample3SpeedTokenClass)
				)
				val defender = new Token(
					currentSpace = field.space((1,1)).get,
					tokenClass = Some(multipleResistTokenClass)
				)
				val list = new ListOfTokens(Seq(Seq(attacker), Seq(defender)))
				val res = defender.takeDamage(attacker, list)
				
				it ("deals 1 damage") {
					assertResult(defender.currentHitpoints - 1){res.currentHitpoints}
				}
			}
		}
	}
	
	
	def isolatedSpace = new RectangularSpace[SpaceClass]() {
		override def east:Option[RectangularSpace[SpaceClass]] = None
		override def north:Option[RectangularSpace[SpaceClass]] = None
		override def south:Option[RectangularSpace[SpaceClass]] = None
		override def west:Option[RectangularSpace[SpaceClass]] = None
		override val typeOfSpace = AttackOnlySpaceClass.apply
	}
	def sample3SpeedTokenClass = new TokenClass("Sample",
		BodyTypes.Humanoid,
		Elements.Fire,
		Weaponkinds.Bladekind,
		Statuses.Burn,
		false,
		1, 3,
		Directions.Left,
		Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
		Statuses.Burn
	)
	def sample5SpeedTokenClass = new TokenClass("Sample",
		BodyTypes.Humanoid,
		Elements.Fire,
		Weaponkinds.Bladekind,
		Statuses.Burn,
		false,
		1, 5,
		Directions.Left,
		Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
		Statuses.Burn
	)
	def weaponWeakTokenClass = new TokenClass("Sample",
		BodyTypes.Humanoid,
		Elements.Fire,
		Weaponkinds.Bladekind,
		Statuses.Burn,
		false,
		1, 5,
		Directions.Left,
		Weaponkinds.values.map{(a) => ((a, 2f))}.toMap,
		Statuses.Burn
	)
	def weaponResistTokenClass = new TokenClass("Sample",
		BodyTypes.Humanoid,
		Elements.Fire,
		Weaponkinds.Bladekind,
		Statuses.Burn,
		false,
		1, 5,
		Directions.Left,
		Weaponkinds.values.map{(a) => ((a, .5f))}.toMap,
		Statuses.Burn
	)
	def elementWeakTokenClass = new TokenClass("Sample",
		BodyTypes.Humanoid,
		Elements.Electric,
		Weaponkinds.Bladekind,
		Statuses.Burn,
		false,
		1, 3,
		Directions.Left,
		Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
		Statuses.Burn
	)
	def multipleWeakTokenClass = new TokenClass("Sample",
		BodyTypes.Humanoid,
		Elements.Electric,
		Weaponkinds.Bladekind,
		Statuses.Burn,
		false,
		1, 3,
		Directions.Left,
		Weaponkinds.values.map{(a) => ((a, 2f))}.toMap,
		Statuses.Burn
	)
	def multipleResistTokenClass = new TokenClass("Sample",
		BodyTypes.Humanoid,
		Elements.Frost,
		Weaponkinds.Bladekind,
		Statuses.Burn,
		false,
		1, 3,
		Directions.Left,
		Weaponkinds.values.map{(a) => ((a, .5f))}.toMap,
		Statuses.Burn
	)
}

