/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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

import java.awt.event.{MouseEvent, MouseMotionListener}
import javax.swing.{ListModel, JList, JToolTip}
import com.rayrobdod.deductionTactics.TokenClass
import com.rayrobdod.deductionTactics.swingView.chooseTokenClasses.NoWeaponWeakTokenClassListRenderer

class TokenClassList(dataModel:ListModel[TokenClass]) extends JList[TokenClass](dataModel) {
	//def this() = this(new javax.swing.DefaultListModel[TokenClass]);
	
	private object HoveringIndexMouseListener extends MouseMotionListener {
		var index:Int = 0;
		
		def mouseDragged(e:MouseEvent):Unit = mouseMoved(e);
		def mouseMoved(e:MouseEvent):Unit = {
			index = locationToIndex(new java.awt.Point(e.getPoint()));
		}
	}
	
	this.addMouseMotionListener(HoveringIndexMouseListener)
	
	override def createToolTip():JToolTip = {
		val retVal = new JToolTip() {
			this.setUI(TokenClassList.MyToolTipUI)
			override def setTipText(tipText:String) {
				this.removeAll();
				val a = new TokenClassPanel(dataModel.getElementAt(HoveringIndexMouseListener.index));
				a.doLayout();
				a.setBackground(new java.awt.Color(0, true))
				this.add(a);
			}
		}
		retVal.setComponent(this);
		retVal.setLayout(new java.awt.BorderLayout())
		retVal;
	}
	
	this.setToolTipText("asdfghjkl");
	this.setCellRenderer(NoWeaponWeakTokenClassListRenderer)
	
	
}

object TokenClassList {


import java.awt._;
import javax.swing._;
import javax.swing.plaf.metal.MetalToolTipUI;

private[TokenClassList] object MyToolTipUI extends MetalToolTipUI {
	override def paint(g:Graphics, c:JComponent) {
		val size = c.getSize()
		g.setColor(c.getBackground());
		g.fillRect(0, 0, size.width, size.height);
		g.setColor(c.getForeground());
		c.getComponents.foreach{child =>
			child.setSize(size);
			child.paint(g)
		}
	}
	
	override def getPreferredSize(c:JComponent):Dimension = {
		var w:Double = 0;
		var h:Double = 0;
		
		c.getComponents.foreach{child =>
			w = math.max(w, child.getPreferredSize.getWidth());
			h = math.max(h, child.getPreferredSize.getHeight());
		}
		
		new Dimension(w.intValue + 4, h.intValue + 4);
	}
}
}
