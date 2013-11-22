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

import com.rayrobdod.boardGame.swingView.{RectangularTilesheet, RectangularTilesheetLoader}
import javax.swing.{AbstractListModel, DefaultListCellRenderer,
		JList, ListCellRenderer, JLabel}

/**
 * A ListModel of all tilesheets.
 * Would thought this could have a more immediate use
 * @author Raymond Dodge
 */
object AvailibleTilesheetListModel extends AbstractListModel[RectangularTilesheet]
{
	val tilesheets = new RectangularTilesheetLoader("com.rayrobdod.deductionTactics.view.tilesheet").toSeq
	
	def getElementAt(index:Int):RectangularTilesheet = tilesheets(index)
	def getSize:Int = tilesheets.size
}

/**
 * A Listrenderer that shows a tilesheet name
 * @author Raymond Dodge
 */
object TilesheetListRenderer extends ListCellRenderer[RectangularTilesheet]
{
	private val back:ListCellRenderer[java.lang.Object] = new DefaultListCellRenderer()
	
	def getListCellRendererComponent(list:JList[_ <: RectangularTilesheet], value:RectangularTilesheet, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = back.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus)
		returnValue.asInstanceOf[javax.swing.JLabel].setText(value.name)
		returnValue
	}
}
