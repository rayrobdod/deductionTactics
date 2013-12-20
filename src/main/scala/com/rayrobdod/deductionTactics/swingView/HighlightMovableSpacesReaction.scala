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

import java.awt.{Color, Graphics, Shape, Graphics2D}
import scala.collection.immutable.{Seq, Set}
import javax.swing.JComponent
import com.rayrobdod.boardGame.{Space, RectangularSpace,
		TokenMovementCost, PhysicalStrikeCost, Token => BoardGameToken}
import com.rayrobdod.deductionTactics.{Token, ListOfTokens}
import HighlightMovableSpacesReaction._

/**
 * A reaction that hilights the spaces a unit can move to or attack
 *
 * @author Raymond Dodge
 * @version a.5.2
 */
final class HighlightMovableSpacesReaction(token:Token, panel:BoardGamePanel, allTokens:ListOfTokens)
		extends Function1[Boolean, Unit] with Function0[Unit]
{
	private val _highlightLayer = new HighlightLayer();
	val highlightLayer:JComponent = _highlightLayer;
	
	private final class HighlightLayer extends JComponent {
		var currentSpeeds:Seq[Shape] = Seq.empty;
		var currentRanges:Seq[Shape] = Seq.empty;
		var maximumSpeeds:Seq[Shape] = Seq.empty;
		var maximumRanges:Seq[Shape] = Seq.empty;
		
		override def paintComponent(g:Graphics) {
			val g2 = g.asInstanceOf[Graphics2D]
			val g2Fill = {(x:Shape) => g2.fill(x)}
			
			g2.setColor(currentSpeedColor)
			currentSpeeds.foreach(g2Fill)
			g2.setColor(currentRangeColor)
			currentRanges.foreach(g2Fill)
			g2.setColor(maximumSpeedColor)
			maximumSpeeds.foreach(g2Fill)
			g2.setColor(maximumRangeColor)
			maximumRanges.foreach(g2Fill)
		}
	}
	
	highlightLayer.setBackground(transparentColor)
	highlightLayer.setOpaque(false)
	panel.centerpiece.add(highlightLayer,0)
	
	var wasSelected:Boolean = false
	
	override def apply(isSelected:Boolean) = {
		wasSelected = isSelected
		this.apply();
	}
	
	override def apply() = {
		
		if (wasSelected) {
			val tokenMaxSpeed = token.tokenClass.speed.getOrElse(0)
			val tokenMaxRange = token.tokenClass.range.getOrElse(0)
			val tokenCurSpeed = token.canMoveThisTurn
			val tokenCurRange = tokenMaxRange // the computation for curRangeSpaces checks for this
			
			//val noUnitIsInSpace:Function1[Space,Boolean] = {(x:Space) => ! allTokens.aliveTokens.flatten.map{_.currentSpace}.contains(x)}
			
			val curSpeedSpaces = token.currentSpace.spacesWithin(tokenCurSpeed, token, TokenMovementCost) - token.currentSpace
			val curRangeSpaces = if (token.canAttackThisTurn) {(curSpeedSpaces + token.currentSpace).map{_.spacesWithin(tokenMaxRange, token, PhysicalStrikeCost)}.flatten -- curSpeedSpaces - token.currentSpace} else {Seq.empty}
			val maxSpeedSpaces = token.currentSpace.spacesWithin(tokenMaxSpeed, token, TokenMovementCost) -- curSpeedSpaces - token.currentSpace
			val maxRangeSpaces = (maxSpeedSpaces + token.currentSpace).map{_.spacesWithin(tokenMaxRange, token, PhysicalStrikeCost)}.flatten -- maxSpeedSpaces -- curSpeedSpaces - token.currentSpace
			
			val spaceToShape = {(x:Space) => panel.centerpiece.spaceLocation(x)}
			
			_highlightLayer.currentSpeeds = Seq.empty ++ curSpeedSpaces.map(spaceToShape)
			_highlightLayer.currentRanges = Seq.empty ++ curRangeSpaces.map(spaceToShape)
			_highlightLayer.maximumSpeeds = Seq.empty ++ maxSpeedSpaces.map(spaceToShape)
			_highlightLayer.maximumRanges = Seq.empty ++ maxRangeSpaces.map(spaceToShape)
		} else {
			_highlightLayer.currentSpeeds = Seq.empty
			_highlightLayer.currentRanges = Seq.empty
			_highlightLayer.maximumSpeeds = Seq.empty
			_highlightLayer.maximumRanges = Seq.empty			
		}
		
		_highlightLayer.repaint();
	}
}

/**
 * Useful functions used by the associated class
 *
 * @author Raymond Dodge
 * @version a.5.2
 */
object HighlightMovableSpacesReaction
{
	val transparentColor  = new Color(0x00000000, true)
	val currentSpeedColor = new Color(0x803333DD, true)
	val currentRangeColor = new Color(0x80DD3333, true)
	val maximumSpeedColor = new Color(0x203333DD, true)
	val maximumRangeColor = new Color(0x20DD3333, true)
	
}
