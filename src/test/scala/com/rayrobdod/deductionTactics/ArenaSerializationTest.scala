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
import com.rayrobdod.deductionTactics.Statuses._
import java.nio.charset.StandardCharsets.UTF_8

class ArenaSerializationTest extends FunSpec {
	import TokenClassFromBinary2.HexArrayStringConverter;
	
	describe ("ArenaService") {
		it ("First is named \"Empty Field\"") {
			assertResult("Empty Field"){
				Arena.fromService(0).name
			}
		}
		it ("First has an arena that is empty") {
			Arena.fromService(0).field.foreach{x =>
				val (index, space) = x
				val spaceClass = space.typeOfSpace
				assertResult(UniPassageSpaceClass.apply){spaceClass}
			}
		}
		it ("First has an possible players count of 2 or 4") {
			assertResult(Set(2,4)){Arena.fromService(0).possiblePlayers}
		}
		it ("First has starting spaces for 2 of Seq(...)") {
			val exp = Seq(Seq(
				Tuple2(1,5),
				Tuple2(1,3),
				Tuple2(1,7),
				Tuple2(1,1),
				Tuple2(1,9),
				Tuple2(1,4),
				Tuple2(1,6),
				Tuple2(1,2),
				Tuple2(1,8),
				Tuple2(1,0)
			),Seq(
				Tuple2(8,5),
				Tuple2(8,3),
				Tuple2(8,7),
				Tuple2(8,1),
				Tuple2(8,9),
				Tuple2(8,4),
				Tuple2(8,6),
				Tuple2(8,2),
				Tuple2(8,8),
				Tuple2(8,0)
			))
			
			assertResult(exp){Arena.fromService(0).startSpaces(2)}
		}
		
		
		
		it ("Third is named \"Tournament Bracket\"") {
			assertResult("Tournament Bracket"){
				Arena.fromService(2).name
			}
		}
		it ("Third has starting spaces for 2 of Seq(...)") {
			val exp = Seq(Seq(
				Tuple2(2 ,13),
				Tuple2(10,13),
				Tuple2(16,13),
				Tuple2(24,13)
			),Seq(
				Tuple2(26,13),
				Tuple2(18,13),
				Tuple2(12,13),
				Tuple2(4 ,13)
			))
			
			assertResult(exp){Arena.fromService(2).startSpaces(2)}
		}
	}
}
