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
package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.{CannonicalToken,
		Player, ListOfTokens}
import com.rayrobdod.boardGame.{Space,
		PhysicalStrikeCost, TokenMovementCost}
import java.awt.event.{MouseAdapter, MouseEvent}
import com.rayrobdod.deductionTactics.ai.attackRangeOf

/**
 * A MouseListener that will cause the token on the current space to be selected 
 * 
 * @author Raymond Dodge
 * @version a.5.0
 */
class SelectTokenOnSpaceMouseListener(space:Space, tokens:ListOfTokens) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent) {
		if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
			val tokenOnThisSpace = tokens.aliveTokens.flatten.find{_.currentSpace == space}
			
			tokenOnThisSpace.foreach{_.beSelected(true)}
		}
	}
}
