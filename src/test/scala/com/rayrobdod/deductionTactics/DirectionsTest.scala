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
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.StrictRectangularSpaceViaFutures
import com.rayrobdod.deductionTactics.Directions._
import org.scalatest.FunSpec

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
		
		describe("Direction.spaceIs") {
			val toNone = {() => None}
			val dest = new StrictRectangularSpaceViaFutures[SpaceClass](FreePassageSpaceClass.apply, toNone, toNone, toNone, toNone)
			val toDest = {() => Some(dest)}
			
			describe ("space to itself is false") {
				values.foreach{dir =>
					it (dir.name) {
						assert(! dir.spaceIs(dest, dest))
					}
				}
			}
			it ("Space.Left is left of Space") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toDest,
						rightFuture = toNone,
						upFuture = toNone,
						downFuture = toNone)
				
				assert(Left.spaceIs(src, dest))
			}
			it ("Space.Right is right of Space") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = toDest,
						upFuture = toNone,
						downFuture = toNone)
				
				assert(Right.spaceIs(src, dest))
			}
			it ("Space.down is down of Space") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = toNone,
						upFuture = toNone,
						downFuture = toDest)
				
				assert(Down.spaceIs(src, dest))
			}
			it ("Space.down is down of Space (implicit)") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = toNone,
						upFuture = toNone,
						downFuture = toDest)
				
				assert(dest is Down of src)
			}
			it ("Space.up is up of Space") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = toNone,
						upFuture = toDest,
						downFuture = toNone)
				
				assert(Up.spaceIs(src, dest))
			}
			it ("Space.up is not down of Space") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = toNone,
						upFuture = toDest,
						downFuture = toNone)
				
				assert(! Down.spaceIs(src, dest))
			}
			it ("Something random is not down of Space, when Space has a down") {
				val notThis = new StrictRectangularSpaceViaFutures[SpaceClass](FirePassageSpaceClass.apply, toNone, toNone, toNone, toNone)
				
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = toNone,
						upFuture = toNone,
						downFuture = toDest)
				
				assert(! Down.spaceIs(src, notThis))
			}
			it ("Recursion is possible (up)") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = toNone,
						upFuture = {() =>
							Some(new StrictRectangularSpaceViaFutures[SpaceClass](
								FreePassageSpaceClass.apply,
								leftFuture = toNone,
								rightFuture = toNone,
								upFuture = toDest,
								downFuture = toNone
							))
						},
						downFuture = toNone)
				
				assert(Up.spaceIs(src, dest))
			}
			it ("Recursion is possible (right)") {
				val src = new StrictRectangularSpaceViaFutures[SpaceClass](
						FreePassageSpaceClass.apply,
						leftFuture = toNone,
						rightFuture = {() =>
							Some(new StrictRectangularSpaceViaFutures[SpaceClass](
								FreePassageSpaceClass.apply,
								leftFuture = toNone,
								rightFuture = toDest,
								upFuture = toNone,
								downFuture = toNone
							))
						},
						upFuture = toNone,
						downFuture = toNone)
				
				assert(Right.spaceIs(src, dest))
			}
		}
		describe ("pathDirections") {
			val field = RectangularField(Seq(
				Seq(AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply),
				Seq(AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply),
				Seq(AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply, AttackOnlySpaceClass.apply)
			))
			val listOfTokens = new ListOfTokens(Seq(Seq(Token(field(0,0)))))
			
			it ("a to a.down is Seq(Down)") {
				val exp = Seq(Down)
				val res = Directions.pathDirections(field(0,0), field(0,0).down.get, listOfTokens.tokens(0,0), listOfTokens)
				assertResult(exp){res}
			}
			it ("a to a.up is Seq(Up)") {
				val exp = Seq(Up)
				val res = Directions.pathDirections(field(1,1), field(1,1).up.get, listOfTokens.tokens(0,0), listOfTokens)
				assertResult(exp){res}
			}
			it ("a to a.left is Seq(Left)") {
				val exp = Seq(Left)
				val res = Directions.pathDirections(field(1,1), field(1,1).left.get, listOfTokens.tokens(0,0), listOfTokens)
				assertResult(exp){res}
			}
			it ("a to a.right is Seq(Right)") {
				val exp = Seq(Right)
				val res = Directions.pathDirections(field(1,1), field(1,1).right.get, listOfTokens.tokens(0,0), listOfTokens)
				assertResult(exp){res}
			}
			it ("a to a.left.left is Seq(Left, Left)") {
				val exp = Seq(Left, Left)
				val res = Directions.pathDirections(field(2,2), field(2,2).left.get.left.get, listOfTokens.tokens(0,0), listOfTokens)
				assertResult(exp){res}
			}
			it ("a to a.left.left.down.down is Seq(Left, Left, Down, Down)") {
				val exp = Seq(Down, Left, Left, Down)
				val res = Directions.pathDirections(field(2,0), field(0,2), listOfTokens.tokens(0,0), listOfTokens)
				assertResult(exp){res}
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
		describe (".weaknessMultiplier") {
			val strngDir = Directions((id + 2) % 4)
			val othogDir1 = Directions((id + 1) % 4)
			val othogDir2 = Directions((id + 3) % 4)
				
			it ("is 2. when pathDirections is contains exactly one " + name) {
				assertResult(2.0)(enumValue.weaknessMultiplier(Seq(enumValue)))
			}
			it ("is 2. when pathDirections is contains exactly two " + name + "s") {
				assertResult(2.0)(enumValue.weaknessMultiplier(Seq(enumValue, enumValue)))
			}
			it ("is .5 when pathDirections is contains one of the element opposite of this") {
				assertResult(0.5)(enumValue.weaknessMultiplier(Seq(strngDir)))
			}
			it ("is .5 when pathDirections is contains two of the element opposite of this") {
				assertResult(0.5)(enumValue.weaknessMultiplier(Seq(strngDir, strngDir)))
			}
			it ("is 1. when pathDirections is contains only orthogDirs (1)") {
				assertResult(1.0)(enumValue.weaknessMultiplier(Seq(othogDir1)))
			}
			it ("is 1. when pathDirections is contains only orthogDirs (2)") {
				assertResult(1.0)(enumValue.weaknessMultiplier(Seq(othogDir2)))
			}
			it ("is 1.5 when pathDirections is contains an orthogDir and a weakDir") {
				assertResult(1.5)(enumValue.weaknessMultiplier(Seq(othogDir2, enumValue)))
			}
			it ("is .75 when pathDirections is contains an orthogDir and a strngDir") {
				assertResult(0.75)(enumValue.weaknessMultiplier(Seq(othogDir2, strngDir)))
			}
			it ("is 2. when pathDirections is contains a " + name + " and one of each othogDir") {
				assertResult(2.0)(enumValue.weaknessMultiplier(Seq(enumValue, othogDir1, othogDir2)))
			}
			it ("is 1. when pathDirections is contains an othogDir and one of each weak and str dir") {
				assertResult(1.0)(enumValue.weaknessMultiplier(Seq(enumValue, othogDir1, strngDir)))
			}
			it ("has a domain between 0.5 and 2.0") {
				val LENGTH_TO_CHECK = 3
				val RANGE_TO_CHECK = (-LENGTH_TO_CHECK to LENGTH_TO_CHECK)
				RANGE_TO_CHECK.flatMap{x => RANGE_TO_CHECK.map{y =>
					Seq.fill(x){strngDir} ++ Seq.fill(-x){enumValue} ++ Seq.fill(y){othogDir1} ++ Seq.fill(-y){othogDir2}
				}}.map{enumValue.weaknessMultiplier _}.foreach{x =>
					assert(0.5 <= x && x <= 2.0)
				}
			}
		}
	}
}

