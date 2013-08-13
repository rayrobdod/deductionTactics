package com.rayrobdod.swing

import javax.swing.{JList, JLabel, ListCellRenderer, Icon}

/** 
 * @author Raymond Dodge
 * @version 02 Feb 2012
 */
trait NameAndIcon
{
	def icon:Icon
	def name:String
}

/** 
 * Uses both a value's icon and name in displaying it as a list cell.
 * 
 * @author Raymond Dodge
 * @version 02 Feb 2012
 */
class NameAndIconCellRenderer extends ListCellRenderer[NameAndIcon]
{
	def getListCellRendererComponent(list:JList[_ <: NameAndIcon], value:NameAndIcon,
			index:Int, isSelected:Boolean, cellHasFocus:Boolean):java.awt.Component =
	{
		val returnValue = new JLabel
		returnValue.setIcon(value.icon)
		returnValue.setText(value.name)
		if (isSelected)
		{
			returnValue.setBackground(list.getSelectionBackground)
			returnValue.setForeground(list.getSelectionForeground)
		}
		returnValue
	}
}
