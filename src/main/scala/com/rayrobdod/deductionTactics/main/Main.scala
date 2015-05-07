/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

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
package com.rayrobdod.deductionTactics
package main

import swingView._
import java.awt.BorderLayout
import java.awt.BorderLayout.{SOUTH => borderSouth, NORTH => borderNorth}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import scala.collection.immutable.Seq
import javax.swing.{JFrame, JButton, JPanel, JCheckBox, BorderFactory}
import java.awt.event.{ActionEvent, ActionListener}
import com.rayrobdod.boardGame.{RectangularField, Space}


/**
 * @author Raymond Dodge
 * @version a.5.0
 */
object Main extends App
{
	this.startNewGame
	
	def startNewGame
	{
		val okButton = new JButton("OK")
		val aiChooser = new ChooseAIsComponent()
		val mapChooser = new ChooseMapComponent()
		
		aiChooser.setBorder(BorderFactory.createMatteBorder(3,0,0,0,java.awt.Color.BLACK))
		
		val aiChooserFrame = new JFrame("Choose PlayerTypes")
		aiChooserFrame.getContentPane.add({
			val returnValue = new JPanel(new BorderLayout)
			returnValue.add(mapChooser, borderNorth)
			returnValue.add(aiChooser)
			returnValue
		})
		aiChooserFrame.getContentPane.add(okButton, borderSouth)
		aiChooserFrame.getRootPane.setDefaultButton(okButton);
		aiChooserFrame.setJMenuBar(new swingView.MenuBar)
		
		
		mapChooser.numPlayersList.addListSelectionListener(AiChooserCountChanger)
		object AiChooserCountChanger extends ListSelectionListener {
			def valueChanged(e:ListSelectionEvent) = {
				Option(mapChooser.numPlayersList.getSelectedValue).map{_.intValue}.foreach{(x:Int) =>
					aiChooser.players = x;
					aiChooserFrame.pack();
				}
			}
		}
		
		okButton.addActionListener(new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				aiChooserFrame.setVisible(false)
				new Thread(new Runnable{
					def run = buildTeams(
							aiChooser.getAIs,
							Maps.getMap(mapChooser.mapList.getSelectedIndex),
							Maps.startingPositions(mapChooser.mapList.getSelectedIndex, mapChooser.numPlayersList.getSelectedValue))
				}, "build teams").start()
			}
		})
		
		aiChooserFrame.pack()
		aiChooserFrame.setVisible(true)
	}
	
	private def buildTeams(ais:Seq[PlayerAI], field:RectangularField[SpaceClass], tokenPositions:Seq[Seq[(Int, Int)]]) =
	{
		val roundOneTokenClasses:Seq[Seq[TokenClass]] = ais.zip(tokenPositions.map{_.length * 2}).map({(p:PlayerAI, l:Int) => p.selectTokenClasses(l)}.tupled)
		val roundTwoTokenClasses:Seq[Seq[TokenClass]] = ais.zip(tokenPositions.map{_.length}).zipWithIndex.map{input => 
			val ((p:PlayerAI, len:Int), index:Int) = input
			p.narrowTokenClasses(roundOneTokenClasses, len, index)
		}
		
		val TokenClassToSpaceIndex:Seq[Seq[(TokenClass, (Int, Int))]] = roundTwoTokenClasses.zip(tokenPositions).map({(x:Seq[TokenClass],y:Seq[(Int, Int)]) => x.zip(y)}.tupled)
		val tokenClassToSpace:Seq[Seq[(Option[TokenClass], Space[SpaceClass])]] = TokenClassToSpaceIndex.map{_.map{(x) => ((Option(x._1), field.space(x._2._1, x._2._2) ))}}
		// limit number of tokens to number of availiable spaces.
		
		val tokens = new ListOfTokens( tokenClassToSpace.map{_.map{(x) => new Token(x._2, tokenClass = x._1)}} )
		
		
		val initialState = GameState(field, tokens)
		
		
		
		
		// run, meaning this returns when PlayerTurnCycler returns
		new PlayerTurnCycler(ais, initialState).run()
	}
}
