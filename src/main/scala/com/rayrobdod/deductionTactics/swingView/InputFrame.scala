package com.rayrobdod.deductionTactics.swingView

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
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
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
