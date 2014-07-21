/*
	Deduction Tactics
	Copyright (C) 2014  Raymond Dodge

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

import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{RectangularField, Space}
import org.scalatest.{FunSuite, FunSpec}
import org.scalatest.prop.PropertyChecks

class ElementsTest extends FunSpec {
	describe ("Element") {
		// damageModifier is a vague term. Choose either "damageMultiplierWhenAttacking" or "damageMulitplierWhenDefending"... 
		describe ("Elements.Fire.damageModifier") {
			it ("should be x1 against Fire"){
				// Fire is neutral against itself
				val res = Elements.Fire.damageModifier(Elements.Fire)
				assertResult(1)(res)
			}
			ignore ("should be x2 against Electric"){
				// Fire is very strong against electric
				val res = Elements.Fire.damageModifier(Elements.Electric)
				assertResult(2)(res)
			}
			ignore ("should be x1.5 against Light"){
				// Fire is somewhat strong against light
				val res = Elements.Fire.damageModifier(Elements.Light)
				assertResult(1.5)(res)
			}
			ignore ("should be x0.75 against Sound"){
				// Fire is somewhat weak against sound
				val res = Elements.Fire.damageModifier(Elements.Sound)
				assertResult(0.75)(res)
			}
			ignore ("should be x0.5 against Frost"){
				// Fire is very weak against frost
				val res = Elements.Fire.damageModifier(Elements.Frost)
				assertResult(0.5)(res)
			}
		}
	}
}


