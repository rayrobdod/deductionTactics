/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

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

class GameStateTest extends FunSpec {
	describe ("GameState") {
		describe ("tokenMove") {
			it ("Should not mutate the initial state"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenMove(0,
						initialState.tokens.tokens(0)(0),
						initialState.board.space(1,1)
				)
				assertResult(initialState.tokens.tokens(0)(0).currentSpace)(initialState.board.space(0,0))
				assertResult(initialState.tokens.tokens(0)(0).currentSpace)(initialState.board.space(1,1))
			}
			it ("Should have a moved token in the result"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenMove(0,
						initialState.tokens.tokens(0)(0),
						initialState.board.space(1,1)
				)
				assertResult(afterState.tokens.tokens(0)(0).currentSpace)(afterState.board.space(0,0))
				assertResult(afterState.tokens.tokens(0)(0).currentSpace)(afterState.board.space(1,1))
			}
			
			
		}
	}
	
	
	def genSimpleGameState() = {
		val field = RectangularField( Seq(
				Seq( FreePassageSpaceClass.apply, FreePassageSpaceClass.apply, FreePassageSpaceClass.apply),
				Seq( FreePassageSpaceClass.apply, FreePassageSpaceClass.apply, FreePassageSpaceClass.apply),
				Seq( FreePassageSpaceClass.apply, FreePassageSpaceClass.apply, FreePassageSpaceClass.apply)
		))
		val tokens = new ListOfTokens( Seq(
			Seq( genActionableToken(field,0,0), genActionableToken(field,0,2) ),
			Seq( genActionableToken(field,2,0), genActionableToken(field,2,2) )
		))
		
		GameState(field, tokens)
	}
	def genActionableToken(field:RectangularField[SpaceClass], x:Int, y:Int) = {
		new Token(
			currentSpace = field.space(x,y),
			canMoveThisTurn = 3,
			canAttackThisTurn = true
		)
	}
}

