package com.rayrobdod.deductionTactics.view

import com.rayrobdod.boardGame.view.{Tilesheet, TilesheetLoader}
import javax.swing.{AbstractListModel, DefaultListCellRenderer,
		JList, ListCellRenderer, JLabel}

/**
 * A ListModel of all tilesheets.
 * Would thought this could have a more immediate use
 * @author Raymond Dodge
 * @version 15 Apr 2012
 * @version 27 Apr 2012 - improving performance by caching the set of tilesheets
 */
object AvailibleTilesheetListModel extends AbstractListModel[Tilesheet]
{
	val tilesheets = new TilesheetLoader("com.rayrobdod.deductionTactics.view.tilesheet").toSeq
	
	def getElementAt(index:Int):Tilesheet = tilesheets(index)
	def getSize:Int = tilesheets.size
}

/**
 * A Listrenderer that shows a tilesheet name
 * @author Raymond Dodge
 * @version 15 Apr 2012
 */
object TilesheetListRenderer extends ListCellRenderer[Tilesheet]
{
	private val back:ListCellRenderer[java.lang.Object] = new DefaultListCellRenderer()
	
	def getListCellRendererComponent(list:JList[_ <: Tilesheet], value:Tilesheet, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = back.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus)
		returnValue.asInstanceOf[javax.swing.JLabel].setText(value.name)
		returnValue
	}
}
