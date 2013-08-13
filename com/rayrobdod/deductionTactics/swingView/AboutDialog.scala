package com.rayrobdod.deductionTactics.swingView

import javax.swing.{JDialog, JPanel, JLabel, ImageIcon}
import javax.imageio.ImageIO
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout,
			Window, Dialog}
import com.rayrobdod.swing.GridBagConstraintsFactory

/**
 * An about dialog
 * @author Raymond Dodge
 * @version 06 Feb 2012
 * @version 27 Apr 2012 - replacing a call to setLocation to an equivalent
 		setLocationRelativeTo call
 * @version 30 May 2012 - turning a bunch of variables into an array of arrays (credits)
 * @version 03 Jun 2012 - Moving version to the deductionTactics package
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 13 Dec 2012 - making all internal variables private; changing some variables to includes
 * @version 29 Jan 2013 - Using the new class: com.rayrobdod.swing.GridBagConstraintsFactory
 * @version 29 Jan 2013 - Using a border for padding instead of playing with setDimension and getDimension
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
