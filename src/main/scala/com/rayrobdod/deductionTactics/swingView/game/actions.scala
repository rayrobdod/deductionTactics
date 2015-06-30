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
package swingView.game

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import com.rayrobdod.boardGame.{RectangularField, RectangularFieldIndex, StrictRectangularSpace}

class MoveCursorAction(
		name:String,
		adjustment:Function1[StrictRectangularSpace[SpaceClass], StrictRectangularSpace[SpaceClass]],
		selectedSpace:CurrentlySelectedSpaceProperty,
		field:RectangularField[SpaceClass]
) extends AbstractAction(name) {
	def actionPerformed(e:ActionEvent):Unit = {
		selectedSpace.set(
			selectedSpace.get
				.map{field}
				.map{adjustment}
				.map{x => field.find{_._2 == x}.map{_._1}}.flatten
		)
	}
}

class ClearSelectionAction(
		selectedSpace:CurrentlySelectedSpaceProperty,
		setSelectedTokenIndex:() => Unit,
		highlightLayer:HighlightMovableSpacesLayer,
		pieMenuLayer:javax.swing.JPanel
) extends AbstractAction("ClearSelection") {
	def actionPerformed(e:ActionEvent):Unit = {
		selectedSpace.set(None)
		setSelectedTokenIndex.apply()
		highlightLayer.update(None, new ListOfTokens(Nil), null)
		pieMenuLayer.removeAll()
	}
}

class SelectAction(
		selectedSpace:Function0[Option[RectangularFieldIndex]],
		currentTokens:Function0[ListOfTokens],
		field:RectangularField[SpaceClass],
		getSelectedTokenIndex:Function0[Option[TokenIndex]],
		setSelectedTokenIndex:Function1[Option[TokenIndex], Any],
		pieMenuLayer:javax.swing.JPanel,
		generateButton:(String, GameState.Action) => javax.swing.JButton,
		playerNumber:Int
) extends AbstractAction("Select") {
	def actionPerformed(e:ActionEvent):Unit = {
		val tokenOnThisSpace:Option[Token] = currentTokens().aliveTokens.flatten.filter{_.currentSpace == field(selectedSpace().get)}.headOption
		val tokenOnThisSpaceIndex:Option[TokenIndex] = tokenOnThisSpace.map{currentTokens().indexOf _}
		
		val newSelectedTokenIndex = getSelectedTokenIndex().fold[Option[TokenIndex]]{
			// no token is selected
			
			tokenOnThisSpaceIndex.getOrElse{
				pieMenuLayer.add(generateButton("endTurnButton", GameState.EndOfTurn))
			}
			
			tokenOnThisSpaceIndex
		}{(index) =>
			if (index._1 == playerNumber) {
				// selected token is mine
				
				tokenOnThisSpace.fold{
					pieMenuLayer.add(generateButton("moveToButton", GameState.TokenMove(currentTokens().tokens(index), field(selectedSpace().get))))
				}{t =>
					pieMenuLayer.add(generateButton("damageAttackButton", GameState.TokenAttackDamage(currentTokens().tokens(index), t)))
					pieMenuLayer.add(generateButton("statusAttackButton", GameState.TokenAttackStatus(currentTokens().tokens(index), t)))
				}
				
				getSelectedTokenIndex()
				
			} else {
				// selected token is not mine
				tokenOnThisSpaceIndex
			}
		}
		setSelectedTokenIndex(newSelectedTokenIndex)
	}
}
