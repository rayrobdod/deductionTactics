package com.rayrobdod.deductionTactics.swingView

import javax.swing.{JList, JPanel, JButton, BoxLayout, JScrollPane}
import javax.swing.{AbstractListModel, ListCellRenderer, DefaultListModel}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_ALWAYS => scrollHorizontalAlways}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis, X_AXIS => boxXAxis}
import java.awt.BorderLayout
import java.awt.event.{ActionListener, ActionEvent}
import com.rayrobdod.deductionTactics.{PlayerAI, TokenClass, CannonicalTokenClass}
import scala.collection.immutable.Seq
import scala.collection.JavaConversions.iterableAsScalaIterable

/**
 * @author Raymond Dodge
 * @version 23 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 * @version 11 Feb 2012 - added cabability to multi-add and multi-remove
 * @version 27 Apr 2012 - made the allTokenClassesList have a VERTICAL_WRAP oritntation, rather than the default
 * @version 01 Jun 2012 - redoing the layout; the currentSelectionList is constant Width, as are the buttons,
			but the allTokenClassesList is variable width 
 * @version 05 Jun 2012 - setting allTokenClassesList's visibleRowCount to 10
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 19 - reducing number of anonymous inner classes
 */
class TeamBuilderPanel extends JPanel
{
	private val currentSelectionModel = new DefaultListModel[CannonicalTokenClass]()
	def currentSelection:Seq[CannonicalTokenClass] = (Seq.empty ++ currentSelectionModel.toArray).asInstanceOf[Seq[CannonicalTokenClass]]
	
	val currentSelectionList = new JList[CannonicalTokenClass](currentSelectionModel)
	currentSelectionList.setCellRenderer(TokenClassListRenderer)
	currentSelectionList.setBackground(null)
	currentSelectionList.setPrototypeCellValue(CannonicalTokenClass.allKnown.head)
	
	val allTokenClassesList = new JList(CannonicalTokenClass.allKnownListModel)
	allTokenClassesList.setCellRenderer(TokenClassListRenderer)
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
	
	setLayout(new BorderLayout)
	add({
		val a = new JPanel(new BorderLayout);
		a.add(new JScrollPane(currentSelectionList,
				scrollVerticalAlways, scrollHorizontalNever), BorderLayout.WEST)
		a.add({val b = new JPanel();
			b.add({
				val c = new JPanel();
				c.setLayout(new BoxLayout(c, boxYAxis))
				c.add(addButton)
				c.add(removeButton)
				c.add(removeAllButton)
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
 */
object TokenClassListRenderer extends ListCellRenderer[TokenClass]
{
	def getListCellRendererComponent(list:JList[_ <: TokenClass], value:TokenClass, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = new TokenClassPanel(value)
		returnValue.doLayout()
		if (isSelected)
		{
			returnValue.setBackground(list.getSelectionBackground)
//			returnValue.atkRow.setBackground(list.getSelectionBackground)
//			returnValue.weakRow.setBackground(list.getSelectionBackground)
		}
		returnValue
	}
}
