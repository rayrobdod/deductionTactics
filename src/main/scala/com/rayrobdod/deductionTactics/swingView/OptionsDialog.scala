package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.Options
import com.rayrobdod.boardGame.swingView.{RectangularTilesheet => Tilesheet}
import java.awt.{Window, Dialog}
import javax.swing.{JDialog, JList, JLabel, JTextField}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import java.awt.event.{ActionListener, ActionEvent}

/**
 * An interface for setting options
 * @author Raymond Dodge
 * @version 29 May 2012 - only contains currentTilesheet
 * @version 24 Jul 2012 - adding movementSpeed
 * @version 28 Oct 2012 - changing imports from com.rayrobdod.boardGame.view to com.rayrobdod.boardGame.swingView
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
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
	
	val movementSpeed = new JTextField()
	movementSpeed.setText(Options.movementSpeed.toString)
	movementSpeed.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent)
		{
			Options.movementSpeed = Integer.parseInt(movementSpeed.getText)
		}
	})
	
	this.getContentPane.setLayout(new java.awt.FlowLayout())
	this.getContentPane.add(new JLabel("TODO: make objects persistent"))
	this.getContentPane.add(new JLabel("the tilesheet to use"))
	this.getContentPane.add(currentTilesheet)
	this.getContentPane.add(new JLabel("Tokens' movement speed (lower is faster)"))
	this.getContentPane.add(movementSpeed)
	
	this.pack()
	this.setLocationRelativeTo(owner)
}
