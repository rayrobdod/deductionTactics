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

import javax.swing.{JList, JButton, JPanel, JFrame, JScrollPane, BoxLayout, JComponent}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.{TokenClass, CannonicalTokenClass}
import scala.collection.immutable.Seq

/**
 * @author Raymond Dodge
 */
class AllKnownTokenClassesComponent extends JPanel
{
	private var _tokenClassToComponent:Function1[TokenClass,JComponent] = {(x:TokenClass) => new TokenClassPanel(x)}
	def tokenClassToComponent = _tokenClassToComponent
	def tokenClassToComponent_=(x:Function1[TokenClass,JComponent]) = {
		_tokenClassToComponent = x
		
		this.removeAll()
		tokenClassPanels.foreach{this.add(_)}
		this.revalidate()
	}
	
	private def tokenClassPanels:Seq[JComponent] = CannonicalTokenClass.allKnown.map(tokenClassToComponent)
	
	// TODO: Create a new LayoutManager that is basically a flow-layout
	// but has a preferred width the same as the component's width and
	// a corresponding height
	
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
				
				val cols = math.max(thisSize.width / childSize.width * 3/4, 1)
				val rows = c.getComponentCount / cols + 1
				
				new Dimension(cols * (childSize.width + getVgap),
						rows * (childSize.height + getHgap))
			}
		}
	})
	tokenClassPanels.foreach{this.add(_)}
}
