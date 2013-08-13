package com.rayrobdod.deductionTactics.view

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
import com.rayrobdod.deductionTactics.ai.HumanAI
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
 */
class TeamBuilderPanel extends JPanel
{
	private val currentSelectionModel = new DefaultListModel[CannonicalTokenClass]()
	def currentSelection:Seq[CannonicalTokenClass] = (Seq.empty ++ currentSelectionModel.toArray).asInstanceOf[Seq[CannonicalTokenClass]]
	
	val currentSelectionList = new JList[CannonicalTokenClass](currentSelectionModel){
		setCellRenderer(TokenClassListRenderer)
		setBackground(null)
		setPrototypeCellValue(CannonicalTokenClass.allKnown.head)
	}
	val allTokenClassesList = new JList(CannonicalTokenClass.allKnownListModel){
		setCellRenderer(TokenClassListRenderer)
		setBackground(null)
		setLayoutOrientation(JList.VERTICAL_WRAP)
	}
	
	val addButton = new JButton("←") {
		this.addActionListener(new ActionListener(){
			override def actionPerformed(e:ActionEvent) = {
				allTokenClassesList.getSelectedValuesList.foreach{
					currentSelectionModel.addElement(_)
				}
			}
		})
	}
	val removeButton = new JButton("→") {
		addActionListener(new ActionListener(){
			override def actionPerformed(e:ActionEvent) = {
				currentSelectionList.getSelectedValuesList.foreach{
					currentSelectionModel.removeElement(_)
				}
			}
		})
	}
	val removeAllButton = new JButton("clear") {
		addActionListener(new ActionListener(){
			override def actionPerformed(e:ActionEvent) = {
				currentSelectionModel.removeAllElements()
			}
		})
	}
	
/*	setLayout(new BoxLayout(this, boxXAxis))
	add(new JScrollPane(currentSelectionList,
			scrollVerticalAlways, scrollHorizontalNever), BorderLayout.WEST)
	add(new JPanel() {
		add(new JPanel() {
			setLayout(new BoxLayout(this, boxYAxis))
			add(addButton)
			add(removeButton)
			add(removeAllButton)
		})
	})
	add(new JScrollPane(allTokenClassesList,
			scrollVerticalAlways, scrollHorizontalAlways), BorderLayout.EAST)
*/
	setLayout(new BorderLayout)
	add(new JPanel(new BorderLayout){
		add(new JScrollPane(currentSelectionList,
				scrollVerticalAlways, scrollHorizontalNever), BorderLayout.WEST)
		add(new JPanel() {
			add(new JPanel() {
				setLayout(new BoxLayout(this, boxYAxis))
				add(addButton)
				add(removeButton)
				add(removeAllButton)
			})
		})
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
		if (isSelected)
		{
			returnValue.setBackground(list.getSelectionBackground)
//			returnValue.atkRow.setBackground(list.getSelectionBackground)
//			returnValue.weakRow.setBackground(list.getSelectionBackground)
		}
		returnValue
	}
}
