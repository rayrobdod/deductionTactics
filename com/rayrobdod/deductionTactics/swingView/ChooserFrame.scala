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
 * @version 06 Feb 2012
 * @version 07 Jun 2012 - changing to use Option instead of Some
 * @version 07 Jun 2012 - adding support for null values in the list
 * @version 15 Jun 2012 - replacing anonymous inner class with a 
		decorator: [[com.rayrobdod.swing.NullReplaceListCellRenderer]]
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
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
 * @version 11 Jun 2012
 * @version 15 Jun 2012 - adding null support
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
 * @version 11 Jun 2012
 * @version 15 Jun 2012 - adding null support
 */
object WeaponMultiplerSetterChooserFrameMaker
{
	val values = Seq[JavaFloat](null, 0.5f, 0.75f, 1f, 1.5f, 2f)
}

/** 
 * @author Raymond Dodge
 * @version 11 Jun 2012
 * @version 12 Jun 2012 - no longer extends ChooserFrameMaker; creates a new Model
			each mouse click rather than using the same model each time
 * @version 12 Jun 2012 - now sets Name and Icon as well as the other things it used to set
 */
class ClassSynchonizerFrameMaker(
			tokenClass:SuspicionsTokenClass,
			panel:JPanel
	) extends MouseAdapter
{
	override def mouseClicked(e:MouseEvent) {
		val returnValue = new ChooserFrame[TokenClass](model, TokenClassListRenderer, result)
		returnValue.setLocationRelativeTo(e.getComponent)
		returnValue
	}
	
	def model = new ScalaSeqListModel[TokenClass]( CannonicalTokenClass.allKnown.filter(
					new TokenClassMatcher(tokenClass)) )
	
	def result = new ListSelectionListener() {
		override def valueChanged(e:ListSelectionEvent)
		{
			val selected = CannonicalTokenClass.allKnown.filter(
					new TokenClassMatcher(tokenClass)).apply(e.getFirstIndex)
			
			tokenClass.name = selected.name
			tokenClass.icon = Option(selected.icon)
			
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
