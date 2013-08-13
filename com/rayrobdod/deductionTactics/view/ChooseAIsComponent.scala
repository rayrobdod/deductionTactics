package com.rayrobdod.deductionTactics.view

import scala.collection.immutable.Seq
import javax.swing.{JPanel, BoxLayout, JList, AbstractListModel, JScrollPane}
import javax.swing.BoxLayout.{X_AXIS => boxXAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.PlayerAI

/**
 * @author Raymond Dodge
 * @version 23 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 */
class ChooseAIsComponent extends JPanel
{
	val players:Int = 2
	
	val aiLists:Seq[JList[PlayerAI]] = Seq.fill(players){new JList[PlayerAI](AvailibleAIListModel)}
	
	this.setLayout(new BoxLayout(this, boxXAxis))
	aiLists.foreach{(list:JList[PlayerAI]) => this.add(new JScrollPane(list, scrollVerticalAsNeeded, scrollHorizontalNever))}
	aiLists.foreach{_.setSelectedIndex(1)}
	
	def getAIs:Seq[PlayerAI] = {
		aiLists.map{_.getSelectedValue.asInstanceOf[PlayerAI]}
	}
}

object AvailibleAIListModel extends AbstractListModel[PlayerAI]
{
	def getElementAt(index:Int):PlayerAI = PlayerAI.serviceSeq(index)
	def getSize:Int = PlayerAI.serviceSeq.size
}
