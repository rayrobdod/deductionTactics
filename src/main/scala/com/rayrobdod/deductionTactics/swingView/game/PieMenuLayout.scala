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

import java.awt.{Point, Dimension, Component, Container, LayoutManager}
import java.awt.{Graphics, Graphics2D}

/**
 * A layer to be place on top of a [[com.rayrobdod.boardGame.swingView.RectangularTilemapComponent]];
 * it will show a cursor on top of the space most recently called with update 
 *
 * @author Raymond Dodge
 * @since a.6.0
 */
final class PieMenuLayout extends LayoutManager {
	private[this] val MAX_COMPS_SHOWN = 8
	private[this] val OFFSET_Y = 16
	private[this] var _center:Point = new Point()
	def center:Point = _center
	def center_=(x:Point):Unit = {
		this._center = x
	}
	
	def addLayoutComponent(name:String, comp:Component):Unit = {}
	def removeLayoutComponent(comp:Component):Unit = {}
	def minimumLayoutSize(comp:Container):Dimension = new Dimension()
	def preferredLayoutSize(comp:Container):Dimension = new Dimension()
	
	def layoutContainer(container:Container):Unit = {
		val count = container.getComponentCount
		
		if (count > 0) {
			val comp1 = container.getComponent(0)
			comp1.setSize(comp1.getPreferredSize)
			comp1.setLocation(new Point(
				center.x - (comp1.getSize.width / 2),
				center.y - comp1.getSize.height - OFFSET_Y
			))
		}
		if (count > 1) {
			val comp1 = container.getComponent(1)
			comp1.setSize(comp1.getPreferredSize)
			comp1.setLocation(new Point(
				center.x - (comp1.getSize.width / 2),
				center.y + OFFSET_Y
			))
		}
	}
}
