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

class SelectAction(
		selectedSpace:Function0[RectangularFieldIndex],
		currentTokens:Function0[ListOfTokens],
		field:RectangularField[SpaceClass],
		selectedTokenIndex:CurrentlySelectedTokenProperty,
		pieMenuLayer:javax.swing.JPanel,
		generateButton:(String, GameState.Action) => javax.swing.JButton,
		playerNumber:Int
) extends AbstractAction("Select") {
	def actionPerformed(e:ActionEvent):Unit = {
		pieMenuLayer.removeAll()
		
		val tokenOnThisSpace:Option[Token] = currentTokens().aliveTokens.flatten.filter{_.currentSpace == field(selectedSpace())}.headOption
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
			if (index._1 == playerNumber) {
				// selected token is mine
				
				tokenOnThisSpace.fold{
					val b = generateButton("moveToButton", GameState.TokenMove(currentTokens().tokens(index), field(selectedSpace())))
					pieMenuLayer.add(b)
					b.requestFocusInWindow()
				}{t =>
					val b = generateButton("damageAttackButton", GameState.TokenAttackDamage(currentTokens().tokens(index), t))
					pieMenuLayer.add(b)
					pieMenuLayer.add(generateButton("statusAttackButton", GameState.TokenAttackStatus(currentTokens().tokens(index), t)))
					b.requestFocusInWindow()
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
		val nextToken = rotation.filter{ListOfTokens.aliveFilter}.filter{x => x.canMoveThisTurn > 0 || x.canAttackThisTurn}.head
		val nextTokenIndex = currentTokens().indexOf(nextToken)
		val nextSpaceIndex = field.find{_._2 == nextToken.currentSpace}.get._1
		selectedToken.set(Option(nextTokenIndex))
		selectedSpace.set(nextSpaceIndex)
	}
}
