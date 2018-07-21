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
import scala.language.reflectiveCalls
import com.rayrobdod.boardGame.SpaceClassMatcher

class SpaceClassTest extends FunSpec {
	
	describe ("SpaceClasses") {
		describe ("UniPassageSpaceClass"){
			happySuite(enumValue = UniPassageSpaceClass, " ", 1, 1, 1)
		}
		describe ("SlowPassageSpaceClass"){
			happySuite(enumValue = SlowPassageSpaceClass, "s", 2, 1, 2)
		}
		describe ("ImpassibleSpaceClass"){
			happySuite(enumValue = ImpassibleSpaceClass, "|", 1000, 1000, 1000)
		}
		describe ("AttackOnlySpaceClass"){
			happySuite(enumValue = AttackOnlySpaceClass, ":", 1000, 1, 1000)
		}
		describe ("FlyingPassageSpaceClass"){
			happySuite(enumValue = FlyingPassageSpaceClass, ".", 1000, 1, 1)
		}
		describe ("FirePassageSpaceClass"){
			happySuite(enumValue = FirePassageSpaceClass, "f", 1, 1, 1)
		}
		
	}
	
	
	private def happySuite(
			enumValue:SpaceClassMatcher[SpaceClass]{def apply:SpaceClass},
			reference:String,
			groundedMoveCost:Int,
			attackCost:Int,
			flyingMoveCost:Int
	):Unit = {
		it ("reference is '" + reference + "' (matcher)") {
			assertResult(enumValue){SpaceClassMatcherFactory(reference)}
		}
		it ("reference is '" + reference + "' (builder)") {
			assertResult(enumValue.apply){SpaceClassFactory(reference)}
		}
		it ("scm matches class produced by scm") {
			assert(enumValue.unapply(enumValue.apply))
		}
		it ("scm doesn't matches class not produced by scm") {
			assert(! enumValue.unapply(SpaceClassFactory("asdfasddf")))
		}
		it ("Move cost for a lone humanoid token is " + groundedMoveCost) {
			val flamingSwordsmanToken = new Token(null, tokenClass = Some(new TokenClass(
				name = "Flaming Swordsman",
				body = BodyTypes.Humanoid,
				atkElement = Elements.Fire,
				atkWeapon = Weaponkinds.Bladekind,
				atkStatus = Statuses.Burn,
				range = 1,
				speed = 3,
				weakDirection = Directions.Down,
				weakWeapon = Map.empty.withDefaultValue(1f),
				weakStatus = Statuses.Burn,
				isSpy = false,
				stanceGroup = TokenClass.SingleStanceGroup
			)))
			
			assertResult(groundedMoveCost){
				enumValue.apply.canEnter(flamingSwordsmanToken, new ListOfTokens(Nil)).apply(null,null)
			}
		}
		it ("Attack cost for a lone humanoid token is " + attackCost) {
			val flamingSwordsmanToken = new Token(null, tokenClass = Some(new TokenClass(
				name = "Flaming Swordsman",
				body = BodyTypes.Humanoid,
				atkElement = Elements.Fire,
				atkWeapon = Weaponkinds.Bladekind,
				atkStatus = Statuses.Burn,
				range = 1,
				speed = 3,
				weakDirection = Directions.Down,
				weakWeapon = Map.empty.withDefaultValue(1f),
				weakStatus = Statuses.Burn,
				isSpy = false,
				stanceGroup = TokenClass.SingleStanceGroup
			)))
			
			assertResult(attackCost){
				enumValue.apply.canAttack(flamingSwordsmanToken, new ListOfTokens(Nil)).apply(null,null)
			}
		}
		it ("Move cost for a lone flying token is " + flyingMoveCost) {
			val flyingToken = new Token(null, tokenClass = Some(new TokenClass(
				name = "Flaming Swordsman",
				body = BodyTypes.Avian,
				atkElement = Elements.Fire,
				atkWeapon = Weaponkinds.Bladekind,
				atkStatus = Statuses.Burn,
				range = 1,
				speed = 3,
				weakDirection = Directions.Down,
				weakWeapon = Map.empty.withDefaultValue(1f),
				weakStatus = Statuses.Burn,
				isSpy = false,
				stanceGroup = TokenClass.SingleStanceGroup
			)))
			
			assertResult(flyingMoveCost){
				enumValue.apply.canEnter(flyingToken, new ListOfTokens(Nil)).apply(null,null)
			}
		}
	}
}

