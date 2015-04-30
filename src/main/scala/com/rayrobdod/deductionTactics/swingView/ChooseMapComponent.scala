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

import scala.collection.immutable.Seq
import javax.swing.{JPanel, JList, JScrollPane, JLabel, ListSelectionModel}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.BoxLayout.{X_AXIS => boxXAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_AS_NEEDED => scrollHorizontalAsNeeded}
import com.rayrobdod.deductionTactics.Maps
import java.awt.{GridBagConstraints, GridBagLayout}
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import java.lang.Integer

/**
 * A component that displays and lets one select map-related options
 * 
 * @version a.4.1
 * @deprecated host.Top serves this purpose now
 */
class ChooseMapComponent extends JPanel
{
	val mapList = new JList(new ScalaSeqListModel(Maps.names))
	mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
	mapList.setSelectedIndex(0)
	
	val numPlayersList = new JList[Integer]()
	numPlayersList.setLayoutOrientation(JList.VERTICAL_WRAP)
	numPlayersList.setVisibleRowCount(1)
	NumPlayersListRepopulator.valueChanged(null)
	
	
	mapList.addListSelectionListener(NumPlayersListRepopulator)
	object NumPlayersListRepopulator extends ListSelectionListener {
		def valueChanged(e:ListSelectionEvent) = {
			val index = numPlayersList.getSelectedValue
			
			if (mapList.getSelectedIndex >= 0) {
				numPlayersList.setListData( Maps.possiblePlayers(
						mapList.getSelectedIndex).map{new Integer(_)}.toArray )
				numPlayersList.setSelectedValue(index, true)
				if (numPlayersList.getSelectedIndices.length == 0)
						numPlayersList.setSelectedIndex(0)
			}
		}
	}
	
	
	this.setLayout(new GridBagLayout)
	this.add(new JLabel("Choose Map:"), new GridBagConstraints())
	this.add(
			new JScrollPane(mapList, scrollVerticalAsNeeded, scrollHorizontalAsNeeded),
			GridBagConstraintsFactory( gridwidth = GridBagConstraints.REMAINDER, fill = GridBagConstraints.BOTH, weightx = 1d, weighty = 1d )
	)
	
	this.add(new JLabel("Player Count:"), new GridBagConstraints())
	this.add(numPlayersList,
			GridBagConstraintsFactory( gridwidth = GridBagConstraints.REMAINDER )
	)
}
