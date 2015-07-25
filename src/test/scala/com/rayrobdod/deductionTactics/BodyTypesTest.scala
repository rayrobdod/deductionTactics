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
import com.rayrobdod.boardGame.StrictRectangularSpaceViaFutures
import com.rayrobdod.deductionTactics.BodyTypes._
import org.scalatest.FunSpec

class BodyTypesTest extends FunSpec {
	
	describe ("BodyTypes") {
		describe ("Humanoid"){
			happySuite(enumValue = Humanoid, id = 0, name = "Human")
		}
		describe ("Avian"){
			happySuite(enumValue = Avian, id = 1, name = "Avian")
		}
		describe ("Gerbil"){
			happySuite(enumValue = Gerbil, id = 2, name = "Gerbillinae")
		}
		
		describe ("Illegal values") {
			it ("apply(-1) throws") {
				intercept[IndexOutOfBoundsException] {
					BodyTypes.apply(-1)
				}
			}
			it ("apply(3) throws") {
				intercept[IndexOutOfBoundsException] {
					BodyTypes.apply(3)
				}
			}
			it ("apply(987654321) throws") {
				intercept[IndexOutOfBoundsException] {
					BodyTypes.apply(987654321)
				}
			}
			
			it ("withName(\"asfd\") throws") {
				intercept[NoSuchElementException] {
					BodyTypes.withName("asfd")
				}
			}
			it ("withName(\"Humanoid\") throws") {
				intercept[NoSuchElementException] {
					BodyTypes.withName("Humanoid")
				}
			}
		}
	}
	
	
	private def happySuite(id:Int, name:String, enumValue:BodyType) {
		it ("id is " + id){
			assertResult(id)(enumValue.id)
		}
		it ("name is \"" + name + "\""){
			assertResult(name)(enumValue.name)
		}
		it ("is the result of apply(" + id + ")"){
			assertResult(enumValue)(BodyTypes.apply(id))
		}
		it ("is the result of withName(\"" + name + "\")"){
			assertResult(enumValue)(BodyTypes.withName(name))
		}
		it ("is contained inside values"){
			assert(values.contains(enumValue))
		}
	}
}

