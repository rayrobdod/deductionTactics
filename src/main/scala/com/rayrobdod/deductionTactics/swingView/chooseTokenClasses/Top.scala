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
package com.rayrobdod.deductionTactics.swingView.chooseTokenClasses

import java.awt._
import java.awt.event._
import javax.swing._
import javax.swing.event._
import java.net.InetAddress
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import com.rayrobdod.deductionTactics.TokenClass

class Top(val maxResultSize:Int) {
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(resources.getString("chooseTokensFrameTitle"))
	private val nextButton = new JButton(resources.getString("nextButton"))
	private val cancelButton = new JButton(resources.getString("cancelButton"))
	private val teamBuilder = new TeamBuilderPanel
	
	{
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		
		
		val buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		{
			buttonPanel.add(cancelButton)
			buttonPanel.add(nextButton)
			
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
		
		frame.getContentPane().add(teamBuilder)
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH)
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
		teamBuilder.currentSelection
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

