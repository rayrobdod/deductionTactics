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
 * Oooooh, real mocking this time.
 * 
 * @version a.6.0
 */
class WithAutorecordTest extends FunSpec {
	class StubPlayerAI extends PlayerAI {
		override def selectTokenClasses(a:Int):Seq[TokenClass] = Nil
		override def narrowTokenClasses(b:Seq[Seq[TokenClass]],a:Int,c:Int):Seq[TokenClass] = Nil
		override def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = Nil
		override def initialize(player:Int, initialState:GameState):Memo = new SimpleMemo
		override def notifyTurn(player:Int,action:GameState.Result,beforeState:GameState,afterState:GameState,memo:Memo):Memo = memo
	}
	val myTokenClass = new TokenClassBlunt("Sample",
			BodyTypes.Humanoid,
			Elements.Fire,
			Weaponkinds.Bladekind,
			Statuses.Burn,
			1, 2,
			Directions.Left,
			Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
			Statuses.Burn
	)
	
	
	
	
	
	
	describe ("WithAutorecord") {
		describe ("The Constructor") {
			it ("exists") {
				new WithAutorecord(new StubPlayerAI)
			}
		}
		
		describe ("buildTeam") {
			it ("forwards its command to the base ai") {
				// setup
				val exCount = 5
				val expected = Seq(myTokenClass)
				object Base extends StubPlayerAI{
					override def selectTokenClasses(a:Int) = {
						buildTeamCount = buildTeamCount + 1
						assertResult(exCount)(a)
						expected
					}
					override def narrowTokenClasses(b:Seq[Seq[TokenClass]],a:Int,c:Int):Seq[TokenClass] = Nil
					
					private var buildTeamCount:Int = 0
					def verify() = {1 == buildTeamCount}
				}
				val ai = new WithAutorecord(Base)
				
				// execute
				val retVal = ai.selectTokenClasses(exCount)
				
				// verify
				assertResult(true)(Base.verify())
				assertResult(expected)(retVal)
			}
		}
		
		describe ("takeTurn") {
			it ("forwards its command to the base ai") {
				// setup
				val exPlayer = 54
				val exGameState = new GameState(null, null)
				val exMemo = new SimpleMemo()
				val expected = Seq(GameState.EndOfTurn)
				object Base extends StubPlayerAI{
					override def takeTurn(p:Int, gs:GameState, m:Memo) = {
						takeTurnCount = takeTurnCount + 1
						
						assertResult(exPlayer)(p)
						assertResult(exGameState)(gs)
						assertResult(exMemo)(m)
						
						expected
					}
					
					private var takeTurnCount:Int = 0
					def verify() = {1 == takeTurnCount}
				}
				val ai = new WithAutorecord(Base)
				
				// execute
				val retVal = ai.takeTurn(exPlayer, exGameState, exMemo)
				
				// verify
				assertResult(true)(Base.verify())
				assertResult(expected)(retVal)
			}
		}
		
		describe ("initialize") {
			it ("forwards its command to the base ai") {
				// setup
				val exPlayer = 54
				val exGameState = new GameState(null, null)
				val expected = new SimpleMemo
				object Base extends StubPlayerAI{
					override def initialize(player:Int, gameState:GameState) = {
						initializeCount = initializeCount + 1
						
						assertResult(exPlayer)(player)
						assertResult(exGameState)(gameState)
						
						expected
					}
					
					private var initializeCount:Int = 0
					def verify() = {1 == initializeCount}
				}
				val ai = new WithAutorecord(Base)
				
				// execute
				val retVal = ai.initialize(exPlayer, exGameState)
				
				// verify
				assertResult(true)(Base.verify())
				assertResult(expected)(retVal)
			}
		}
		
		describe ("notifyTurn") {
			
			it ("forwards its command to the base ai") {
				// setup
				val exPlayer = 54
				val exAction = GameState.TokenMoveResult(null, null)
				val exBeforeState = new GameState(null, null)
				val exAfterState = new GameState(null, null)
				val exMemo = new SimpleMemo
				val expected = new SimpleMemo
				object Base extends StubPlayerAI{
					override def notifyTurn(player:Int, action:GameState.Result, beforeState:GameState, afterState:GameState, memo:Memo):Memo = {
						notifyTurnCount = notifyTurnCount + 1
						
						assertResult(exPlayer)(player)
						assertResult(exAction)(action)
						assertResult(exBeforeState)(beforeState)
						assertResult(exAfterState)(afterState)
						assertResult(exMemo)(memo)
						
						expected
					}
					
					private var notifyTurnCount:Int = 0
					def verify() = {1 == notifyTurnCount}
				}
				val ai = new WithAutorecord(Base)
				
				// execute
				ai.notifyTurn(exPlayer, exAction, exBeforeState, exAfterState, exMemo)
				
				// verify
				assertResult(true)(Base.verify())
			}
			it ("Adds action to memo attack list") {
				val action = GameState.TokenMoveResult(null, null)
				val inMemo = new SimpleMemo
				val ai = new WithAutorecord(new StubPlayerAI)
				
				
				val retVal = ai.notifyTurn(0, action, null, null, inMemo)
				
				
				assertResult(true)(retVal.attacks.contains(action))
			}
			it ("Adds action to memo attack list (2)") {
				val action = GameState.TokenAttackDamageResult(null, null, Elements.Fire, Weaponkinds.Bladekind)
				val inMemo = new SimpleMemo
				val ai = new WithAutorecord(new StubPlayerAI)
				
				
				val retVal = ai.notifyTurn(0, action, null, null, inMemo)
				
				
				assertResult(true)(retVal.attacks.contains(action))
			}
			it ("Records Status") {
				val attackerIndex = ((3,3))
				val exStatus = Statuses.Confuse
				val action = GameState.TokenAttackStatusResult(attackerIndex, (2,2), exStatus)
				val inMemo = new SimpleMemo
				val ai = new WithAutorecord(new StubPlayerAI)
				
				
				val retVal = ai.notifyTurn(0, action, null, null, inMemo)
				
				
				assertResult(Some(exStatus))(retVal.suspisions(attackerIndex).atkStatus)
			}
			it ("Records Damage") {
				val attackerIndex = ((3,3))
				val exElem = Elements.Electric
				val exKind = Weaponkinds.Spearkind
				val action = GameState.TokenAttackDamageResult(attackerIndex, (2,2), exElem, exKind)
				val inMemo = new SimpleMemo
				val ai = new WithAutorecord(new StubPlayerAI)
				
				
				val retVal = ai.notifyTurn(0, action, null, null, inMemo)
				
				
				assertResult(Some(exElem))(retVal.suspisions(attackerIndex).atkElement)
				assertResult(Some(exKind))(retVal.suspisions(attackerIndex).atkWeapon)
			}
		}
	}
}
