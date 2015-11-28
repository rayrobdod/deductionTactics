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
package serialization

import org.scalatest.FunSpec
import scala.collection.immutable.Seq

class GenerateBasicTokensTest extends FunSpec {
	describe ("GenerateBasicTokens.classes") {
		it ("contains 25 classes") {
			assertResult(25){GenerateBasicTokens.classes.length}
		}
		it ("contains 5 Fire-elemental classes") {
			assertResult(5){GenerateBasicTokens.classes.count{_.atkElement == Elements.Fire}}
		}
		it ("contains 5 Bladekind classes") {
			assertResult(5){GenerateBasicTokens.classes.count{_.atkWeapon == Weaponkinds.Bladekind}}
		}
		it ("all cleasses have the same (Humanoid, range=1, speed=3)") {
			assert{GenerateBasicTokens.classes.forall{_.range == 1}}
			assert{GenerateBasicTokens.classes.forall{_.speed == 3}}
			assert{GenerateBasicTokens.classes.forall{_.body == BodyTypes.Humanoid}}
		}
		it ("is sorted; the first element is Light/Blade") {
			val dut = GenerateBasicTokens.classes.head
			assertResult(Elements.Light){dut.atkElement}
			assertResult(Weaponkinds.Bladekind){dut.atkWeapon}
		}
	}
	describe ("GenerateBasicTokens.nameToIcon") {
		ignore ("???") {
		}
	}
}
