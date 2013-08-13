package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.view.AboutDialog
import javax.swing.{JDialog, JPanel, JLabel, ImageIcon}
import javax.imageio.ImageIO
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout,
			Window, Dialog}
import javax.swing.JFrame

object AboutDialogTest extends App
{
	val frame:JFrame = new JFrame()
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(50,50)
	frame.setVisible(true)
	
	val dialog:JDialog = new AboutDialog(frame, Dialog.ModalityType.MODELESS)
	dialog.setVisible(true)
}
