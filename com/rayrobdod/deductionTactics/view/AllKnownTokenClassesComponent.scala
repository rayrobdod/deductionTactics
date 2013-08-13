package com.rayrobdod.deductionTactics.view

import javax.swing.{JList, JButton, JPanel, JFrame, JScrollPane, BoxLayout}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.{TokenClass, CannonicalTokenClass}
import scala.collection.immutable.Seq

/**
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 23 Aug 2011 - renamed from AllKnownTokenClassesFrame to AllKnownTokenClassesComponent,
 			and made extends JPanel instead of JFrame
 * @version 23 Aug 2011 - JScrollPane no longer included
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 */
class AllKnownTokenClassesComponent extends JPanel
{
	val tokenClassPanels:Seq[TokenClassPanel] = CannonicalTokenClass.allKnown.map{new TokenClassPanel(_)}
	
	this.setLayout(new BoxLayout(this, boxYAxis))
	tokenClassPanels.foreach{this.add(_)}
}
