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
import com.rayrobdod.boardGame.{RectangularField, Space}
import org.scalatest.FunSpec

class ListOfTokensTest extends FunSpec {
	class ZeroDSpace extends Space[SpaceClass] {
		override def adjacentSpaces = Seq.empty
		override def typeOfSpace = FreePassageSpaceClass.apply
	}
	val halfAlive = new ListOfTokens(Seq(
			Seq(new Token(new ZeroDSpace), new Token(new ZeroDSpace, 0)),
			Seq(new Token(new ZeroDSpace), new Token(new ZeroDSpace, 0))
	))
			
	
	describe ("ListOfTokens") {
		describe ("tokens") {
			it ("mirrors input") {
				assertResult(Seq(2,2))(halfAlive.tokens.map{_.size})
			}
		}
		describe ("aliveTokens") {
			it ("filters tokens with zero hitpoints") {
				assertResult(Seq(1,1))(halfAlive.aliveTokens.map{_.size})
			}
		}
		describe ("alivePlayerTokens") {
			it ("filters tokens with zero hitpoints") {
				assertResult(1)(halfAlive.alivePlayerTokens(0).size)
			}
			it ("returns the correct token (0)") {
				assertResult(halfAlive.tokens(0)(0))(halfAlive.alivePlayerTokens(0).apply(0))
			}
			it ("returns the correct token (1)") {
				assertResult(halfAlive.tokens(1)(0))(halfAlive.alivePlayerTokens(1).apply(0))
			}
		}
		describe ("aliveNotPlayerTokens") {
			it ("filters tokens with zero hitpoints") {
				assertResult(Seq(1))(halfAlive.aliveNotPlayerTokens(0).map{_.size})
			}
			it ("returns the correct token (0)") {
				assertResult(halfAlive.tokens(1)(0))(halfAlive.aliveNotPlayerTokens(0).apply(0)(0))
			}
			it ("returns the correct token (1)") {
				assertResult(halfAlive.tokens(0)(0))(halfAlive.aliveNotPlayerTokens(1).apply(0)(0))
			}
		}
		describe ("indexOf") {
			it ("a") {
				assertResult((1,1)){halfAlive.indexOf(halfAlive.tokens(1,1))}
			}
			it ("b") {
				assertResult((1,0)){halfAlive.indexOf(halfAlive.tokens(1,0))}
			}
		}
	}
	
}
