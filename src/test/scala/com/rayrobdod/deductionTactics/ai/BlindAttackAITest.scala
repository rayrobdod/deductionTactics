/*
	Deduction Tactics
	Copyright (C) 2014  Raymond Dodge

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
import org.scalatest.{FunSuite, FunSpec}
import org.scalatest.prop.PropertyChecks

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
				1, 2,
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
					def suspisions:Map[(Int, Int), TokenClassSuspision] = throw new UnsupportedOperationException
					def addAttack(r:GameState.Result):Memo = throw new UnsupportedOperationException
					def updateSuspision(key:(Int, Int), value:TokenClassSuspision):Memo = throw new UnsupportedOperationException
				}
				
				assertResult(exp){(new BlindAttackAI).notifyTurn(0, null, null, null, exp)}
			}
		}
		describe("takeTurn") {
			// TODO: ???
		}
	}
}
