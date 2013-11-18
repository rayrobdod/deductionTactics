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
import com.rayrobdod.deductionTactics.ai.attackRangeOf

/**
 * @author Raymond Dodge
 * @version 11 Feb 2012
 * @version 12 Feb 2012 - made do the RequestMove thing instead of the Moved thing
 * @version 21 Mar 2012 - modified reactions for new event model
 * @version 06 Apr 2012 - adding SellectAttackTypePanel parameter
 * @version 30 May 2012 - allowing for long-range attacks without running out of movment
 * @version 01 Jun 2012 - Now only resonds to BUTTON1 mouse clicks.
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
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
