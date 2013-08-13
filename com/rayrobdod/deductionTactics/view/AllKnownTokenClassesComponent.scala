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
 * @version 03 Jun 2012 - Allowing layout to have multiple columns when wide enough to do so.
 */
class AllKnownTokenClassesComponent extends JPanel
{
	val tokenClassPanels:Seq[TokenClassPanel] = CannonicalTokenClass.allKnown.map{new TokenClassPanel(_)}
	
	// TODO: Create a new LayoutManager that is basically a flow-layout
	// but has a preferred width the same as the component's width and
	// a coresponding height
	
	import java.awt.{FlowLayout, Container, Dimension}
	
//	this.setLayout(new BoxLayout(this, boxYAxis))
	this.setLayout(new FlowLayout(){
		override def preferredLayoutSize(c:Container) = minimumLayoutSize(c)
		
		override def minimumLayoutSize(c:Container) = {
			if (c.getComponentCount == 0) {new Dimension(0,0)}
			else
			{
				val childSize = c.getComponent(0).getPreferredSize
				val thisSize = javax.swing.SwingUtilities.getWindowAncestor(c).getSize
				
				val cols = math.max(thisSize.width / childSize.width, 1)
				val rows = c.getComponentCount / cols + 1
				
				new Dimension(cols * (childSize.width + getVgap),
						rows * (childSize.height + getHgap))
			}
		}
	})
	tokenClassPanels.foreach{this.add(_)}
}
