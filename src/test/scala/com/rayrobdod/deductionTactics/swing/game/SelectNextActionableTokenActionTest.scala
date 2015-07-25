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
package com.rayrobdod.deductionTactics.swingView.game

import org.scalatest.FunSpec
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.deductionTactics.{Token, ListOfTokens}
import com.rayrobdod.deductionTactics.{SpaceClass, SlowPassageSpaceClass}

class SelectNextActionableTokenActionTest extends FunSpec {
	
	val field:RectangularField[SpaceClass] = RectangularField(Seq.fill(7,7){SlowPassageSpaceClass.apply})
	val allActionableTokens = new ListOfTokens(Seq(
			Seq(new Token(field((1,1)), canAttackThisTurn = true), new Token(field((5,5)), canAttackThisTurn = true)),
			Seq(new Token(field((2,2)), canAttackThisTurn = true), new Token(field((3,4)), canAttackThisTurn = true))
	))
	val halfActionableTokens = new ListOfTokens(Seq(
			Seq(new Token(field((1,1)), canAttackThisTurn = false), new Token(field((5,5)), canAttackThisTurn = true)),
			Seq(new Token(field((2,2)), canAttackThisTurn = true), new Token(field((3,4)), canAttackThisTurn = false))
	))
	val noActionableTokens = new ListOfTokens(Seq(
			Seq(new Token(field((1,1)), canAttackThisTurn = false), new Token(field((5,5)), canAttackThisTurn = false)),
			Seq(new Token(field((2,2)), canAttackThisTurn = false), new Token(field((3,4)), canAttackThisTurn = false))
	))
	val playerOneActionableTokens = new ListOfTokens(Seq(
			Seq(new Token(field((1,1)), canAttackThisTurn = false), new Token(field((5,5)), canAttackThisTurn = false)),
			Seq(new Token(field((2,2)), canAttackThisTurn = true), new Token(field((3,4)), canAttackThisTurn = true))
	))
	
	
	describe ("SelectNextActionableTokenAction") {
		it ("if no token is selected, selects player first token (player = 0)") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => allActionableTokens}, field, 0)
			
			dut.actionPerformed(null)
			
			assertResult(Some((0,0))){selToken.get}
			assertResult(((1,1))){selSpace.get}
		}
		it ("if no token is selected, selects player first token (player = 1)") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => allActionableTokens}, field, 1)
			
			dut.actionPerformed(null)
			
			assertResult(Some((1,0))){selToken.get}
			assertResult(((2,2))){selSpace.get}
		}
		it ("if enemy token is selected, selects player first token (player = 0)") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			selToken.set(Some((1,0)))
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => allActionableTokens}, field, 0)
			
			dut.actionPerformed(null)
			
			assertResult(Some((0,0))){selToken.get}
			assertResult(((1,1))){selSpace.get}
		}
		it ("if player's first token is selected, selects player's second token (player = 0)") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			selToken.set(Some((0,0)))
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => allActionableTokens}, field, 0)
			
			dut.actionPerformed(null)
			
			assertResult(Some((0,1))){selToken.get}
			assertResult(((5,5))){selSpace.get}
		}
		it ("if player's last token is selected, selects player's first token (player = 0)") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			selToken.set(Some((0,1)))
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => allActionableTokens}, field, 0)
			
			dut.actionPerformed(null)
			
			assertResult(Some((0,0))){selToken.get}
			assertResult(((1,1))){selSpace.get}
		}
		it ("skips tokens that cannot perform actions") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => halfActionableTokens}, field, 0)
			
			dut.actionPerformed(null)
			
			assertResult(Some((0,1))){selToken.get}
			assertResult(((5,5))){selSpace.get}
		}
		it ("deselects token; does not change selected space if there are no tokens that can perform actions") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			selToken.set(Some((1,1)))
			selSpace.set(((5,1)))
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => noActionableTokens}, field, 0)
			
			dut.actionPerformed(null)
			
			assertResult(None){selToken.get}
			assertResult(((5,1))){selSpace.get}
		}
		it ("deselects token; does not change selected space if all tokens that can perform actions are owned by the opponents") {
			val selSpace = new CurrentlySelectedSpaceProperty
			val selToken = new CurrentlySelectedTokenProperty
			selToken.set(Some((1,1)))
			selSpace.set(((5,1)))
			
			val dut = new SelectNextActionableTokenAction(selSpace, selToken, {() => playerOneActionableTokens}, field, 0)
			
			dut.actionPerformed(null)
			
			assertResult(None){selToken.get}
			assertResult(((5,1))){selSpace.get}
		}
	}
	
}
