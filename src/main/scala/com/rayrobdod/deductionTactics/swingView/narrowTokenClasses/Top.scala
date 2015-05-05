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
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_ALWAYS => scrollHorizontalAlways
}
import javax.swing.event._
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import com.rayrobdod.deductionTactics.TokenClass
import scala.collection.JavaConversions.asScalaBuffer

/**
 * @pre 0 <= myIndex < choosnClasses.size
 */
class Top(val myIndex:Int, val choosenClasses:Seq[Seq[TokenClass]]) {
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
		
		val listsPanel = new JPanel(new GridLayout(1,0));
		classesLists.map{x => 
			new JScrollPane(x, scrollVerticalAlways, scrollHorizontalNever)
		}.foreach{listsPanel.add(_)}
		
		
		
		
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
		
		frame.getContentPane().add(listsPanel)
		frame.getContentPane().add(navButtonPanel, BorderLayout.SOUTH)
		frame.pack()
	}
	
	
	def show() = {
		frame.setVisible(true);
	}
	
	def addNextActionListener(a:ActionListener) {
		nextButton.addActionListener(a)
	}
	
	/**
	 * @post results.size <= maxResultSize
	 */
	def results:Seq[TokenClass] = {
		myClassesList.getSelectedValuesList()
	}
}

object Top {
	def main(args:Array[String]):Unit = {
		val l = Seq(
			Seq(TokenClass.allKnown(2), TokenClass.allKnown(4), TokenClass.allKnown(6)),
			Seq(TokenClass.allKnown(22), TokenClass.allKnown(24), TokenClass.allKnown(26)),
			Seq(TokenClass.allKnown(12), TokenClass.allKnown(14), TokenClass.allKnown(16))
		)
		
		val t = new Top(1, l);
		t.addNextActionListener(new ActionListener() {
			def actionPerformed(e:ActionEvent) {
				Top.main(new Array[String](0))
			}
		})
		t.show();
	}
}

