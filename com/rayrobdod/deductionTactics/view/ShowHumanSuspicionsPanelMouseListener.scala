package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics.{Token, MirrorToken}
import javax.swing.{JDialog, JList}
import java.awt.event.{MouseAdapter, MouseEvent}

/**
 * Since elements in a JList do not support mouseactions directly,
 * this will create a component outside the JList to take the mouseactions instead
 * 
 * @author Raymond Dodge
 * @version 11 Feb 2012
 */
class ShowHumanSuspicionsPanelMouseListener(list:JList[Token]) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent) {
		if (e.getClickCount() == 2) {
			val index = list.locationToIndex(e.getPoint());
			val token = list.getModel.getElementAt(index)
			
			token match {
				case x:MirrorToken => {
			
					val panel = new HumanSuspicionsPanel(x.tokenClass)
					val frame:JDialog = new JDialog() {
						getContentPane add panel
						setTitle("")
						pack()
						setVisible(true)
					}
				}
				case _ => {}
			}
		}
	}
}
