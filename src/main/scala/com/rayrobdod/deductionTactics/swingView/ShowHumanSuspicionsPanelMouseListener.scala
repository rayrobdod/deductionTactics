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

import com.rayrobdod.deductionTactics.{Token, MirrorToken}
import javax.swing.{JDialog, JList}
import java.awt.event.{MouseAdapter, MouseEvent}

/**
 * Since elements in a JList do not support mouseactions directly,
 * this will create a component outside the JList to take the mouseactions instead
 * 
 * @author Raymond Dodge
 */
class ShowHumanSuspicionsPanelMouseListener(list:JList[Token]) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent) {
		if (e.getClickCount() == 2) {
			val index = list.locationToIndex(e.getPoint());
			val token = list.getModel.getElementAt(index)
			
			token match {
				case x:MirrorToken => {
			
					val panel = new HumanSuspicionsPanel(x.tokenClass)
					val frame:JDialog = new JDialog()
					frame.getContentPane add panel
					frame.setTitle("")
					frame.pack()
					frame.setVisible(true)
				}
				case _ => {}
			}
		}
	}
}
