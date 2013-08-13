package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.view.{FilterKnownTokenClassesComponent, HumanSuspicionsPanel}
import com.rayrobdod.deductionTactics.SuspicionsTokenClass
import javax.swing.{JFrame, JScrollPane}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import java.awt.BorderLayout.NORTH
import java.awt.event.{FocusAdapter, FocusEvent}

/**
 * @author Raymond Dodge
 * @version 29 Feb 2012
 */
object FilterKnownTokenClassesComponentTest extends App
{
	val frame:JFrame = new JFrame() {
		val display = new FilterKnownTokenClassesComponent()
		add(new JScrollPane(display,
			scrollVerticalAsNeeded, scrollHorizontalNever))
		
		val filterClass = new SuspicionsTokenClass
		add(new HumanSuspicionsPanel(filterClass), NORTH)
		
		addFocusListener(new FocusAdapter(){
			override def focusGained(e:FocusEvent) {
				display.filter(filterClass)
			}
		})
	}
	frame.setTitle("FilterKnownTokenClassesComponentTest")
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(250,600)
	frame.setVisible(true)
}
