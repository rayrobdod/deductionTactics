package com.rayrobdod.deductionTactics.view

import java.awt.{BorderLayout, Component}
import java.awt.event.ActionListener
import javax.swing.{JFrame, JButton, JPanel}

/**
 * A frame with an OK button and an other component.
 *
 * Put here for reusablity and reducing anonimous inner functions
 * 
 * @author Raymond Dodge
 * @version 03 Aug 2012
 */
class InputFrame(
			title:String,
			main:Component,
			okActionListener:ActionListener
) extends JFrame(title)
{
	add(main)
	
	val okButton = new JButton("OK")
	okButton.addActionListener(okActionListener)
	private val okPanel = new JPanel
	okPanel.add(okButton)
	add(okPanel, BorderLayout.SOUTH)
	
	pack()
	getRootPane.setDefaultButton(okButton)
}
