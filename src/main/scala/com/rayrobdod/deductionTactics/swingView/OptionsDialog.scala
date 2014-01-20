/*
	Deduction Tactics
	Copyright (C) 2012-2014  Raymond Dodge

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
import java.awt.{Window, Dialog}
import javax.swing.{JPanel, JList, JLabel, JTextField}
import java.awt.event.{ActionListener, ActionEvent}
import com.rayrobdod.swing.GridBagConstraintsFactory

/**
 * An interface for setting options
 * @author Raymond Dodge
 * @version a.5.3
 */
class OptionsPanel extends JPanel
{
	val currentTilesheet = new JList[RectangularTilesheet](AvailibleTilesheetListModel)
	currentTilesheet.setCellRenderer(TilesheetListRenderer)
	currentTilesheet.setSelectedValue(BoardGamePanel.currentTilesheet, true)
	
	val movementSpeed = new JTextField(5)
	movementSpeed.setText(BoardGamePanel.movementSpeed.toString)
	
	this.setLayout(new java.awt.GridBagLayout())
	this.add(new JLabel("the tilesheet to use"),
			GridBagConstraintsFactory( gridx = 0, gridy = 0 ))
	this.add(currentTilesheet,
			GridBagConstraintsFactory( gridx = 0, gridy = 1 ))
	this.add(new JLabel("Tokens' movement speed (lower is faster)"),
			GridBagConstraintsFactory( gridx = 1, gridy = 0 ))
	this.add(movementSpeed,
			GridBagConstraintsFactory( gridx = 1, gridy = 1 ))
	
	
	object apply extends ActionListener() {
		override def actionPerformed(e:ActionEvent)
		{
			BoardGamePanel.currentTilesheet = currentTilesheet.getSelectedValue()
			BoardGamePanel.movementSpeed = Integer.parseInt(movementSpeed.getText)
		}
	}
}
