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
package com.rayrobdod.deductionTactics.swingView.host

import java.awt._
import java.awt.event._
import javax.swing._
import javax.swing.event._
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import com.rayrobdod.deductionTactics.swingView.ChooseAIsComponent

class Top {
	
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(resources.getString("startNewGameFrameTitle"))
	private val nextButton = new JButton(resources.getString("nextButton"))
	private val cancelButton = new JButton(resources.getString("cancelButton"))
	private val maps:ListModel[Nothing] = new ScalaSeqListModel(Nil)
	private val aisPanel = new ChooseAIsComponent
	
	{
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		
		val mapChoosingPanel = new JPanel(new GridBagLayout());
		{
			val mapPreviewPicture = new JLabel("Map Preview Picture")
			val tokensPerPlayer = new JLabel("4")
			val playerCount = new JLabel("4")
			val mapList = new JList()
			
			mapChoosingPanel.add(mapPreviewPicture, GridBagConstraintsFactory(gridx = 0, gridy = 0, gridwidth = 2, weighty = 2, weightx = 1, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(new JLabel(resources.getString("tokensLabel")), GridBagConstraintsFactory(gridx = 0, gridy = 1, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(tokensPerPlayer, GridBagConstraintsFactory(gridx = 1, gridy = 1, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(new JLabel(resources.getString("playersLabel")), GridBagConstraintsFactory(gridx = 0, gridy = 2, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(playerCount, GridBagConstraintsFactory(gridx = 1, gridy = 2, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(mapList, GridBagConstraintsFactory(gridx = 2, gridy = 0, gridheight = 3, weighty = 2, weightx = 2, fill = GridBagConstraints.BOTH))
		}
		
		val topPanel = new JPanel(new GridLayout(2,1));
		{
			topPanel.add(mapChoosingPanel)
			topPanel.add(aisPanel)
		}
		
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
		
		frame.getContentPane().add(topPanel)
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH)
		frame.pack()
	}
	
	
	def show() = {
		frame.setVisible(true);
	}
	
	def addNextActionListener(a:ActionListener) {
		nextButton.addActionListener(a)
	}
	
}

object Top {
	def main(args:Array[String]):Unit = {
		val t = new Top();
		t.addNextActionListener(new ActionListener() {
			def actionPerformed(e:ActionEvent) {
				Top.main(new Array[String](0))
			}
		})
		t.show();
	}
}
