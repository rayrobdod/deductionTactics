package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.boardGame.swingView.{RectangularTilesheet, RectangularTilesheetLoader}
import javax.swing.{AbstractListModel, DefaultListCellRenderer,
		JList, ListCellRenderer, JLabel}

/**
 * A ListModel of all tilesheets.
 * Would thought this could have a more immediate use
 * @author Raymond Dodge
 * @version 15 Apr 2012
 * @version 27 Apr 2012 - improving performance by caching the set of tilesheets
 * @version 28 Oct 2012 - changing imports from com.rayrobdod.boardGame.view to com.rayrobdod.boardGame.swingView
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
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
 * @version 15 Apr 2012
 * @version 28 Oct 2012 - changing imports from com.rayrobdod.boardGame.view to com.rayrobdod.boardGame.swingView
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
