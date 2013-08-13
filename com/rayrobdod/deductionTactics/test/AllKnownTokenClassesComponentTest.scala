package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.view.AllKnownTokenClassesComponent
import javax.swing.{JFrame, JScrollPane}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}

/**
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 23 Aug 2011 - renamed from AllKnownTokenClassesFrameTest
			to AllKnownTokenClassesComponentTest
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.test
			to com.rayrobdod.deductionTactics.test
 */
object AllKnownTokenClassesComponentTest extends App
{
	val frame:JFrame = new JFrame() {
		add(new JScrollPane(
			new AllKnownTokenClassesComponent(),
			scrollVerticalAsNeeded, scrollHorizontalNever))
	}
	frame.setTitle("AllKnownTokenClassesComponentTest")
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(250,600)
	frame.setVisible(true)
}
