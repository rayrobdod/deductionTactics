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
package ai

import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{RectangularField, Space}
import org.scalatest.FunSpec

class MoveRangeOfTest extends FunSpec {
	val board = RectangularField( Seq.fill(4,4)(UniPassageSpaceClass.apply) )
	val myTokenClass = Some(new TokenClass("Sample",
			BodyTypes.Humanoid,
			Elements.Fire,
			Weaponkinds.Bladekind,
			Statuses.Burn,
			1, 2,
			Directions.Left,
			Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
			Statuses.Burn
	))
	
	describe ("moveRangeOf") {
		it ("A healthy current-turn Token has a full range") {
			val token = new Token(board(1,1), tokenClass = myTokenClass).startOfTurn()
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(11)(moveRangeOf(token, list).size)
		}
		it ("A healthy Token has a full speed") {
			val token = new Token(board(1,1), tokenClass = myTokenClass)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(11)(moveRangeOf(token, list).size)
		}
		it ("A SnakeToxin'd Token has a speed of one") {
			val token = new Token(board(1,1), tokenClass = myTokenClass, currentStatus = Statuses.Snake)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(5)(moveRangeOf(token, list).size)
		}
		it ("A Sleep'd Token has a speed of zero") {
			val token = new Token(board(1,1), tokenClass = myTokenClass, currentStatus = Statuses.Sleep)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(1)(moveRangeOf(token, list).size)
		}
		it ("A Burn'd Token has a full speed") {
			val token = new Token(board(1,1), tokenClass = myTokenClass, currentStatus = Statuses.Burn)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(11)(moveRangeOf(token, list).size)
		}
		it ("An unknown token guesses no speed") {
			val token = new Token(board(1,1)).startOfTurn()
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(1)(moveRangeOf(token, list).size)
		}
		it ("An unknown token guesses the suspicion's speed") {
			val token = new Token(board(1,1)).startOfTurn()
			val susp = new TokenClassSuspicion(speed = Some(2))
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(11)(moveRangeOf(token, list, susp).size)
		}
	}
	
	describe ("attackRangeOf") {
		it ("A healthy current-turn Token has a full range") {
			val token = new Token(board(1,1), tokenClass = myTokenClass).startOfTurn()
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(15)(attackRangeOf(token, list).size)
		}
		it ("A healthy Token has a full speed") {
			val token = new Token(board(1,1), tokenClass = myTokenClass)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(15)(attackRangeOf(token, list).size)
		}
		it ("A SnakeToxin'd Token has a speed of one") {
			val token = new Token(board(1,1), tokenClass = myTokenClass, currentStatus = Statuses.Snake)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(11)(attackRangeOf(token, list).size)
		}
		it ("A Sleep'd Token has a speed of zero") {
			val token = new Token(board(1,1), tokenClass = myTokenClass, currentStatus = Statuses.Sleep)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(5)(attackRangeOf(token, list).size)
		}
		it ("A Burn'd Token has a full speed") {
			val token = new Token(board(1,1), tokenClass = myTokenClass, currentStatus = Statuses.Burn)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(15)(attackRangeOf(token, list).size)
		}
		it ("A Blind'd Token has a full speed") {
			val token = new Token(board(1,1), tokenClass = myTokenClass, currentStatus = Statuses.Blind)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(0)(attackRangeOf(token, list).size)
		}
		it ("An unknown token guesses no range") {
			val token = new Token(board(1,1)).startOfTurn()
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(1)(attackRangeOf(token, list).size)
		}
		it ("An unknown token guesses the suspicion's range") {
			val token = new Token(board(1,1)).startOfTurn()
			val susp = new TokenClassSuspicion(range = Some(2))
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(11)(attackRangeOf(token, list, susp).size)
		}
	}
	
	
	
	
	
	describe ("MoveToCostFunction") {
		it ("UniPassageSpaceClass occupied") {
			val token = new Token(board(1,1), tokenClass = myTokenClass)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(SpaceClass.impossiblePassage)(
					new MoveToCostFunction(token, list)(board(1,1), board(1,1))
			)
		}
		it ("UniPassageSpaceClass unoccupied") {
			val token = new Token(board(1,1), tokenClass = myTokenClass)
			val list = new ListOfTokens(Seq(Seq(token)))
			
			assertResult(SpaceClass.normalPassage)(
					new MoveToCostFunction(token, list)(board(2,2), board(2,2))
			)
		}
	}
	
	
}
