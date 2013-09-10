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
