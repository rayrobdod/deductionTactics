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

import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.Weaponkinds._
import org.scalatest.FunSpec

class WeaponkindsTest extends FunSpec {
	
	describe ("Weaponkinds") {
		describe ("Bladekind"){
			happySuite(enumValue = Bladekind, id = 0, name = "Blade")
		}
		describe ("Bluntkind"){
			happySuite(enumValue = Bluntkind, id = 1, name = "Blunt")
		}
		describe ("Spearkind"){
			happySuite(enumValue = Spearkind, id = 2, name = "Spear")
		}
		describe ("Whipkind"){
			happySuite(enumValue = Whipkind, id = 3, name = "Whip")
		}
		describe ("Powderkind"){
			happySuite(enumValue = Powderkind, id = 4, name = "Powder")
		}
		
		describe ("Illegal values") {
			it ("apply(-1) throws") {
				intercept[IndexOutOfBoundsException] {
					Weaponkinds.apply(-1)
				}
			}
			it ("apply(5) throws") {
				intercept[IndexOutOfBoundsException] {
					Weaponkinds.apply(5)
				}
			}
			it ("apply(987654321) throws") {
				intercept[IndexOutOfBoundsException] {
					Weaponkinds.apply(987654321)
				}
			}
			
			it ("withName(\"asfd\") throws") {
				intercept[NoSuchElementException] {
					Weaponkinds.withName("asfd")
				}
			}
			it ("withName(\"Spearkind\") throws") {
				intercept[NoSuchElementException] {
					Weaponkinds.withName("Spearkind")
				}
			}
		}
	}
	
	
	private def happySuite(id:Int, name:String, enumValue:Weaponkind) {
		it ("id is " + id){
			assertResult(id)(enumValue.id)
		}
		it ("name is \"" + name + "\""){
			assertResult(name + "kind")(enumValue.name)
		}
		it ("toString is \"com.rayrobdod.deductionTactics.Weaponkinds." + name + "\"") {
			assertResult("com.rayrobdod.deductionTactics.Weaponkinds." + name + "kind"){enumValue.toString}
		}
		it ("is the result of apply(" + id + ")"){
			assertResult(enumValue)(Weaponkinds.apply(id))
		}
		it ("is the result of withName(\"" + name + "\")"){
			assertResult(enumValue)(Weaponkinds.withName(name))
		}
		it ("is contained inside values"){
			assert(values.contains(enumValue))
		}
	}
}

