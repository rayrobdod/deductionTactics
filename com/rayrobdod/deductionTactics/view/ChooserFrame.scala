package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics._

import javax.swing.{JFrame, JLabel, JPanel, JList, JDialog}
import java.awt.event.{MouseAdapter, MouseEvent}
import java.lang.Integer
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.{ListModel, ListCellRenderer, DefaultListCellRenderer, AbstractListModel}

import com.rayrobdod.deductionTactics.test.RangeListModel
import com.rayrobdod.deductionTactics.test.ScalaSeqListModel
import com.rayrobdod.swing.NameAndIconCellRenderer
import com.rayrobdod.swing.NameAndIcon


/** 
 * @author Raymond Dodge
 * @version 06 Feb 2012
 * @version 07 Jun 2012 - changing to use Option instead of Some
 * @version 07 Jun 2012 - adding support for null values in the list
 */
class NameAndIconSetterChooserFrameMaker[A <: NameAndIcon](
			listOfItems:Seq[A],
			setter:Function1[Option[A],Any],
			panel:JPanel
	) extends ChooserFrameMaker[A](
			new ScalaSeqListModel[A](listOfItems),
			new NameAndIconCellRenderer() {
				override def getListCellRendererComponent(list:JList[_ <: NameAndIcon], value:NameAndIcon,
						index:Int, isSelected:Boolean, cellHasFocus:Boolean):java.awt.Component =
				{
					super.getListCellRendererComponent(list,
							if (value == null) {UnsetNameAndIcon} else {value},
							index, isSelected, cellHasFocus)
				}
			},
			new ListSelectionListener() {
				override def valueChanged(e:ListSelectionEvent)
				{
					setter(Option(listOfItems(e.getFirstIndex)))
					panel.repaint()
				}
			}
	)

object UnsetNameAndIcon extends NameAndIcon
{
	val name = "Unset"
	val icon = TokenClassPanel.unknownIcon
}

/** 
 * @author Raymond Dodge
 * @version 06 Feb 2012
 * @version 07 Jun 2012 - changing to use Option instead of Some
 * @version 07 Jun 2012 - adding support for null values in the list
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
 * @version 01 Feb 2012
 * @version 06 Feb 2012 - moved from com.rayrobdod.deductionTactics.test
			 to com.rayrobdod.deductionTactics.view
 * @version 27 Apr 2012 - making created Dialog have a setLocationRelativeTo the source of the MouseEvent
 */
class ChooserFrameMaker[A](model:ListModel[A],
			renderer:ListCellRenderer[_ >: A],
			result:ListSelectionListener) extends MouseAdapter {
	override def mouseClicked(e:MouseEvent) {
		val returnValue = new ChooserFrame[A](model, renderer, result)
		returnValue.setLocationRelativeTo(e.getComponent)
		returnValue
	}
}

/** 
 * @author Raymond Dodge
 * @version 01 Feb 2012
 * @version 06 Feb 2012 - moved from com.rayrobdod.deductionTactics.test
			 to com.rayrobdod.deductionTactics.view
 * @version 12 Feb 2012 - now extends JDialog instead of JFrame
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
