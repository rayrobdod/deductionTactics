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
import scala.collection.immutable.Seq
import scala.collection.mutable.Buffer
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import com.rayrobdod.deductionTactics.{Maps, PlayerAI}
import com.rayrobdod.deductionTactics.swingView.ChooseAIsComponent

class Top {
	type NextListener = Function3[Seq[PlayerAI], String, Seq[Seq[(Int,Int)]], Unit]
	
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(resources.getString("startNewGameFrameTitle"))
	private val nextListeners:Buffer[NextListener] = Buffer[NextListener]();
	private val nextButton = new JButton(resources.getString("nextButton"))
	
	{
		val mapList:JList[String] = new JList(new ScalaSeqListModel(Maps.names))
		val playerCount:JList[Int] = new JList(new ScalaSeqListModel(Seq(2,4)))
		val aisPanel = new ChooseAIsComponent
		
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		
		val mapChoosingPanel = new JPanel(new GridBagLayout());
		{
			val mapPreviewPicture = new JLabel("Map Preview Picture")
			val tokensPerPlayer = new JLabel("4")
			
			playerCount.setLayoutOrientation(JList.VERTICAL_WRAP)
			playerCount.setVisibleRowCount(1)
			mapList.setVisibleRowCount(8)
			
			mapList.addListSelectionListener(new ListSelectionListener(){
				override def valueChanged(e:ListSelectionEvent):Unit = {
					if (mapList.getSelectedIndex >= 0) {
						playerCount.setModel(new ScalaSeqListModel(Maps.possiblePlayers(mapList.getSelectedIndex).toSeq))
						playerCount.setSelectedIndex(0)
						nextButton.setEnabled(true)
					} else {
						playerCount.setModel(new ScalaSeqListModel(Nil))
						tokensPerPlayer.setText(resources.getString("invalid"))
						nextButton.setEnabled(false)
					}
				}
			})
			playerCount.addListSelectionListener(new ListSelectionListener(){
				override def valueChanged(e:ListSelectionEvent):Unit = {
					if (mapList.getSelectedIndex >= 0 && playerCount.getSelectedIndex >= 0) {
						tokensPerPlayer.setText(Maps.startingPositions(mapList.getSelectedIndex, playerCount.getSelectedValue).map{_.size}.toString)
						nextButton.setEnabled(true)
					} else {
						tokensPerPlayer.setText(resources.getString("invalid"))
						nextButton.setEnabled(false)
					}
				}
			})
			
			mapList.setSelectedIndex(0)
			
			mapChoosingPanel.add(mapPreviewPicture, GridBagConstraintsFactory(gridx = 0, gridy = 0, gridwidth = 2, weighty = 2, weightx = 1, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(new JLabel(resources.getString("tokensLabel")), GridBagConstraintsFactory(gridx = 0, gridy = 1, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(tokensPerPlayer, GridBagConstraintsFactory(gridx = 1, gridy = 1, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(new JLabel(resources.getString("playersLabel")), GridBagConstraintsFactory(gridx = 0, gridy = 2, fill = GridBagConstraints.BOTH))
			mapChoosingPanel.add(playerCount, GridBagConstraintsFactory(gridx = 1, gridy = 2, anchor = GridBagConstraints.LINE_START))
			mapChoosingPanel.add(mapList, GridBagConstraintsFactory(gridx = 2, gridy = 0, gridheight = 3, weighty = 2, weightx = 2, fill = GridBagConstraints.BOTH))
		}
		
		val topPanel = new JPanel(new GridLayout(2,1));
		frame.getContentPane().add(topPanel);
		{
			topPanel.add(mapChoosingPanel)
			topPanel.add(aisPanel)
		}
		
		val buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		{
			val cancelButton = new JButton(resources.getString("cancelButton"))
			
			buttonPanel.add(cancelButton)
			buttonPanel.add(nextButton)
			nextButton.getRootPane.setDefaultButton(nextButton)
			
			cancelButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent) {
					Top.this.frame.dispose();
				}
			})
			nextButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent) {
					Top.this.frame.dispose();
					nextListeners.foreach{x:NextListener =>
						x.apply(
							aisPanel.getAIs,
							mapList.getSelectedValue,
							Maps.startingPositions(mapList.getSelectedIndex, playerCount.getSelectedValue)
						)
					}
				}
			})
		}
		
		frame.pack()
	}
	
	
	def setVisible(b:Boolean) = {
		frame.setVisible(b);
	}
	
	def addNextListener(a:NextListener) {
		nextListeners append a
	}
}

object Top {
	def main(args:Array[String]):Unit = {
		val t = new Top();
		t.addNextListener({(ais:Seq[PlayerAI], map:String, startSpaces:Seq[Seq[(Int,Int)]]) =>
			System.out.println(ais)
			System.out.println(map)
			System.out.println(startSpaces)
		})
		t.setVisible(true);
	}
}
