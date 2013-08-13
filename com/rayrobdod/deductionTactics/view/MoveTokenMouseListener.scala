package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics.{CannonicalToken, RequestMove,
		Player, RequestAttackForDamage}
import com.rayrobdod.boardGame.{Space, BeSelected,
		PhysicalStrikeCost, TokenMovementCost}
import javax.swing.{JDialog, JList}
import java.awt.event.{MouseAdapter, MouseEvent}
import com.rayrobdod.deductionTactics.ai.attackRangeOf

/**
 * @author Raymond Dodge
 * @version 11 Feb 2012
 * @version 12 Feb 2012 - made do the RequestMove thing instead of the Moved thing
 * @version 21 Mar 2012 - modified reactions for new event model
 * @version 06 Apr 2012 - adding SellectAttackTypePanel parameter
 * @version 30 May 2012 - allowing for long-range attacks without running out of movment
 */
class MoveTokenMouseListener(owner:Player, space:Space, attackType:SellectAttackTypePanel) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent) {
		if (e.getClickCount() == 2) {
			val tokenOnThisSpace = owner.tokens.aliveOtherTokens.flatten.find{_.currentSpace == space}
			
			// don't move if there is a unit on this space and it is not out of reach
			if (!tokenOnThisSpace.isDefined ||
					activeToken.currentSpace.distanceTo(space, activeToken, PhysicalStrikeCost) > activeToken.tokenClass.range.get)
			{
				activeToken.movementEndedLock.synchronized {
					owner ! RequestMove(activeToken, space)
					activeToken.movementEndedLock.wait
				}
			}
			
			tokenOnThisSpace.foreach{owner ! attackType.requestAttackForType(activeToken, _)}
		}
	}
	
	private var activeToken = owner.tokens.myTokens.head
	
	owner.tokens.myTokens.foreach{(token:CannonicalToken) =>
		token.reactions += {
			case BeSelected(isSelected) => {
				if (isSelected) activeToken = token
			}
		}
	}
}
