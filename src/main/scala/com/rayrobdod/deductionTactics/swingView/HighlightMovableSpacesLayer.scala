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

import java.awt.{Component, Color, Graphics, Shape, Graphics2D}
import scala.collection.immutable.{Seq, Set}
import javax.swing.JComponent
import com.rayrobdod.boardGame.{Space, RectangularSpace, RectangularField, StrictRectangularSpace}
import com.rayrobdod.boardGame.swingView.RectangularTilemapComponent
import com.rayrobdod.deductionTactics.{Token, ListOfTokens, SpaceClass, AttackCostFunction, MoveToCostFunction}
import HighlightMovableSpacesLayer._

/**
 * A component that can be added to a FieldComponent's tokenLayer to
 * show what spaces a token can move to 
 *
 * @author Raymond Dodge
 * @since a.6.0
 */
final class HighlightMovableSpacesLayer(tilemap:RectangularTilemapComponent) extends JComponent
{
	var currentSpeeds:Seq[Shape] = Seq.empty;
	var currentRanges:Seq[Shape] = Seq.empty;
	var maximumSpeeds:Seq[Shape] = Seq.empty;
	var maximumRanges:Seq[Shape] = Seq.empty;
	
	override def paintLayer(c:Component, g:Graphics, offsetX:Int, offsetY:Int) {
		val g2 = g.asInstanceOf[Graphics2D]
		g2.translate(-offsetX, -offsetY)
		val g2Fill = {(x:Shape) => g2.fill(x)}
		
		g2.setColor(currentSpeedColor)
		currentSpeeds.foreach(g2Fill)
		g2.setColor(currentRangeColor)
		currentRanges.foreach(g2Fill)
		g2.setColor(maximumSpeedColor)
		maximumSpeeds.foreach(g2Fill)
		g2.setColor(maximumRangeColor)
		maximumRanges.foreach(g2Fill)
		
		g2.translate(offsetX, offsetY)
	}
	
	
	
	
	def update(selectedTokenOption:Option[Token], list:ListOfTokens, field:RectangularField[SpaceClass]) = {
		
		selectedTokenOption.foreach{(selectedToken) =>
			val tokenMaxSpeed = selectedToken.tokenClass.map{_.speed}.getOrElse(0)
			val tokenMaxRange = selectedToken.tokenClass.map{_.range}.getOrElse(0)
			val tokenCurSpeed = selectedToken.canMoveThisTurn
			val tokenCurRange = tokenMaxRange // the computation for curRangeSpaces checks for this
			
			val atf = new AttackCostFunction(selectedToken, list)
			val mcf = new MoveToCostFunction(selectedToken, list)
			
			val curSpeedSpaces = selectedToken.currentSpace.spacesWithin(tokenCurSpeed, mcf) - selectedToken.currentSpace
			val curRangeSpaces = if (selectedToken.canAttackThisTurn) {(curSpeedSpaces + selectedToken.currentSpace).map{_.spacesWithin(tokenMaxRange, atf)}.flatten -- curSpeedSpaces - selectedToken.currentSpace} else {Seq.empty}
			val maxSpeedSpaces = selectedToken.currentSpace.spacesWithin(tokenMaxSpeed, mcf) -- curSpeedSpaces - selectedToken.currentSpace
			val maxRangeSpaces = (maxSpeedSpaces + selectedToken.currentSpace).map{_.spacesWithin(tokenMaxRange, atf)}.flatten -- maxSpeedSpaces -- curSpeedSpaces - selectedToken.currentSpace
			
			val spaceToShape = {(x:Space[SpaceClass]) => tilemap.spaceBounds(field.map{x => ((x._2:Space[SpaceClass], x._1)) }.apply(x))}
			
			this.currentSpeeds = Seq.empty ++ curSpeedSpaces.map(spaceToShape)
			this.currentRanges = Seq.empty ++ curRangeSpaces.map(spaceToShape)
			this.maximumSpeeds = Seq.empty ++ maxSpeedSpaces.map(spaceToShape)
			this.maximumRanges = Seq.empty ++ maxRangeSpaces.map(spaceToShape)
		} 
		selectedTokenOption.getOrElse{
			this.currentSpeeds = Seq.empty
			this.currentRanges = Seq.empty
			this.maximumSpeeds = Seq.empty
			this.maximumRanges = Seq.empty
		}
	}
}

/**
 * Useful functions used by the associated class
 *
 * @author Raymond Dodge
 * @since a.6.0
 */
object HighlightMovableSpacesLayer
{
	val transparentColor  = new Color(0x00000000, true)
	val currentSpeedColor = new Color(0x803333DD, true)
	val currentRangeColor = new Color(0x80DD3333, true)
	val maximumSpeedColor = new Color(0x203333DD, true)
	val maximumRangeColor = new Color(0x20DD3333, true)
}
