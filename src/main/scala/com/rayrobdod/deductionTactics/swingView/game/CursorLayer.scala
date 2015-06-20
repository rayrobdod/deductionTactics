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
package com.rayrobdod.deductionTactics.swingView.game

import java.awt.{Component, Color, Graphics, Shape, Graphics2D}
import scala.collection.immutable.{Seq, Set}
import javax.swing.JComponent
import com.rayrobdod.boardGame.{Space, RectangularSpace, RectangularField, StrictRectangularSpace}
import com.rayrobdod.boardGame.swingView.RectangularTilemapComponent
import com.rayrobdod.deductionTactics.{Token, ListOfTokens, SpaceClass, AttackCostFunction, MoveToCostFunction}

/**
 * A component that can be added to a FieldComponent's tokenLayer to
 * show what spaces a token can move to 
 *
 * @author Raymond Dodge
 * @since a.6.0
 */
final class CursorLayer(
	tilemap:RectangularTilemapComponent
) extends JComponent {
	//val color = new Color(0xFF33FF33)
	val color = new Color(0xFF000000)
	var currentShapes:Seq[Shape] = Nil
	
	override def paintComponent(g:Graphics) {
		val g2 = g.asInstanceOf[Graphics2D]
		val g2Fill = {(x:Shape) => g2.fill(x)}
		
		g2.setColor(color)
		currentShapes.foreach(g2Fill)
	}
	
	
	
	def update(space:(Int, Int)) = {
		val spaceBounds = tilemap.spaceBounds(space)
		currentShapes = RectangularCursorShape(spaceBounds)
		
		this.repaint()
	}
	
	private def RectangularCursorShape(bounds:Shape):Seq[Shape] = {
		val bounds2 = bounds.getBounds()
		val left = bounds2.x
		val top  = bounds2.y
		val right  = left + bounds2.width
		val bottom = top  + bounds2.height
		val width = 3
		val length = bounds2.width / 3
		
		val xLeftPoints   = Array[Int](left, left + length, left + length, left + width, left + width, left, left)
		val xRightPoints  = Array[Int](right, right - length, right - length, right - width, right - width, right, right)
		val yTopPoints    = Array[Int](top, top, top + width, top + width, top + length, top + length, top)
		val yBottomPoints = Array[Int](bottom, bottom, bottom - width, bottom - width, bottom - length, bottom - length, bottom)
		
		Seq(
			new java.awt.Polygon(xLeftPoints,  yTopPoints,    xLeftPoints.length),
			new java.awt.Polygon(xLeftPoints,  yBottomPoints, xLeftPoints.length),
			new java.awt.Polygon(xRightPoints, yTopPoints,    xLeftPoints.length),
			new java.awt.Polygon(xRightPoints, yBottomPoints, xLeftPoints.length)
		)
	}
}
