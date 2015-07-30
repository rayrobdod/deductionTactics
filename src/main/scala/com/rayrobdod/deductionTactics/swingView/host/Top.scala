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

import java.awt.{Component, BorderLayout, FlowLayout, GridLayout, GridBagLayout, GridBagConstraints}
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JList, ListCellRenderer, JButton, JPanel, JLabel, JFrame, WindowConstants, ListSelectionModel, JScrollPane}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_AS_NEEDED => scrollHorizontalAsNeeded}
import scala.collection.immutable.Seq
import scala.collection.mutable.Buffer
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import com.rayrobdod.deductionTactics.{Arena, Maps, PlayerAI}
import com.rayrobdod.deductionTactics.swingView.ChooseAIsComponent

/**
 * A display for selecting an arena and players
 * @since a.6.0
 */
class Top {
	import Top.NextListener
	import Top.MyCellRenderer
	
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(resources.getString("startNewGameFrameTitle"))
	private val nextListeners:Buffer[NextListener] = Buffer[NextListener]();
	private val nextButton = new JButton(resources.getString("nextButton"))
	
	{
		val mapList:JList[Arena] = new JList(new ScalaSeqListModel(Maps.arenas))
		val playerCount:JList[Int] = new JList(new ScalaSeqListModel(Seq.empty[Int]))
		val aisPanel = new ChooseAIsComponent
		
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		frame.setJMenuBar(new com.rayrobdod.deductionTactics.swingView.menuBar.MenuBar)
		
		val mapChoosingPanel = new JPanel(new GridBagLayout());
		{
			val mapPreviewPicture = new JLabel("Map Preview Picture")
			val tokensPerPlayer = new JLabel("")
			val mapListScrollPane = new JScrollPane(mapList, scrollVerticalAsNeeded, scrollHorizontalAsNeeded)
			
			mapChoosingPanel.setName("mapChoosingPanel")
			mapList.setName("mapList")
			mapListScrollPane.setName("mapListScrollPane")
			playerCount.setName("playerCount")
			
			playerCount.setLayoutOrientation(JList.VERTICAL_WRAP)
			playerCount.setVisibleRowCount(1)
			playerCount.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
			mapList.setVisibleRowCount(8)
			mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
			
			mapList.setCellRenderer(MyCellRenderer)
			mapList.addListSelectionListener(new ListSelectionListener(){
				override def valueChanged(e:ListSelectionEvent):Unit = {
					if (! mapList.isSelectionEmpty) {
						playerCount.setModel(new ScalaSeqListModel(mapList.getSelectedValue.possiblePlayers.toSeq))
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
					if (! mapList.isSelectionEmpty && ! playerCount.isSelectionEmpty) {
						tokensPerPlayer.setText(mapList.getSelectedValue.startSpaces(playerCount.getSelectedValue).map{_.size}.mkString("[", ",", "]"))
						nextButton.setEnabled(true)
						aisPanel.players = playerCount.getSelectedValue
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
			mapChoosingPanel.add(mapListScrollPane, GridBagConstraintsFactory(gridx = 2, gridy = 0, gridheight = 3, weighty = 2, weightx = 2, fill = GridBagConstraints.BOTH))
		}
		
		val topPanel = new JPanel(new GridBagLayout);
		frame.getContentPane().add(topPanel);
		{
			topPanel.setName("topPanel")
			topPanel.add(mapChoosingPanel, GridBagConstraintsFactory(gridx = 0, gridy = 0, weighty = 1, weightx = 1, fill = GridBagConstraints.BOTH))
			topPanel.add(aisPanel, GridBagConstraintsFactory(gridx = 0, gridy = 1, weighty = 1, weightx = 1, fill = GridBagConstraints.BOTH))
		}
		
		val buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		{
			val cancelButton = new JButton(resources.getString("cancelButton"))
			
			buttonPanel.setName("buttonPanel")
			buttonPanel.add(cancelButton)
			buttonPanel.add(nextButton)
			cancelButton.setName("cancelButton")
			nextButton.setName("nextButton")
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
							Maps.arenas(mapList.getSelectedIndex)
						)
					}
				}
			})
		}
		
		frame.pack()
	}
	
	
	def setVisible(b:Boolean):Unit = {
		frame.setVisible(b);
	}
	
	def addNextListener(a:NextListener) {
		nextListeners append a
	}
}

object Top {
	type NextListener = Function2[Seq[PlayerAI], Arena, Unit]
	
	def main(args:Array[String]):Unit = {
		val t = new Top();
		t.addNextListener({(ais:Seq[PlayerAI], map:Arena) =>
			System.out.println(ais)
			System.out.println(map)
		})
		t.setVisible(true);
	}
	
	object MyCellRenderer extends ListCellRenderer[Arena] {
		private val backing = new javax.swing.DefaultListCellRenderer
		
		override def getListCellRendererComponent(list:JList[_ <: Arena], value:Arena,
				index:Int, isSelected:Boolean, cellHasFocus:Boolean
		):Component = {
			backing.getListCellRendererComponent(list:JList[_], value.name, index, isSelected, cellHasFocus)
		}
	}
}
