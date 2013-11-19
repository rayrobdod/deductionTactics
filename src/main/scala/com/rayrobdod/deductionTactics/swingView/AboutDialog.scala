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

import javax.swing.{JDialog, JPanel, JLabel, ImageIcon}
import javax.imageio.ImageIO
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout,
			Window, Dialog}
import com.rayrobdod.swing.GridBagConstraintsFactory

/**
 * An about dialog
 * @author Raymond Dodge
 * @version a.5.0
 */
class AboutDialog(
		owner:Window,
		modality:Dialog.ModalityType
				= Dialog.ModalityType.APPLICATION_MODAL)
		extends JDialog(owner, "About", modality)
{
	private val icon = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/com/rayrobdod/deductionTactics/tokenClasses/sprites/generic/Gray shirt.png")))
	import com.rayrobdod.deductionTactics.{TITLE => appName, VERSION => version}
	private val runningOn = "Running on Java; " + System.getProperty("java.vendor") + " " + System.getProperty("java.version")
	
	private val credits = Array(
			/* reason, name, url */
		Array("Programmed By:", "Raymond Dodge", "http://rayrobdod.name/"),
		Array("Inspired By:", "Sean 'Squidi' Howard", "http://www.squidi.net/"),
		
		// buffer
		Array(" "),
		
		// libraries
		Array("Scala:", "", "http://www.scala-lang.org/"),
		Array("SVG Salamander:", "Mark 'kitfox' MacKay", "http://svgsalamander.java.net/"),
		
		// resources
		Array("dark_forest tileset:", "Stephen 'Redshrike' Challener",
				"http://opengameart.org/content/32x32-and-16x16-rpg-tiles-forest-and-some-interior-tiles")
	)
	
	private val RemainderGridBagConstraints = GridBagConstraintsFactory(gridwidth = GridBagConstraints.REMAINDER)
	private val Width2GridBagConstraints = GridBagConstraintsFactory(gridwidth = 2)
	
	rootPane.setLayout(new GridBagLayout)
	rootPane.add(new JLabel(icon), GridBagConstraintsFactory(gridheight = 2, gridx = 0, gridy = 0))
	rootPane.add(new JLabel(appName), RemainderGridBagConstraints)
	rootPane.add(new JLabel(version), RemainderGridBagConstraints)
	rootPane.add(new JLabel(runningOn), RemainderGridBagConstraints)
	
	credits.foreach{(line:Array[String]) =>
		line.init.foreach{(x:String) =>
			rootPane.add(new JLabel(x), Width2GridBagConstraints)
		}
		rootPane.add(new JLabel(line.last), RemainderGridBagConstraints)
	}
	
	rootPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10,10,10,10))
	pack()
	setResizable(false)
	setLocationRelativeTo(null);
}
