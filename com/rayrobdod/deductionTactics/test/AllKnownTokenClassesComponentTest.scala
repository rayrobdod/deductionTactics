package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.view.{AllKnownTokenClassesComponent, TokenClassPanelTypeSelector}
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
 * @version 08 Aug 2012 - implementing tokenClassPanelTypeSelector
 * @version 14 Aug 2012 - moving inner object tokenClassPanelTypeSelector to com.rayrobdod.deductionTactics.view.TokenClassPanelTypeSelector
 */
object AllKnownTokenClassesComponentTest extends App
{
	val allTokenClassesComponent = new AllKnownTokenClassesComponent()
	
	val frame:JFrame = new JFrame()
	frame.add(new JScrollPane(allTokenClassesComponent,
			scrollVerticalAsNeeded, scrollHorizontalNever))
	frame.add(new TokenClassPanelTypeSelector(allTokenClassesComponent), java.awt.BorderLayout.SOUTH)
	frame.setTitle("AllKnownTokenClassesComponentTest")
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(250,600)
	frame.setVisible(true)
	
	import java.awt.event.{ComponentAdapter, ComponentEvent}
	frame.addComponentListener(new ComponentAdapter(){
		override def componentResized(e:ComponentEvent) {
			e.getComponent.invalidate()
		}
	})
	
}
