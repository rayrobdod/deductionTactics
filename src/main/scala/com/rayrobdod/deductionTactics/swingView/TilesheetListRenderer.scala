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

import com.rayrobdod.boardGame.swingView.{RectangularTilesheet}
import com.rayrobdod.deductionTactics.SpaceClass
import javax.swing.{AbstractListModel, DefaultListCellRenderer,
		JList, ListCellRenderer, JLabel}


/**
 * A Listrenderer that shows a tilesheet name
 * @version a.6.0
 */
object TilesheetListRenderer extends ListCellRenderer[RectangularTilesheet[SpaceClass]]
{
	private val back:ListCellRenderer[java.lang.Object] = new DefaultListCellRenderer()
	
	def getListCellRendererComponent(list:JList[_ <: RectangularTilesheet[SpaceClass]], value:RectangularTilesheet[SpaceClass], index:Int,
			isSelected:Boolean, cellHasFocus:Boolean):java.awt.Component =
	{
		back.getListCellRendererComponent(
				list, value.name, index, isSelected, cellHasFocus)
	}
}
