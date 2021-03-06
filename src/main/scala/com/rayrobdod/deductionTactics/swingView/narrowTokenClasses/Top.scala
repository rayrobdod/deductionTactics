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
package narrowTokenClasses

import java.awt._
import java.awt.event._
import javax.swing._
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever
}
import com.rayrobdod.swing.ScalaSeqListModel
import com.rayrobdod.deductionTactics.TokenClass
import scala.collection.immutable.Seq
import scala.collection.JavaConversions.asScalaBuffer

/**
 * @pre 0 <= myIndex < choosenClasses.size
 * @since a.6.0
 */
class Top(
		val myIndex:Int,
		val choosenClasses:Seq[Seq[TokenClass]],
		val maxResultSize:Int
) {
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(resources.getString("chooseTokensFrameTitle"))
	private val nextButton = new JButton(resources.getString("nextButton"))
	private val cancelButton = new JButton(resources.getString("cancelButton"))
	private val myClassesList = new TokenClassList(new ScalaSeqListModel(choosenClasses(myIndex)))
	
	{
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		
		
		val classesLists = choosenClasses.indices.map{x => new TokenClassList(new ScalaSeqListModel(choosenClasses(x)))}.updated(myIndex, myClassesList)
		classesLists.zip(choosenClasses).foreach{pair =>
			val (list, model) = pair
			list.setPrototypeCellValue(TokenClass.allKnown.head)
			list.setEnabled(false)
		}
		
		myClassesList.setEnabled(true)
		myClassesList.setSelectionModel(new Top.MaxCountListSelectionModel(maxResultSize))
		
		val listsPanel = new JPanel(new GridLayout(1,0));
		classesLists.map{x => 
			new JScrollPane(x, scrollVerticalAlways, scrollHorizontalNever)
		}.foreach{listsPanel.add(_)}
		
		
		
		
		val navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		frame.getContentPane().add(navButtonPanel, BorderLayout.SOUTH);
		{
			navButtonPanel.add(cancelButton)
			navButtonPanel.add(nextButton)
			cancelButton.setName("cancelButton")
			nextButton.setName("nextButton")
			nextButton.getRootPane.setDefaultButton(nextButton)
			
			cancelButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent):Unit = {
					Top.this.frame.dispose();
				}
			})
			nextButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent):Unit = {
					Top.this.frame.dispose();
				}
			})
		}
		
		frame.getContentPane().add(listsPanel)
		frame.pack()
		// pack makes the size larger than desktop
		frame.setSize(
				math.min(800, frame.getSize.width),
				math.min(600, frame.getSize.height)
		)
	}
	
	
	def show():Unit = {
		frame.setVisible(true);
	}
	
	def addNextActionListener(a:ActionListener):Unit = {
		nextButton.addActionListener(a)
	}
	
	/**
	 * @post results.size <= maxResultSize
	 */
	def results:Seq[TokenClass] = {
		myClassesList.getSelectedValuesList().toList
	}
}

object Top {
	def main(args:Array[String]):Unit = {
		val l = (0 to 30 by 10).map{a =>
			(0 to 9).map{b => 
				TokenClass.allKnown(a + b)
			}
		}
		
		val t = new Top(1, l, 4);
		t.addNextActionListener(new ActionListener() {
			def actionPerformed(e:ActionEvent):Unit = {
				Top.main(new Array[String](0))
			}
		})
		t.show();
	}
	
	// TODO: convert to decorator
	class MaxCountListSelectionModel(maxSelected:Int) extends DefaultListSelectionModel {
		private def selectedCount:Int = {
			(getMinSelectionIndex to getMaxSelectionIndex).count{isSelectedIndex(_)}
		}
		
		
		override def addSelectionInterval(a:Int, b:Int):Unit = {
			if (a > b) {
				this.addSelectionInterval(b, a)
			} else {
				val c = math.min(b, a + maxSelected - selectedCount - 1)
				if (c >= a) {super.addSelectionInterval(a, c)}
			}
		}
		override def setSelectionInterval(a:Int, b:Int):Unit = {
			if (a > b) {
				this.addSelectionInterval(b, a)
			} else {
				val c = math.min(b, a + maxSelected - 1)
				super.setSelectionInterval(a, c)
			}
		}
	}
}
