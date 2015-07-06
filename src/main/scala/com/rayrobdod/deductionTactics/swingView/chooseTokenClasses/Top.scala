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
package chooseTokenClasses

import java.awt._
import java.awt.event._
import javax.swing._
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis, X_AXIS => boxXAxis}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_ALWAYS => scrollHorizontalAlways
}
import javax.swing.event._
import java.net.InetAddress
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import com.rayrobdod.deductionTactics.TokenClass
import scala.collection.immutable.{Seq, IndexedSeq}
import scala.collection.JavaConversions.asScalaBuffer

class Top(val maxResultSize:Int) {
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(resources.getString("chooseTokensFrameTitle"))
	private val nextButton = new JButton(resources.getString("nextButton"))
	private val cancelButton = new JButton(resources.getString("cancelButton"))
	private val selectedClasses = new DefaultListModel[TokenClass]()
	private val selectedClassesList = new TokenClassList(selectedClasses)
	
	{
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		
		selectedClassesList.setPrototypeCellValue(TokenClass.allKnown.head)
		
		val allClasssesList = new TokenClassList(new ScalaSeqListModel(TokenClass.allKnown))
		allClasssesList.setLayoutOrientation(JList.VERTICAL_WRAP)
		allClasssesList.setVisibleRowCount(10)
		
		val addRemoveButtonPanel = new JPanel();
		{
			addRemoveButtonPanel.setLayout(new BoxLayout(addRemoveButtonPanel, boxYAxis))
			
			val addButton = new JButton(resources.getString("addTeambuilderButton"))
			addButton.addActionListener(new ActionListener(){
				override def actionPerformed(e:ActionEvent):Unit = {
					allClasssesList.getSelectedValuesList.foreach{x =>
						if (selectedClasses.getSize < maxResultSize) {
							selectedClasses.addElement(x)
						}
					}
				}
			})
			val removeButton = new JButton(resources.getString("removeTeambuilderButton"))
			removeButton.addActionListener(new ActionListener(){
				override def actionPerformed(e:ActionEvent):Unit = {
					selectedClassesList.getSelectedValuesList.foreach{
						selectedClasses.removeElement(_)
					}
				}
			})
			val removeAllButton = new JButton(resources.getString("removeAllTeambuilderButton"))
			removeAllButton.addActionListener(new ActionListener(){
				override def actionPerformed(e:ActionEvent):Unit = {
					selectedClasses.removeAllElements()
				}
			})
			
			object AddRemoveEnableListener extends ListDataListener() {
				override def intervalAdded(e:ListDataEvent):Unit = contentsChanged(e)
				override def intervalRemoved(e:ListDataEvent):Unit = contentsChanged(e)
				
				override def contentsChanged(e:ListDataEvent):Unit = {
					val notEmpty = (selectedClasses.getSize != 0)
					val notFull = (selectedClasses.getSize != maxResultSize)
					
					addButton.setEnabled(notFull)
					removeButton.setEnabled(notEmpty)
					removeAllButton.setEnabled(notEmpty)
				}
			}
			AddRemoveEnableListener.contentsChanged(null);
			selectedClasses.addListDataListener(AddRemoveEnableListener)  
			
			addRemoveButtonPanel.add(addButton);
			addRemoveButtonPanel.add(removeButton);
			addRemoveButtonPanel.add(removeAllButton);
		}
		
		val leftPanel = new JPanel();
		{
			leftPanel.setLayout(new BoxLayout(leftPanel, boxXAxis))
			
			leftPanel.add(new JScrollPane(selectedClassesList,
					scrollVerticalAlways, scrollHorizontalNever));
			leftPanel.add(addRemoveButtonPanel);
		}
		
		
		
		val navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		{
			navButtonPanel.add(cancelButton)
			navButtonPanel.add(nextButton)
			
			cancelButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent) {
					Top.this.frame.dispose();
				}
			})
			nextButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent) {
					Top.this.frame.dispose();
				}
			})
		}
		
		// frame.getContentPane().add(teamBuilder)
		frame.getContentPane().add(new JScrollPane(allClasssesList,
					scrollVerticalAlways, scrollHorizontalAlways))
		frame.getContentPane().add(leftPanel, BorderLayout.WEST)
		frame.getContentPane().add(navButtonPanel, BorderLayout.SOUTH)
		frame.pack()
	}
	
	
	def show():Unit = {
		frame.setVisible(true);
	}
	
	def addNextActionListener(a:ActionListener) {
		nextButton.addActionListener(a)
	}
	
	/**
	 * @post results.size <= maxResultSize
	 */
	def results:Seq[TokenClass] = {
		val a = selectedClassesList.getModel()
		
		class ListModelSeq[A](a:ListModel[A]) extends IndexedSeq[A] {
			override def apply(index:Int):A = a.getElementAt(index)
			override def length:Int = a.getSize
		}
		
		new ListModelSeq(a)
	}
}

object Top {
	def main(args:Array[String]):Unit = {
		val t = new Top(5);
		t.addNextActionListener(new ActionListener() {
			def actionPerformed(e:ActionEvent) {
				Top.main(new Array[String](0))
			}
		})
		t.show();
	}
}

