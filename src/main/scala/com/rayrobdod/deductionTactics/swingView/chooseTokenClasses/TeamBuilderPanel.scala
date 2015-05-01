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
package chooseTokenClasses

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
import com.rayrobdod.deductionTactics.{PlayerAI, TokenClass}
import com.rayrobdod.swing.{MapToNameAndIconCellRenderer, ScalaSeqListModel}
import scala.collection.immutable.Seq
import scala.collection.JavaConversions.iterableAsScalaIterable

/**
 * @author Raymond Dodge
 * @version a.6.0
 */
class TeamBuilderPanel extends JPanel
{
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val currentSelectionModel = new DefaultListModel[TokenClass]()
	def currentSelection:Seq[TokenClass] = (Seq.empty ++ currentSelectionModel.toArray).asInstanceOf[Seq[TokenClass]]
	
	val currentSelectionList = new JList[TokenClass](currentSelectionModel)
	currentSelectionList.setCellRenderer(FullTokenClassListRenderer)
	currentSelectionList.setBackground(null)
	currentSelectionList.setPrototypeCellValue(TokenClass.allKnown.head)
	
	val allTokenClassesList = new JList(new ScalaSeqListModel(TokenClass.allKnown))
	allTokenClassesList.setCellRenderer(FullTokenClassListRenderer)
	allTokenClassesList.setBackground(null)
	allTokenClassesList.setLayoutOrientation(JList.VERTICAL_WRAP)
	allTokenClassesList.setVisibleRowCount(10)
	
	val addButton = new JButton(resources.getString("addTeambuilderButton"))
	addButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			allTokenClassesList.getSelectedValuesList.foreach{
				currentSelectionModel.addElement(_)
			}
		}
	})
	val removeButton = new JButton(resources.getString("removeTeambuilderButton"))
	removeButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			currentSelectionList.getSelectedValuesList.foreach{
				currentSelectionModel.removeElement(_)
			}
		}
	})
	val removeAllButton = new JButton(resources.getString("removeAllTeambuilderButton"))
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
 * @version a.5.0
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
 * @version a.6.0
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
