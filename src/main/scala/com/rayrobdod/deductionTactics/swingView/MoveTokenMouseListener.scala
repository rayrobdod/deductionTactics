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
package swingView

import com.rayrobdod.boardGame.{Space}
import java.awt.event.{MouseAdapter, MouseEvent}

/**
 * @author Raymond Dodge
 * @version a.6.0
 */
class MoveTokenMouseListener(
		player:Int,
		tokens:Function0[ListOfTokens],
		space:Space[SpaceClass],
		attackType:SellectAttackTypePanel,
		onSelectedTokenChanged:Function2[Token, ListOfTokens, Any],
		writeGameAction:Function1[GameState.Action, Any],
		activeToken:SharedActiveTokenProperty
) extends MouseAdapter {
	
	override def mouseClicked(e:MouseEvent) {
		val tokenOnThisSpace = tokens().tokens.flatten.find{_.currentSpace == space}
		
		// TODO: right click, rather than any other than button1
		if (e.getButton() != MouseEvent.BUTTON1) {
			
			if (tokenOnThisSpace.isDefined) {
				System.out.println("Token selected")
				activeToken.value = tokenOnThisSpace.get
				onSelectedTokenChanged(tokenOnThisSpace.get, tokens.apply)
			} else {
				System.out.println("No token  to select")
				// do nothing
			}
			
			
		} else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			// if clicked on a token, attack. Else, move.
			
			if (tokenOnThisSpace.isDefined) {
				System.out.println("Token Attack")
				writeGameAction(
					attackType.requestAttackForType(activeToken.value, tokenOnThisSpace.get)
				)
			} else {
				System.out.println("Token Move")
				writeGameAction(
					GameState.TokenMove(activeToken.value, space)
				)
			}
		}
	}
	
}



/**
 * So that all the TokenMouseListeners can agree on who the active token is
 * @todo utilities? JavaFX Property?
 */
final class SharedActiveTokenProperty {
	var value:Token = null
}

/**
 * So that all the TokenMouseListeners can be updated when the ListOfTOkens changes
 * @todo utilities? JavaFX Property?
 */
final class ListOfTokensProperty extends Function0[ListOfTokens] {
	var value:ListOfTokens = null
	
	def apply = value
}
