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

import com.rayrobdod.deductionTactics.{CannonicalToken, Player}
import com.rayrobdod.boardGame.{Space,
		PhysicalStrikeCost, TokenMovementCost}
import java.awt.event.{MouseAdapter, MouseEvent}

/**
 * @author Raymond Dodge
 * @version a.5.0
 */
class MoveTokenMouseListener(owner:Player, space:Space, attackType:SellectAttackTypePanel) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent) {
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			val tokenOnThisSpace = owner.tokens.aliveOtherTokens.flatten.find{_.currentSpace == space}
			
			// don't move if there is a unit on this space and it is not out of reach
			if (!tokenOnThisSpace.isDefined || activeToken.currentSpace.distanceTo(
					space, activeToken, PhysicalStrikeCost) > activeToken.tokenClass.range.get)
			{
				activeToken.requestMoveTo(space)
			}
			
			tokenOnThisSpace.foreach{attackType.requestAttackForType(activeToken, _)}
		}
	}
	
	private var activeToken = owner.tokens.myTokens.head
	
	owner.tokens.myTokens.foreach{(token:CannonicalToken) =>
		token.selectedReactions_+={(isSelected) =>
			if (isSelected) activeToken = token
		}
	}
}
