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
import com.rayrobdod.boardGame.{RectangularField}


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
		val tokenClasses = ais.zip(tokenPositions.map{_.length}).map({(p:PlayerAI, l:Int) => p.buildTeam(l)}.tupled)
				// limit number of tokens to number of availiable spaces.
				.zip(tokenPositions).map({(x:Seq[TokenClass],y:Seq[_]) => x.take(y.size)}.tupled)
		val tokens = tokenClasses.map{_.map{new Token(_)}}
		
		val tokensList = new ListOfTokens(canonTokens)
		
		
		
		canonTokens.zip(tokenPositions).map(
			{(x:Seq[Token], y:Seq[(Int, Int)]) => x.zip(y)}.tupled
		).flatten.foreach({(t:Token, p:(Int,Int)) => 
			t.requestMoveTo(field.space(p._1, p._2))
		}.tupled)
		
		
		val players = playerListOfTokens.zip(ais).map({new Player(_, _)}.tupled)
		players.foreach({(player:Player) =>
			player.ai.initialize(player, field)
		})
		
		players.zip(canonTokens).foreach({(p:Player, ts:Seq[Token]) => {
			ts.foreach{(t:Token) => {
				p.addStartTurnReaction(t.TurnStartReaction)
				p.addEndTurnReaction(t.TurnEndReaction)
				p.addStartTurnReaction(new t.StatusAct(p.tokens))
			}}
		}}.tupled)
		
		// run, meaning this returns when PlayerTurnCycler returns
		new PlayerTurnCycler(players).run()
	}
}
