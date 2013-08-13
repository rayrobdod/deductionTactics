package com.rayrobdod.deductionTactics.view

import javax.swing.{JDialog, JPanel, JLabel, ImageIcon}
import javax.imageio.ImageIO
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout,
			Window, Dialog}

/**
 * An about dialog
 * @author Raymond Dodge
 * @version 06 Feb 2012
 * @version 27 Apr 2012 - replacing a call to setLocation to an equivalent
 		setLocationRelativeTo call
 */
class AboutDialog(
		owner:Window,
		modality:Dialog.ModalityType
				= Dialog.ModalityType.APPLICATION_MODAL)
		extends JDialog(owner, "About", modality)
{
	val icon = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/sprites/generic/Gray shirt.png")))
	val appName = "Deduction Tactics"
	val version = "0.4.1"
	val runningOn = "Using Java v" + System.getProperty("java.version")
	
	val credits = Array(
			/* reason, name, url */
		Array("Programmed By:", "Raymond Dodge", "http://rayrobdod.name/"),
		Array("Inspired By:", "Sean 'Squidi' Howard", "http://www.squidi.net/")
	)
	
	val RemainderGridBagConstraints = new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER}
	val Width2GridBagConstraints = new GridBagConstraints() {gridwidth = 2}
	
	rootPane.setLayout(new GridBagLayout)
	rootPane.add(new JLabel(icon), new GridBagConstraints{gridheight = 2; gridx = 0; gridy = 0})
	rootPane.add(new JLabel(appName), RemainderGridBagConstraints)
	rootPane.add(new JLabel(version), RemainderGridBagConstraints)
	rootPane.add(new JLabel(runningOn), RemainderGridBagConstraints)
	
	credits.foreach{(line:Array[String]) =>
		line.init.foreach{(x:String) =>
			rootPane.add(new JLabel(x), Width2GridBagConstraints)
		}
		rootPane.add(new JLabel(line.last), RemainderGridBagConstraints)
	}
	
	pack()
	setSize(getWidth+10, getHeight+10)
	setResizable(false)
	
	val gc = getGraphicsConfiguration
	val gcbounds = gc.getBounds()
	//setLocation(((gcbounds.getWidth - this.getWidth) / 2).intValue, ((gcbounds.getHeight - this.getHeight) / 2).intValue)
	setLocationRelativeTo(null);
}
