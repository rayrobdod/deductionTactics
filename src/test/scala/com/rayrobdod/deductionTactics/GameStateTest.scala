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

class GameStateTest extends FunSpec {
	describe ("GameState") {
		describe ("tokenMove") {
			it ("Should not mutate the initial state"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenMove(0,
						initialState.tokens.tokens(0)(0),
						initialState.board(1,1)
				)
				assertResult(initialState.board(0,0))(initialState.tokens.tokens(0)(0).currentSpace)
			}
			it ("Should not move other tokens"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenMove(0,
						initialState.tokens.tokens(0)(0),
						initialState.board(1,1)
				)
				assertResult(afterState.board(0,2))(afterState.tokens.tokens(0)(1).currentSpace)
				assertResult(afterState.board(2,0))(afterState.tokens.tokens(1)(0).currentSpace)
				assertResult(afterState.board(2,2))(afterState.tokens.tokens(1)(1).currentSpace)
			}
			it ("Should have a moved token in the result"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenMove(0,
						initialState.tokens.tokens(0)(0),
						initialState.board(1,1)
				)
				assertResult(afterState.board(1,1))(afterState.tokens.tokens(0)(0).currentSpace)
			}
			it ("Should change the number of spaces a token can move afterwards"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenMove(0,
						initialState.tokens.tokens(0)(0),
						initialState.board(1,1)
				)
				assertResult(1)(afterState.tokens.tokens(0)(0).canMoveThisTurn)
			}
			it ("Should reject a move that's too far away"){
				intercept[IllegalArgumentException] {
					val initialState = genSimpleGameState()
					val afterState = initialState.tokenMove(0,
							initialState.tokens.tokens(0)(0),
							initialState.board(2,2)
					)
				}
			}
			it ("Should reject a move by the wrong player"){
				intercept[IllegalArgumentException] {
					val initialState = genSimpleGameState()
					val afterState = initialState.tokenMove(1,
							initialState.tokens.tokens(0)(0),
							initialState.board(1,1)
					)
				}
			}
			
			
		}
		describe ("tokenAttackDamage") {
			it ("Should not mutate the initial state"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenAttackDamage(0,
						initialState.tokens.tokens(0)(0),
						initialState.tokens.tokens(1)(0)
				)
				assertResult(Token.maximumHitpoints)(initialState.tokens.tokens(1)(0).currentHitpoints)
			}
			it ("Should deal damage to the attackee"){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenAttackDamage(0,
						initialState.tokens.tokens(0)(0),
						initialState.tokens.tokens(1)(0)
				)
				assertResult(Token.maximumHitpoints - Token.baseDamage * 2)(afterState.tokens.tokens(1)(0).currentHitpoints)
			}
			it ("Should use up the Attacker's attack "){
				val initialState = genSimpleGameState()
				val afterState = initialState.tokenAttackDamage(0,
						initialState.tokens.tokens(0)(0),
						initialState.tokens.tokens(1)(0)
				)
				assertResult(false)(afterState.tokens.tokens(0)(0).canAttackThisTurn)
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
			currentSpace = field(x,y),
			canMoveThisTurn = 3,
			canAttackThisTurn = true,
			tokenClass = Some(new TokenClassBlunt("Sample",
					BodyTypes.Humanoid,
					Elements.Fire,
					Weaponkinds.Bladekind,
					Statuses.Burn,
					3, 3,
					Directions.Left,
					Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
					Statuses.Burn
			))
		)
	}
}

