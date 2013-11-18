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

import javax.swing.{JFrame, JLabel, JPanel, JList, JDialog}
import java.awt.event.{MouseAdapter, MouseEvent}
import java.lang.Integer
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.{ListModel, ListCellRenderer, DefaultListCellRenderer, AbstractListModel, BoundedRangeModel}

import com.rayrobdod.swing.RangeListModel
import com.rayrobdod.swing.ScalaSeqListModel
import com.rayrobdod.swing.{NameAndIconCellRenderer, NameAndIcon, NullReplaceListCellRenderer, MapToNameAndIconCellRenderer}

import java.lang.{Float => JavaFloat}


/** 
 * @author Raymond Dodge
 */
class NameAndIconSetterChooserFrameMaker[A](
			listOfItems:Seq[A],
			setter:Function1[Option[A],Any],
			panel:JPanel )(
			implicit converter:(A) => NameAndIcon
) extends ChooserFrameMaker[A](
			new ScalaSeqListModel[A](listOfItems),
			new MapToNameAndIconCellRenderer[A]()(converter),
			new ListSelectionListener() {
				override def valueChanged(e:ListSelectionEvent)
				{
					setter(Option(listOfItems(e.getFirstIndex)))
					panel.repaint()
				}
			}
	)

/** 
 * @author Raymond Dodge
 */
class IntSetterChooserFrameMaker(
			setter:Function1[Option[Int],Any],
			panel:JPanel
	) extends ChooserFrameMaker[Integer](
			new RangeListModel(6),
			new DefaultListCellRenderer(),
			new ListSelectionListener() {
				override def valueChanged(e:ListSelectionEvent)
				{
					setter(Option(e.getFirstIndex))
					panel.repaint()
				}
			}
	)

/** 
 * @author Raymond Dodge
 * @pre weaknessModel.getMin <= 50
 * @pre weaknessModel.getMin >= 200
 */
class WeaponMultiplerSetterChooserFrameMaker(
			setter:Function1[Option[Float],Any],
			panel:JPanel
	) extends ChooserFrameMaker[JavaFloat](
			new ScalaSeqListModel[JavaFloat](WeaponMultiplerSetterChooserFrameMaker.values),
			new NullReplaceListCellRenderer[Object](new DefaultListCellRenderer, "unset"),
			new ListSelectionListener() {
				override def valueChanged(e:ListSelectionEvent)
				{
					setter(Option(WeaponMultiplerSetterChooserFrameMaker.values(e.getFirstIndex)).map{Float2float})
					panel.repaint()
				}
			}
	)

/** 
 * @author Raymond Dodge
 */
object WeaponMultiplerSetterChooserFrameMaker
{
	val values = Seq[JavaFloat](null, 0.5f, 0.75f, 1f, 1.5f, 2f)
}

/** 
 * @author Raymond Dodge
 */
class ClassSynchonizerFrameMaker(
			tokenClass:SuspicionsTokenClass,
			panel:JPanel
	) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent):Unit = {
		val returnValue = new ChooserFrame[TokenClass](model, FullTokenClassListRenderer, result)
		returnValue.setLocationRelativeTo(e.getComponent)
	}
	
	def model = new ScalaSeqListModel[TokenClass]( CannonicalTokenClass.allKnown.filter(
					new TokenClassMatcher(tokenClass)) )
	
	def result = new ListSelectionListener() {
		override def valueChanged(e:ListSelectionEvent)
		{
			val selected = CannonicalTokenClass.allKnown.filter(
					new TokenClassMatcher(tokenClass)).apply(e.getFirstIndex)
			
			tokenClass.name = selected.name
			
			tokenClass.body = selected.body
			tokenClass.atkElement = selected.atkElement
			tokenClass.atkWeapon = selected.atkWeapon
			tokenClass.atkStatus = selected.atkStatus
			tokenClass.weakStatus = selected.weakStatus
			tokenClass.weakDirection = selected.weakDirection
			tokenClass.speed = selected.speed
			tokenClass.range = selected.range
			tokenClass.weakWeapon = selected.weakWeapon
		}
	}
}

/** 
 * @author Raymond Dodge
 */
class ChooserFrameMaker[A](model:ListModel[A],
			renderer:ListCellRenderer[_ >: A],
			result:ListSelectionListener) extends MouseAdapter {
	override def mouseClicked(e:MouseEvent):Unit = {
		val returnValue = new ChooserFrame[A](model, renderer, result)
		returnValue.setLocationRelativeTo(e.getComponent)
	}
}

/** 
 * @author Raymond Dodge
 */
class ChooserFrame[A](model:ListModel[A],
			renderer:ListCellRenderer[_ >: A],
			result:ListSelectionListener) extends JDialog
{
	val possibilitiesList:JList[A] = new JList[A](model)
	possibilitiesList.setCellRenderer(renderer)
	
	ChooserFrame.this.add(possibilitiesList)
	possibilitiesList.addListSelectionListener(result)
	possibilitiesList.addListSelectionListener(new ListSelectionListener() {
		override def valueChanged(e:ListSelectionEvent)
		{
			ChooserFrame.this.setVisible(false)
		}
	})
	
	pack()
	setVisible(true)
}
