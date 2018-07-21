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
package com.rayrobdod.deductionTactics.swingView.menuBar

import java.awt.event.{ActionListener, ActionEvent}
import java.awt.event.{KeyEvent, KeyAdapter}
import javax.swing.{JPanel, JList, JButton, JLabel, JTextField, KeyStroke, Icon}
import com.rayrobdod.swing.GridBagConstraintsFactory
import com.rayrobdod.boardGame.RectangularSpace
import com.rayrobdod.boardGame.RectangularIndex
import com.rayrobdod.boardGame.view.Tilesheet
import com.rayrobdod.boardGame.view.RectangularDimension
import com.rayrobdod.deductionTactics.SpaceClass
import com.rayrobdod.deductionTactics.swingView.AvailibleTilesheetListModel
import com.rayrobdod.deductionTactics.swingView.RectangularTilesheet
import com.rayrobdod.deductionTactics.swingView.game.{preferences => gamePreferences, KeyboardActions}
import KeyboardActions.KeyboardAction

/**
 * A user interface for setting options
 * 
 * Previously known as OptionsPanel
 * @version next
 */
class AppearanceOptionsPanel extends JPanel
{
	val currentTilesheet = new JList[RectangularTilesheet](AvailibleTilesheetListModel)
	currentTilesheet.setCellRenderer(TilesheetListRenderer)
	currentTilesheet.setSelectedValue(gamePreferences.currentTilesheet, true)
	
	val movementSpeed = new JTextField(5)
	movementSpeed.setText(gamePreferences.movementSpeed.toString)
	
	this.setLayout(new java.awt.GridBagLayout())
	this.add(new JLabel("the tilesheet to use"),
			GridBagConstraintsFactory( gridx = 0, gridy = 0 ))
	this.add(currentTilesheet,
			GridBagConstraintsFactory( gridx = 0, gridy = 1 ))
	this.add(new JLabel("Tokens' movement speed"),
			GridBagConstraintsFactory( gridx = 1, gridy = 0 ))
	this.add(movementSpeed,
			GridBagConstraintsFactory( gridx = 1, gridy = 1 ))
	
	
	object apply extends ActionListener() {
		override def actionPerformed(e:ActionEvent):Unit =
		{
			gamePreferences.currentTilesheet = currentTilesheet.getSelectedValue()
			gamePreferences.movementSpeed = Integer.parseInt(movementSpeed.getText)
		}
	}
}

/**
 * A user interface for setting options
 * 
 * @since next
 */
class KeyInputsOptionsPanel extends JPanel {
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	this.setLayout(new java.awt.GridLayout(0,2))
	gamePreferences.inputMap.foreach{x =>
		val labelStr = resources.getString("optionsKeyboard" + x._1.name)
		KeyInputsOptionsPanel.this.add(new JLabel(labelStr))
		KeyInputsOptionsPanel.this.add(new KeyStrokeSetter(x._1, x._2))
	}
	
	//
	class KeyStrokeSetter(val action:KeyboardAction, initial:KeyStroke) extends JButton {
		private[this] var _keyStroke:KeyStroke = initial
		private[this] var isInInputState:Boolean = false
		def keyStroke:KeyStroke = _keyStroke
		
		def resetText():Unit = {
			if (isInInputState) {
				this.setText("Type new input key")
			} else {
				this.setText(_keyStroke.toString)
			}
			this.repaint()
		}
		this.resetText()
		
		this.addActionListener(new ActionListener() {
			override def actionPerformed(e:ActionEvent):Unit = {
				KeyStrokeSetter.this.isInInputState = ! KeyStrokeSetter.this.isInInputState
				KeyStrokeSetter.this.requestFocus()
				KeyStrokeSetter.this.resetText()
			}
		})
		this.addKeyListener(new KeyAdapter() {
			override def keyPressed(e:KeyEvent):Unit = {
				if (KeyStrokeSetter.this.isInInputState) {
					KeyStrokeSetter.this._keyStroke = KeyStroke.getKeyStrokeForEvent(e)
					KeyStrokeSetter.this.isInInputState = false
					KeyStrokeSetter.this.resetText()
				}
			}
		})
		
		this.setFocusable(true)
	}
	
	object apply extends ActionListener() {
		override def actionPerformed(e:ActionEvent):Unit = {
			gamePreferences.inputMap = Map.empty ++ (
				KeyInputsOptionsPanel.this.getComponents
					.filter{_.isInstanceOf[KeyStrokeSetter]}
					.map{_.asInstanceOf[KeyStrokeSetter]}
					.map{x => ((x.action, x.keyStroke))}
			)
		}
	}
}
