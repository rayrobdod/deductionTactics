package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.{CannonicalToken, RequestMove,
		Player, RequestAttackForDamage, ListOfTokens}
import com.rayrobdod.boardGame.{Space, BeSelected,
		PhysicalStrikeCost, TokenMovementCost}
import java.awt.event.{MouseAdapter, MouseEvent}
import com.rayrobdod.deductionTactics.ai.attackRangeOf

/**
 * A MouseListener that will cause the token on the current space to be selected 
 * 
 * @author Raymond Dodge
 * @version 01 Jun 2012
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 */
class SelectTokenOnSpaceMouseListener(space:Space, tokens:ListOfTokens) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent) {
		if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
			val tokenOnThisSpace = tokens.aliveTokens.flatten.find{_.currentSpace == space}
			
			tokenOnThisSpace.foreach{_ ! BeSelected(true)}
		}
	}
}
