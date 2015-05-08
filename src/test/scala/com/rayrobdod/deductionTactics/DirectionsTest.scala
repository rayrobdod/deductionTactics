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
import com.rayrobdod.deductionTactics.Directions._
import org.scalatest.{FunSuite, FunSpec}
import org.scalatest.prop.PropertyChecks

class DirectionsTest extends FunSpec {
	
	describe ("Directions") {
		describe ("Left"){
			happySuite(enumValue = Left, id = 0, name = "Left")
		}
		describe ("Up"){
			happySuite(enumValue = Up, id = 1, name = "Up")
		}
		describe ("Right"){
			happySuite(enumValue = Right, id = 2, name = "Right")
		}
		describe ("Down"){
			happySuite(enumValue = Down, id = 3, name = "Down")
		}
		
		describe ("Illegal values") {
			it ("apply(-1) throws") {
				intercept[IndexOutOfBoundsException] {
					Directions.apply(-1)
				}
			}
			it ("apply(5) throws") {
				intercept[IndexOutOfBoundsException] {
					Directions.apply(4)
				}
			}
			it ("apply(987654321) throws") {
				intercept[IndexOutOfBoundsException] {
					Directions.apply(987654321)
				}
			}
			
			it ("withName(\"asfd\") throws") {
				intercept[NoSuchElementException] {
					Directions.withName("asfd")
				}
			}
			it ("withName(\"Spearkind\") throws") {
				intercept[NoSuchElementException] {
					Directions.withName("Spearkind")
				}
			}
		}
	}
	
	
	private def happySuite(id:Int, name:String, enumValue:Direction) {
		it ("id is " + id){
			assertResult(id)(enumValue.id)
		}
		it ("name is \"" + name + "\""){
			assertResult(name)(enumValue.name)
		}
		it ("is the result of apply(" + id + ")"){
			assertResult(enumValue)(Directions.apply(id))
		}
		it ("is the result of withName(\"" + name + "\")"){
			assertResult(enumValue)(Directions.withName(name))
		}
		it ("is contained inside values"){
			assert(values.contains(enumValue))
		}
	}
}

