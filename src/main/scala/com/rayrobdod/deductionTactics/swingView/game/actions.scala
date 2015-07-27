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
package swingView.game

import java.awt.Shape
import java.awt.event.ActionEvent
import javax.swing.{JPanel, AbstractAction}
import com.rayrobdod.boardGame.{RectangularField, RectangularFieldIndex, StrictRectangularSpace}

/**
 * Upon a call to action performed, this will move the cursor
 * and then clear the context-sensitive buttons
 */
class MoveCursorAction(
		name:String,
		adjustment:Function1[StrictRectangularSpace[SpaceClass], StrictRectangularSpace[SpaceClass]],
		selectedSpace:CurrentlySelectedSpaceProperty,
		field:RectangularField[SpaceClass],
		pieMenuLayer:JPanel,
		pieMenuLayout:PieMenuLayout,
		spaceBounds:Function1[(Int,Int), Shape]
) extends AbstractAction(name) {
	def actionPerformed(e:ActionEvent):Unit = {
		selectedSpace.set({
			
			val a = field(selectedSpace.get)
			val b = adjustment(a)
			field.find{_._2 == b}.get._1
		});
		{
			pieMenuLayer.removeAll()
			pieMenuLayout.center = {
				val spaceShape = spaceBounds(selectedSpace.get)
				val spaceRect = spaceShape.getBounds()
				val spaceCenter = new java.awt.Point(
					spaceRect.x + spaceRect.width / 2,
					spaceRect.y + spaceRect.height / 2
				)
				spaceCenter
			}
		}
	}
}

/**
 * Upon a call to actionPerformed, sets the selected token to None, and removes
 * all children of pieMenuLayer
 */
class ClearSelectionAction(
		selectedSpace:CurrentlySelectedSpaceProperty,
		selectedToken:CurrentlySelectedTokenProperty,
		pieMenuLayer:javax.swing.JPanel
) extends AbstractAction("ClearSelection") {
	def actionPerformed(e:ActionEvent):Unit = {
		selectedToken.set(None)
		pieMenuLayer.removeAll()
		pieMenuLayer.validate()
	}
}

/**
 * Upon a call to actionPerformed, selects a particular space and 
 * depending on context may select the token on that space as well.
 */
class SelectAction(
		selectedSpaceIndex:Function0[RectangularFieldIndex],
		currentTokens:Function0[ListOfTokens],
		field:RectangularField[SpaceClass],
		selectedTokenIndex:CurrentlySelectedTokenProperty,
		pieMenuLayer:javax.swing.JPanel,
		generateButton:(String, GameState.Action) => javax.swing.JButton,
		playerNumber:Int
) extends AbstractAction("Select") {
	def actionPerformed(e:ActionEvent):Unit = {
		pieMenuLayer.removeAll()
		
		val selectedSpace:StrictRectangularSpace[SpaceClass] = field(selectedSpaceIndex())
		val tokenOnThisSpace:Option[Token] = currentTokens().aliveTokens.flatten.filter{_.currentSpace == selectedSpace}.headOption
		val tokenOnThisSpaceIndex:Option[TokenIndex] = tokenOnThisSpace.map{currentTokens().indexOf _}
		
		val newSelectedTokenIndex = selectedTokenIndex.get.fold[Option[TokenIndex]]{
			// no token is selected
			
			tokenOnThisSpaceIndex.getOrElse{
				val b = generateButton("endTurnButton", GameState.EndOfTurn)
				pieMenuLayer.add(b)
				b.requestFocusInWindow()
			}
			
			tokenOnThisSpaceIndex
		}{(index) =>
			val selectedToken:Token = currentTokens().tokens(index)
			val moveCostFun = new MoveToCostFunction(selectedToken, currentTokens())
			val attackCostFun = new AttackCostFunction(selectedToken, currentTokens())
			
			if (index._1 == playerNumber) {
				// selected token is mine
				
				if (selectedToken.currentSpace.distanceTo(selectedSpace, moveCostFun) <= selectedToken.canMoveThisTurn) {
					// if selected space is within speed, present option to move to space
					val b = generateButton("moveToButton", GameState.TokenMove(currentTokens().tokens(index), selectedSpace))
					pieMenuLayer.add(b)
					b.requestFocusInWindow()
				} else {
					// if selected space is not within speed, present option to move as far along path to space as possible
					val path = selectedToken.currentSpace.pathTo(selectedSpace, moveCostFun)
					val endSpace = path.takeWhile{selectedToken.currentSpace.distanceTo(_, moveCostFun) <= selectedToken.canMoveThisTurn}.last
					
					if (endSpace != selectedToken.currentSpace) {
						val b = generateButton("moveTowardsButton", GameState.TokenMove(currentTokens().tokens(index), endSpace))
						pieMenuLayer.add(b)
						b.requestFocusInWindow()
					}
				}
				
				tokenOnThisSpace.foreach[Unit]{t =>
					if (selectedToken.canAttackThisTurn &&
						tokenOnThisSpaceIndex.get._1 != playerNumber
					) {
						if (selectedToken.currentSpace.distanceTo(t.currentSpace, attackCostFun) <= selectedToken.tokenClass.map{_.range}.getOrElse{0}) {
							// enemy token is in range
							val b = generateButton("damageAttackButton", GameState.TokenAttackDamage(currentTokens().tokens(index), t))
							pieMenuLayer.add(b)
							pieMenuLayer.add(generateButton("statusAttackButton", GameState.TokenAttackStatus(currentTokens().tokens(index), t)))
							b.requestFocusInWindow()
						} else if (ai.attackRangeOf(selectedToken, currentTokens()).contains(t.currentSpace)) {
							// enemy token is in range + speed
							
							// TODO: move then attack
						} else {
							// enemy cannot be reached
						}
					} else {
						// cannot attack other token at all
					}
				}
				
				selectedTokenIndex.get
			} else {
				// selected token is not mine
				tokenOnThisSpaceIndex
			}
		}
		selectedTokenIndex.set(newSelectedTokenIndex)
		
		pieMenuLayer.validate()
	}
}

/**
 * Upon a call to actionPerformed, goes through the tokens listed in
 * currentTokens and selects one that is able to perform actions and
 * owned by the player indicated by playerNumber
 * 
 */
class SelectNextActionableTokenAction(
		selectedSpace:CurrentlySelectedSpaceProperty,
		selectedToken:CurrentlySelectedTokenProperty,
		currentTokens:Function0[ListOfTokens],
		field:RectangularField[SpaceClass],
		playerNumber:Int
) extends AbstractAction("FindNextActionableToken") {
	def actionPerformed(e:ActionEvent):Unit = {
		val initialPlayerTokenIndex:Int = selectedToken.get.filter{_._1 == playerNumber}.map{_._2}.getOrElse{-1}
		val rotation = (currentTokens().alivePlayerTokens(playerNumber) ++ currentTokens().alivePlayerTokens(playerNumber)).drop(initialPlayerTokenIndex + 1)
		val nextToken = rotation.filter{ListOfTokens.aliveFilter}.filter{x => x.canMoveThisTurn > 0 || x.canAttackThisTurn}.headOption
		val nextTokenIndex = nextToken.map{x => currentTokens().indexOf(x)}
		val nextSpaceIndex = nextToken.map{x => field.find{_._2 == x.currentSpace}.get._1}
		selectedToken.set(nextTokenIndex)
		nextSpaceIndex.foreach{x => selectedSpace.set(x)}
	}
}
