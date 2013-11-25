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

import com.rayrobdod.boardGame.swingView.{RectangularTilesheet => Tilesheet}
import java.awt.{Window, Dialog}
import javax.swing.{JDialog, JList, JLabel, JTextField}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import java.awt.event.{ActionListener, ActionEvent}

/**
 * An interface for setting options
 * @author Raymond Dodge
 * @version a.5.0
 */
class OptionsDialog(
		owner:Window,
		modality:Dialog.ModalityType
				= Dialog.ModalityType.APPLICATION_MODAL)
		extends JDialog(owner, "Options", modality)
{
	val currentTilesheet = new JList[Tilesheet](AvailibleTilesheetListModel)
	currentTilesheet.addListSelectionListener(new ListSelectionListener() {
		override def valueChanged(e:ListSelectionEvent)
		{
			BoardGamePanel.currentTilesheet = currentTilesheet.getSelectedValue()
		}
	})
	currentTilesheet.setCellRenderer(TilesheetListRenderer)
	currentTilesheet.setSelectedValue(BoardGamePanel.currentTilesheet, true)
	
	val movementSpeed = new JTextField()
	movementSpeed.setText(BoardGamePanel.movementSpeed.toString)
	movementSpeed.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent)
		{
			BoardGamePanel.movementSpeed = Integer.parseInt(movementSpeed.getText)
		}
	})
	
	this.getContentPane.setLayout(new java.awt.FlowLayout())
	this.getContentPane.add(new JLabel("the tilesheet to use"))
	this.getContentPane.add(currentTilesheet)
	this.getContentPane.add(new JLabel("Tokens' movement speed (lower is faster)"))
	this.getContentPane.add(movementSpeed)
	
	this.pack()
	this.setLocationRelativeTo(owner)
}
