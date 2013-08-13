package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics._
import com.rayrobdod.deductionTactics.swingView._

import javax.swing.JFrame
import javax.swing.{JLabel, JPanel, JList}
import java.awt.event.{MouseAdapter, MouseEvent}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.{ListModel, ListCellRenderer, DefaultListCellRenderer, AbstractListModel}

import com.rayrobdod.swing.NameAndIconCellRenderer

/** 
 * @author Raymond Dodge
 * @version 01 Feb 2012
 * @version 06 Feb 2012 - replaced invocations of {@link ChooserFrameMaker} with 
 			invocations of either {@link IntSetterChooserFrameMaker} or 
 			{@link NameAndIconSetterChooserFrameMaker}
 * @version 11 Feb 2012 - cut vast majority of content and pasted in {@link HumanSuspicionsPanel}
 */
object HumanSuspicionTest extends App
{
	val tokenClass = new SuspicionsTokenClass
	val panel = new HumanSuspicionsPanel(tokenClass)
	
	val frame:JFrame = new JFrame() {
		getContentPane add panel
		setTitle("HumanSuspicionTest")
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		setSize(250,200)
		setVisible(true)
	}
}


