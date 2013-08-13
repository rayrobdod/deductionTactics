package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics.Options
import com.rayrobdod.boardGame.view.Tilesheet
import java.awt.{Window, Dialog}
import javax.swing.{JDialog, JList, JLabel}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}

/**
 * An interface for setting options
 * @author Raymond Dodge
 * @version 29 May 2012 - only contains currentTilesheet
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
			Options.currentTilesheet = currentTilesheet.getSelectedValue()
		}
	})
	currentTilesheet.setCellRenderer(TilesheetListRenderer)
	currentTilesheet.setSelectedValue(Options.currentTilesheet, true)
	
	this.getContentPane.setLayout(new java.awt.FlowLayout())
	this.getContentPane.add(new JLabel("TODO: make objects persistent"))
	this.getContentPane.add(new JLabel("the tilesheet to use"))
	this.getContentPane.add(currentTilesheet)
	
	this.pack()
	this.setLocationRelativeTo(owner)
}
