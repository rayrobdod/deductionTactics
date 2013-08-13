package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics._
import com.rayrobdod.deductionTactics.view._

import javax.swing.JFrame
import javax.swing.{JLabel, JPanel, JList}
import java.awt.event.{MouseAdapter, MouseEvent}
import java.lang.Integer
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.{ListModel, ListCellRenderer, DefaultListCellRenderer, AbstractListModel}

import com.rayrobdod.swing.NameAndIconCellRenderer

/** 
 * @author Raymond Dodge
 * @version 01 Feb 2012
 * @version 06 Feb 2012 - replaced invocations of {@link ChooserFrameMaker} with 
 			invocations of either {@link IntSetterChooserFrameMaker} or 
 			{@link NameAndIconSetterChooserFrameMaker}
 * @version 11 Feb 2012 - cut vast majority of content and pasted in {@link HumanSuspicionsPanel}
 */
object HumanSuspicionTest extends App
{
	val tokenClass = new SuspicionsTokenClass
	val panel = new HumanSuspicionsPanel(tokenClass)
	
	val frame:JFrame = new JFrame() {
		getContentPane add panel
		setTitle("HumanSuspicionTest")
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		setSize(250,200)
		setVisible(true)
	}
}

/** 
 * @author Raymond Dodge
 * @version 01 Feb 2012
 * TODO: move to util
 */
class ScalaSeqListModel[A](backing:Seq[A]) extends AbstractListModel[A]
{
	def getSize = backing.size
	def getElementAt(i:Int) = backing(i)
}

/** 
 * @author Raymond Dodge
 * @version 01 Feb 2012
 * TODO: move to util
 */
class RangeListModel(override val getSize:Int) extends AbstractListModel[Integer]
{
	override def getElementAt(i:Int):Integer = i
}

