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

import java.awt.Color
import scala.collection.immutable.{Seq, Set}
import javax.swing.{JLabel, Icon}
import com.rayrobdod.boardGame.{Space, RectangularSpace,
		TokenMovementCost, PhysicalStrikeCost, Token => BoardGameToken}
import com.rayrobdod.deductionTactics.{Token, ListOfTokens}
import com.rayrobdod.swing.SolidColorIcon

/**
 * A reaction that hilights the spaces a unit can move to or attack
 *
 * @author Raymond Dodge
 * @version 11 Feb 2012 - 12 Feb 2012
 * @version 13 Feb 2012 - using java.awt.EventQueue.invokeLater to eliminate OutOfBounds errors while repainting a component
 			It only seems to notice when removing components, not when adding them.
 * @version 13 Feb 2012 - no longer hilights spaces that cannot be moved to as attackable when guy cannot attack.
 * @version 15 Feb 2012 - now reacts to things that can change what a token can do.
 * @version 05 Apr 2012 - adding TokenCosts to Space and SpaceClass methods that now require them
 * @version 05 Apr 2012 - Made the RangeSpaces take into account that you can't attack from someplace you can't reach.
 * @version 06 Apr 2012 - Now reasponds to AttackFor* and Moved, rather than RequestAttackFor* and RequestMove,
			meaning it now updates when the token moves or attacks.
 * @version 28 Oct 2012 - changing a name due to change in BoardGamePanel
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
 */
class HighlightMovableSpacesReaction(token:Token, panel:BoardGamePanel, allTokens:ListOfTokens)
		extends Function1[Boolean, Unit] with BoardGameToken.MoveReactionType with Function0[Unit]
{
	val highlightLayer = new javax.swing.JPanel(panel.centerpiece.lowLayer.getLayout)
	highlightLayer.setPreferredSize(panel.centerpiece.lowLayer.getPreferredSize)
//	val highlightLayer = panel.tokenLayer

	val transparent = new java.awt.Color(0,0,0,0);
	
	val hilights:Map[Space, JLabel] = panel.centerpiece.spaces.map{(space:RectangularSpace) =>
		val label = new JLabel 
		label.setIcon(null)
		label.setBackground(transparent)
		label.setOpaque(false)
		highlightLayer.add(label)
		(space, label)
	}.toMap
	highlightLayer.setBackground(transparent)
	highlightLayer.setOpaque(false)
	panel.centerpiece.add(highlightLayer,0)
	
	var wasSelected:Boolean = false
	
	override def apply(isSelected:Boolean) = {
		wasSelected = isSelected
		this.apply();
	}
	override def apply(a:Space, b:Boolean) = this.apply();
	
	override def apply() = {
		highlightLayer.setPreferredSize(panel.centerpiece.lowLayer.getPreferredSize)
		
		hilights.foreach({(space:Space, label:JLabel) =>
			space match{
				case rs:RectangularSpace =>
				{
					val hilightedLabel = panel.centerpiece.spaceLabelMap(rs)
					label.setIcon(null)
				}
			}
		}.tupled)
		
		if (wasSelected) {
			val tokenMaxSpeed = token.tokenClass.speed.getOrElse(0)
			val tokenMaxRange = token.tokenClass.range.getOrElse(0)
			val tokenCurSpeed = token.canMoveThisTurn
			val tokenCurRange = tokenMaxRange // the computation for curRangeSpaces checks for this
			
			//val noUnitIsInSpace:Function1[Space,Boolean] = {(x:Space) => ! allTokens.aliveTokens.flatten.map{_.currentSpace}.contains(x)}
			
			val maxSpeedSpaces = token.currentSpace.spacesWithin(tokenMaxSpeed, token, TokenMovementCost) - token.currentSpace
			val maxRangeSpaces = (maxSpeedSpaces + token.currentSpace).map{_.spacesWithin(tokenMaxRange, token, PhysicalStrikeCost)}.flatten -- maxSpeedSpaces - token.currentSpace
			val curSpeedSpaces = token.currentSpace.spacesWithin(tokenCurSpeed, token, TokenMovementCost) - token.currentSpace
			val curRangeSpaces = if (token.canAttackThisTurn) {(curSpeedSpaces + token.currentSpace).map{_.spacesWithin(tokenMaxRange, token, PhysicalStrikeCost)}.flatten -- curSpeedSpaces - token.currentSpace} else {Seq.empty}
			
			import HighlightMovableSpacesReaction._
			maxSpeedSpaces.foreach{hilights(_).setIcon(maximumSpeedIcon)}
			maxRangeSpaces.foreach{hilights(_).setIcon(maximumRangeIcon)}
			curSpeedSpaces.foreach{hilights(_).setIcon(currentSpeedIcon)}
			curRangeSpaces.foreach{hilights(_).setIcon(currentRangeIcon)}
		}
	}
}

/**
 * Useful functions used by the associated class
 *
 * @author Raymond Dodge
 * @version 11 Feb 2012 - 12 Feb 2012
 * @version 2013 Aug 07 - replacing custom impl with com.rayrobdod.swing.SolidColorIcon
 */
object HighlightMovableSpacesReaction
{
	val currentSpeedIcon:Icon = new SolidColorIcon(new Color(0x803333DD, true), 32, 32)
	val currentRangeIcon:Icon = new SolidColorIcon(new Color(0x80DD3333, true), 32, 32)
	val maximumSpeedIcon:Icon = new SolidColorIcon(new Color(0x203333DD, true), 32, 32)
	val maximumRangeIcon:Icon = new SolidColorIcon(new Color(0x20DD3333, true), 32, 32)
	
}
