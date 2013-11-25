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

import javax.swing.{JList, JPanel, ButtonGroup, JButton, BoxLayout,
		JScrollPane, JRadioButton, AbstractListModel, ListCellRenderer,
		DefaultListModel
}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_ALWAYS => scrollHorizontalAlways
}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis, X_AXIS => boxXAxis}
import java.awt.BorderLayout
import java.awt.event.{ActionListener, ActionEvent}
import com.rayrobdod.deductionTactics.{PlayerAI, TokenClass, CannonicalTokenClass}
import com.rayrobdod.swing.{MapToNameAndIconCellRenderer, ScalaSeqListModel}
import scala.collection.immutable.Seq
import scala.collection.JavaConversions.iterableAsScalaIterable

/**
 * @author Raymond Dodge
 * @version a.5.0
 */
class TeamBuilderPanel extends JPanel
{
	private val currentSelectionModel = new DefaultListModel[CannonicalTokenClass]()
	def currentSelection:Seq[CannonicalTokenClass] = (Seq.empty ++ currentSelectionModel.toArray).asInstanceOf[Seq[CannonicalTokenClass]]
	
	val currentSelectionList = new JList[CannonicalTokenClass](currentSelectionModel)
	currentSelectionList.setCellRenderer(FullTokenClassListRenderer)
	currentSelectionList.setBackground(null)
	currentSelectionList.setPrototypeCellValue(CannonicalTokenClass.allKnown.head)
	
	val allTokenClassesList = new JList(new ScalaSeqListModel(CannonicalTokenClass.allKnown))
	allTokenClassesList.setCellRenderer(FullTokenClassListRenderer)
	allTokenClassesList.setBackground(null)
	allTokenClassesList.setLayoutOrientation(JList.VERTICAL_WRAP)
	allTokenClassesList.setVisibleRowCount(10)
	
	val addButton = new JButton("←")
	addButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			allTokenClassesList.getSelectedValuesList.foreach{
				currentSelectionModel.addElement(_)
			}
		}
	})
	val removeButton = new JButton("→")
	removeButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			currentSelectionList.getSelectedValuesList.foreach{
				currentSelectionModel.removeElement(_)
			}
		}
	})
	val removeAllButton = new JButton("clear")
	removeAllButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			currentSelectionModel.removeAllElements()
		}
	})
	val fullStyle = new JRadioButton("Full", true)
	fullStyle.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			allTokenClassesList.setCellRenderer(FullTokenClassListRenderer)
			currentSelectionList.setCellRenderer(FullTokenClassListRenderer)
			allTokenClassesList.setVisibleRowCount(10)
		}
	})
	val noWeaponWeakStyle = new JRadioButton("Without Weapon Weakness", true)
	noWeaponWeakStyle.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			allTokenClassesList.setCellRenderer(NoWeaponWeakTokenClassListRenderer)
			currentSelectionList.setCellRenderer(NoWeaponWeakTokenClassListRenderer)
			allTokenClassesList.setVisibleRowCount(10)
		}
	})
	val nameAndIconStyle = new JRadioButton("Name and Icon", true)
	nameAndIconStyle.addActionListener(new ActionListener(){
		implicit def tokenClassToNameAndIcon(x:TokenClass) = {
			new MyNameAndIcon(x.name, tokenClassToIcon(x))
		}
		override def actionPerformed(e:ActionEvent) = {
			allTokenClassesList.setCellRenderer(new MapToNameAndIconCellRenderer[TokenClass])
			currentSelectionList.setCellRenderer(new MapToNameAndIconCellRenderer[TokenClass])
			allTokenClassesList.setVisibleRowCount(20)
		}
	})
	private val styleGroup = new ButtonGroup()
	styleGroup.add(fullStyle)
	styleGroup.add(noWeaponWeakStyle)
	styleGroup.add(nameAndIconStyle)
	
	setLayout(new BorderLayout)
	add({
		val a = new JPanel(new BorderLayout);
		a.add(new JScrollPane(currentSelectionList,
				scrollVerticalAlways, scrollHorizontalNever), BorderLayout.WEST)
		a.add({val b = new JPanel();
			b.setLayout(new BoxLayout(b, boxYAxis))
			b.add({
				val c = new JPanel();
				c.setLayout(new BoxLayout(c, boxYAxis))
				c.add(addButton)
				c.add(removeButton)
				c.add(removeAllButton)
				c;
			})
			b.add({
				val c = new JPanel();
				c.setLayout(new BoxLayout(c, boxYAxis))
				c.add(fullStyle)
				c.add(noWeaponWeakStyle)
				c.add(nameAndIconStyle)
				c;
			})
			b;
		})
		a;
	}, BorderLayout.WEST)
	add(new JScrollPane(allTokenClassesList,
			scrollVerticalAlways, scrollHorizontalAlways), BorderLayout.CENTER)
}

/**
 * @author Raymond Dodge
 * @version 23 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 * @version 06 Feb 2012 - made the thing's color the same as the list's prefered seleciton color
 * @version 11 Feb 2012 - renamed from TokenClassListRender to TokenClassListRenderer
 * @version 2013 Aug 19 - renamed from TokenClassListRenderer to FullTokenClassListRenderer
 */
object FullTokenClassListRenderer extends ListCellRenderer[TokenClass]
{
	def getListCellRendererComponent(list:JList[_ <: TokenClass], value:TokenClass, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = new TokenClassPanel(value)
		returnValue.doLayout()
		if (isSelected) {
			returnValue.setBackground(list.getSelectionBackground)
		}
		returnValue
	}
}

/**
 * @author Raymond Dodge
 * @version 2013 Aug 19 - Copeid and modified from FullTokenClassListRenderer
 */
object NoWeaponWeakTokenClassListRenderer extends ListCellRenderer[TokenClass]
{
	def getListCellRendererComponent(list:JList[_ <: TokenClass], value:TokenClass, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = new TokenClassPanel(value)
		returnValue.remove(returnValue.weaponWeakPanel)
		returnValue.doLayout()
		if (isSelected) {
			returnValue.setBackground(list.getSelectionBackground)
		}
		returnValue
	}
}
