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

import java.awt.{Shape, Rectangle, Polygon}
import java.awt.geom.Area
import java.awt.Color
import java.awt.{Graphics, Graphics2D}
import javax.swing.JComponent

/**
 * A layer to be place on top of a [[com.rayrobdod.boardGame.swingView.RectangularTilemapComponent]];
 * it will show a cursor on top of the space most recently called with update 
 *
 * @author Raymond Dodge
 * @since a.6.0
 */
final class CursorLayer(
	spaceBounds:Function1[(Int,Int), Shape]
) extends JComponent {
	private[this] var currentShape:Shape = new Area()
	
	override def paintComponent(g:Graphics) {
		val g2 = g.asInstanceOf[Graphics2D]
		
		g2.setColor(this.getForeground())
		g2.fill(currentShape)
	}
	
	def update(spaceIndex:(Int, Int)):Unit = {
		currentShape = cursorShape(spaceBounds(spaceIndex))
		this.repaint()
	}
	
	def clear():Unit = {
		currentShape = new Area()
		this.repaint()
	}
	
	private[this] def cursorShape(bounds:Shape):Shape = {
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
		
		val retVal = new Area()
		retVal.add(new Area(new Polygon(xLeftPoints,  yTopPoints,    xLeftPoints.length)))
		retVal.add(new Area(new Polygon(xLeftPoints,  yBottomPoints, xLeftPoints.length)))
		retVal.add(new Area(new Polygon(xRightPoints, yTopPoints,    xLeftPoints.length)))
		retVal.add(new Area(new Polygon(xRightPoints, yBottomPoints, xLeftPoints.length)))
		retVal
	}
}
