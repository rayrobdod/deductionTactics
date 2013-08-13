package com.rayrobdod.deductionTactics.swingView

import scala.collection.immutable.Seq
import javax.swing.{JPanel, BoxLayout, JList, AbstractListModel, JScrollPane, ListSelectionModel}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.BoxLayout.{X_AXIS => boxXAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.Maps
import java.awt.GridLayout
import com.rayrobdod.swing.ScalaSeqListModel
import java.lang.Integer

/**
 * A component that displays and lets one selecte between map-related options
 * 
 * @version 28 Nov 2012
 */
class ChooseMapComponent extends JPanel
{
	val mapList = new JList(new ScalaSeqListModel(Maps.names))
	mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
	mapList.setSelectedIndex(0)
	
	val numPlayersList = new JList[Integer]()
	NumPlayersListRepopulator.valueChanged(null)
	
	
	mapList.addListSelectionListener(NumPlayersListRepopulator)
	object NumPlayersListRepopulator extends ListSelectionListener {
		def valueChanged(e:ListSelectionEvent) = {
			val index = numPlayersList.getSelectedValue
			
			numPlayersList.setListData( Maps.possiblePlayers(mapList.getSelectedIndex).map{new Integer(_)}.toArray )
			numPlayersList.setSelectedValue(index, true)
			if (numPlayersList.getSelectedIndices.length == 0)
				numPlayersList.setSelectedIndex(0)
		}
	}
	
	
	this.add(new JScrollPane(mapList, scrollVerticalAsNeeded, scrollHorizontalNever))
	this.add(numPlayersList)
}
