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

import javax.swing.{JList, JScrollPane, ListSelectionModel}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_AS_NEEDED => scrollHorizontalAsNeeded}
import com.rayrobdod.deductionTactics.PlayerAI
import java.awt.Component
import com.rayrobdod.swing.ScalaSeqListModel

/**
 * @author Raymond Dodge
 * @since a.6.0
 */
final class AiChoosingPanels
{
	private val baseList = new JList(new ScalaSeqListModel(PlayerAI.basePlayerAIs))
	private val decoratorList = new JList(new ScalaSeqListModel(PlayerAI.decoratorPlayerAIs))
	
	val baseComponent:Component = {
		baseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		baseList.setSelectedIndex(0)
		baseList.setVisibleRowCount(math.min(6, PlayerAI.basePlayerAIs.size))
		
		new JScrollPane(baseList, scrollVerticalAsNeeded, scrollHorizontalAsNeeded)
	}
	val decoratorComponent:Component = {
		decoratorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
		decoratorList.setVisibleRowCount(math.min(6, PlayerAI.decoratorPlayerAIs.size))
		
		new JScrollPane(decoratorList, scrollVerticalAsNeeded, scrollHorizontalAsNeeded)
	}
	
	
	def createAi:PlayerAI = {
		import scala.collection.JavaConversions.iterableAsScalaIterable
		
		val base:PlayerAI = baseList.getSelectedValue
		val decs:Iterable[PlayerAI => PlayerAI] = decoratorList.getSelectedValuesList
		
		decs.foldLeft(base){(base:PlayerAI, dec:PlayerAI => PlayerAI) => dec(base)}
	}
}
