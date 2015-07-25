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

/**
 * @version a.6.0
 */
class BlindAttackAITest extends FunSpec {
	
	val tokenClasses = Elements.values.flatMap{elem =>
		Weaponkinds.values.map{weap =>
			new TokenClassBlunt("Sample",
				BodyTypes.Humanoid,
				elem,
				weap,
				Statuses.Burn,
				1, 3,
				Directions.Left,
				Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
				Statuses.Burn
			)
		}
	}
	
	
	describe ("BlindAttackAI") {
		describe ("selectTokenClasses") {
			it ("returns a sequence as long as the size parameter (-1)") {
				assertResult(0)((new BlindAttackAI).selectTokenClasses(-1).size)
			}
			it ("returns a sequence as long as the size parameter (0)") {
				assertResult(0)((new BlindAttackAI).selectTokenClasses(0).size)
			}
			it ("returns a sequence as long as the size parameter (5)") {
				assertResult(5){(new BlindAttackAI).selectTokenClasses(5).size}
			}
		}
		describe ("narrowTokenClasses") {
			it ("returns a value with length of the resultSize input (-1)") {
				assertResult(0){(new BlindAttackAI).narrowTokenClasses(Seq(tokenClasses), 0, 0).length}
			}
			it ("returns a value with length of the resultSize input (0)") {
				assertResult(0){(new BlindAttackAI).narrowTokenClasses(Seq(tokenClasses), 0, 0).length}
			}
			it ("returns a value with length of the resultSize input (5)") {
				assertResult(5){(new BlindAttackAI).narrowTokenClasses(Seq(tokenClasses), 5, 0).length}
			}
			it ("returns a value which is a subset of the selectedClasses input (Normal)") {
				assert{(new BlindAttackAI).narrowTokenClasses(Seq(tokenClasses), 5, 0).forall{tokenClasses.contains(_)}}
			}
			it ("returns a value which is a subset of the selectedClasses input (Nil)") {
				assert{(new BlindAttackAI).narrowTokenClasses(Seq(Nil), 5, 0).forall{tokenClasses.contains(_)}}
			}
			it ("is aware of the myPlayerIndex input (0)") {
				assert{(new BlindAttackAI).narrowTokenClasses(Seq(tokenClasses, Nil), 5, 0).forall{tokenClasses.contains(_)}}
			}
			it ("is aware of the myPlayerIndex input (1)") {
				assert{(new BlindAttackAI).narrowTokenClasses(Seq(Nil, tokenClasses), 5, 1).forall{tokenClasses.contains(_)}}
			}
		}
		describe("initialize") {
			it ("Returns a new SimpleMemo") {
				assertResult(new SimpleMemo){(new BlindAttackAI).initialize(0, null)}
			}
		}
		describe("notifyTurn") {
			it ("Returns the 'memo' parameter") {
				val exp = new Memo() {
					def attacks:Seq[GameState.Result] = throw new UnsupportedOperationException
					def suspicions:Map[(Int, Int), TokenClassSuspicion] = throw new UnsupportedOperationException
					def addAttack(r:GameState.Result):Memo = throw new UnsupportedOperationException
					def updateSuspicion(key:(Int, Int), value:TokenClassSuspicion):Memo = throw new UnsupportedOperationException
				}
				
				assertResult(exp){(new BlindAttackAI).notifyTurn(0, null, null, null, exp)}
			}
		}
		describe("takeTurn") {
			val field = RectangularField( Seq.fill(4,1){UniPassageSpaceClass.apply} )
			val myT = (new GameStateTest).genActionableToken(field, 0, 0)

			
			it ("when no attacks are possible, moves") {
				val gameState = GameState(field,
					new ListOfTokens(Seq(Seq(myT), Seq(Token(field(0,3)))))
				)
				val exp = Seq(GameState.TokenMove(myT, field(0,1)))
				val res = (new BlindAttackAI).takeTurn(0, gameState, null)
				
				assertResult(exp){res}
			}
			it ("when attacks is possible, attacks") {
				val otT = Token(field(0,1))
				val gameState = GameState(field,
					new ListOfTokens(Seq(Seq(myT), Seq(otT)))
				)
				val exp = Seq(GameState.TokenAttackDamage(myT, otT))
				val res = (new BlindAttackAI).takeTurn(0, gameState, null)
				
				assertResult(exp){res}
			}
			it ("when nothing else is possible, EndsTurn") {
				val gameState = GameState(field,
					new ListOfTokens(Seq(Seq(Token(field(0,0))), Seq(Token(field(0,3)))))
				)
				val exp = Seq(GameState.EndOfTurn)
				val res = (new BlindAttackAI).takeTurn(0, gameState, null)
				
				assertResult(exp){res}
			}
		}
	}
}
