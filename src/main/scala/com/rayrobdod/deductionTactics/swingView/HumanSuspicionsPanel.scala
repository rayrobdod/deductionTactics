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

import com.rayrobdod.deductionTactics._

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.BodyType
import com.rayrobdod.deductionTactics.Directions.Direction

import scala.collection.immutable.Seq
import javax.swing.{JPanel, JLabel, Icon, JProgressBar,
		DefaultBoundedRangeModel, JComboBox, JSpinner,
		SpinnerNumberModel, SpinnerListModel
}
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout}
import com.rayrobdod.deductionTactics.{TokenClass, Weaponkinds}
import com.rayrobdod.swing.{GridBagConstraintsFactory, ScalaSeqListModel, AbstractComboBoxModel}
import javax.swing.event.{ChangeListener, ChangeEvent}

/** 
 * @author Raymond Dodge
 * @version a.5.2
 */
class HumanSuspicionsPanel(tokenClass:SuspicionsTokenClass) extends JPanel
{
	this.setLayout(new GridBagLayout)
	
	val icon = new JLabel(generateGenericIcon(tokenClass))
	val name = new JLabel("???")
	val range = new JSpinner(new SpinnerNumberModel(0,0,5,1))
	val speed = new JSpinner(new SpinnerNumberModel(0,0,5,1))
	val atkElement = new JComboBox(new ScalaSeqListModel[Option[Element]](None +: Elements.values.map{Option(_)}) with AbstractComboBoxModel[Option[Element]])
	val atkWeapon = new JComboBox(new ScalaSeqListModel[Option[Weaponkind]](None +: Weaponkinds.values.map{Option(_)}) with AbstractComboBoxModel[Option[Weaponkind]])
	val atkStatus = new JComboBox(new ScalaSeqListModel[Option[Status]](None +: Statuses.values.map{Option(_)}) with AbstractComboBoxModel[Option[Status]])
	val weakStatus = new JComboBox(new ScalaSeqListModel[Option[Status]](None +: Statuses.values.map{Option(_)}) with AbstractComboBoxModel[Option[Status]])
	val weakWeapon = new JLabel
	val weakDirection = new JComboBox(new ScalaSeqListModel[Option[Direction]](None +: Directions.values.map{Option(_)}) with AbstractComboBoxModel[Option[Direction]])
	
	atkWeapon.setRenderer(myCellRenderer)
	atkElement.setRenderer(myCellRenderer)
	atkStatus.setRenderer(myCellRenderer)
	weakStatus.setRenderer(myCellRenderer)
	weakDirection.setRenderer(myCellRenderer)
	range.addChangeListener(updateTokenClass)
	speed.addChangeListener(updateTokenClass)
	atkElement.addActionListener(updateTokenClass)
	atkWeapon.addActionListener(updateTokenClass)
	atkStatus.addActionListener(updateTokenClass)
	weakStatus.addActionListener(updateTokenClass)
	weakDirection.addActionListener(updateTokenClass)
	atkElement.getModel.setSelectedItem(None)
	atkWeapon.getModel.setSelectedItem(None)
	atkStatus.getModel.setSelectedItem(None)
	weakStatus.getModel.setSelectedItem(None)
	weakDirection.getModel.setSelectedItem(None)
	
	
	val InnerGridBagConstraints = GridBagConstraintsFactory(weightx = 1d)
	val RemainderGridBagConstraints = GridBagConstraintsFactory(weightx = 1d, gridwidth = GridBagConstraints.REMAINDER)
	
	add(icon, GridBagConstraintsFactory(gridheight = 2))
	add(name, RemainderGridBagConstraints)
	add({
		val retVal = new JPanel
		retVal.add(new JLabel("Range: "))
		retVal.add(range)
		retVal.add(new JLabel("Speed: "))
		retVal.add(speed)
		retVal
	}, RemainderGridBagConstraints)
	add(new JLabel("Atk:"), new GridBagConstraints)
	add(atkElement, InnerGridBagConstraints)
	add(atkWeapon, InnerGridBagConstraints)
	add(atkStatus, RemainderGridBagConstraints)
	add(new JLabel("Weak:"), new GridBagConstraints)
	add(weakDirection, InnerGridBagConstraints)
	add(weakWeapon, InnerGridBagConstraints)
	add(weakStatus, RemainderGridBagConstraints)
	// add(weaponWeakPanel, RemainderGridBagConstraints)
	
	
	
	object myCellRenderer extends javax.swing.ListCellRenderer[Object] {
		private val default = new JLabel
		private val label = new JLabel
		
		override def getListCellRendererComponent(list:javax.swing.JList[_ <: Object], value:Object,
				index:Int, isSelected:Boolean, cellHasFocus:Boolean) = {
			
			label.setIcon( makeIconFor(value, 24) )
			if (isSelected) {
				label.setBackground(list.getSelectionBackground)
				label.setForeground(list.getSelectionForeground)
			} else {
				label.setBackground(default.getForeground)
				label.setForeground(default.getForeground)
			}
			label
		}
	}
	
	object updateTokenClass extends java.awt.event.ActionListener
			with ChangeListener
	{
		override def stateChanged(e:ChangeEvent) = {
			tokenClass.range = Some(HumanSuspicionsPanel.this.range.getValue.toString.toInt)
			tokenClass.speed = Some(HumanSuspicionsPanel.this.speed.getValue.toString.toInt)
			changeListeners.foreach{_.stateChanged(new ChangeEvent(HumanSuspicionsPanel.this))}
		} 
		
		override def actionPerformed(e:java.awt.event.ActionEvent) = {
			tokenClass.atkElement = HumanSuspicionsPanel.this.atkElement.getSelectedItem.asInstanceOf[Option[Element]]
			tokenClass.atkWeapon  = HumanSuspicionsPanel.this.atkWeapon.getSelectedItem.asInstanceOf[Option[Weaponkind]]
			tokenClass.atkStatus  = HumanSuspicionsPanel.this.atkStatus.getSelectedItem.asInstanceOf[Option[Status]]
			tokenClass.weakStatus = HumanSuspicionsPanel.this.weakStatus.getSelectedItem.asInstanceOf[Option[Status]]
			tokenClass.weakDirection = HumanSuspicionsPanel.this.weakDirection.getSelectedItem.asInstanceOf[Option[Direction]]
			
			HumanSuspicionsPanel.this.icon.setIcon( generateGenericIcon(tokenClass) )
			changeListeners.foreach{_.stateChanged(new ChangeEvent(HumanSuspicionsPanel.this))}
		}
	}
	
	private var changeListeners = Seq.empty[ChangeListener]
	def addChangeListener(x:ChangeListener)    { changeListeners = changeListeners :+ x }
	def removeChangeListener(x:ChangeListener) { changeListeners = changeListeners diff Seq(x) }
}
