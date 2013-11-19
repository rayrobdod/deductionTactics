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

import java.awt.{BorderLayout, Component}
import java.awt.event.ActionListener
import javax.swing.{JFrame, JButton, JPanel}

/**
 * A frame with an OK button and an other component.
 *
 * Put here for reusablity and reducing anonimous inner functions
 * 
 * @note javax.swing.JOptionPane.showMessageDialog(main, ...)
 * @author Raymond Dodge
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
