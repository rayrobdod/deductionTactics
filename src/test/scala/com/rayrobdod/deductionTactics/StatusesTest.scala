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
import com.rayrobdod.deductionTactics.Statuses._
import org.scalatest.{FunSuite, FunSpec}
import org.scalatest.prop.PropertyChecks

class StatusesTest extends FunSpec {
	
	describe ("Statuses") {
		describe ("Sleep"){
			happySuite(enumValue = Sleep, id = 0, name = "Sleep")
		}
		describe ("Burn"){
			happySuite(enumValue = Burn, id = 1, name = "Burn")
		}
		describe ("Blind"){
			happySuite(enumValue = Blind, id = 2, name = "Blind")
		}
		describe ("Confuse"){
			happySuite(enumValue = Confuse, id = 3, name = "Confuse")
		}
		describe ("Neuro"){
			happySuite(enumValue = Neuro, id = 4, name = "Neuro")
		}
		describe ("Snake"){
			happySuite(enumValue = Snake, id = 5, name = "Snake")
		}
		describe ("Heal"){
			happySuite(enumValue = Heal, id = 6, name = "Heal")
		}
		describe ("Normal"){
			happySuite(enumValue = Normal, id = 7, name = "Normal")
		}
		
		describe ("Illegal values") {
			it ("apply(-1) throws") {
				intercept[IndexOutOfBoundsException] {
					Statuses.apply(-1)
				}
			}
			it ("apply(5) throws") {
				intercept[IndexOutOfBoundsException] {
					Statuses.apply(8)
				}
			}
			it ("apply(987654321) throws") {
				intercept[IndexOutOfBoundsException] {
					Statuses.apply(987654321)
				}
			}
			
			it ("withName(\"asfd\") throws") {
				intercept[NoSuchElementException] {
					Statuses.withName("asfd")
				}
			}
			it ("withName(\"Spearkind\") throws") {
				intercept[NoSuchElementException] {
					Statuses.withName("Spearkind")
				}
			}
		}
	}
	
	
	private def happySuite(id:Int, name:String, enumValue:Status) {
		it ("id is " + id){
			assertResult(id)(enumValue.id)
		}
		it ("name is \"" + name + "\""){
			assertResult(name)(enumValue.name)
		}
		it ("is the result of apply(" + id + ")"){
			assertResult(enumValue)(Statuses.apply(id))
		}
		it ("is the result of withName(\"" + name + "\")"){
			assertResult(enumValue)(Statuses.withName(name))
		}
		it ("is contained inside values"){
			assert(values.contains(enumValue))
		}
	}
}

