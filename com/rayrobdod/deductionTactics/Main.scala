package com.rayrobdod.deductionTactics
package main

import swingView._
import java.awt.BorderLayout
import java.awt.BorderLayout.{SOUTH => borderSouth, NORTH => borderNorth}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import scala.swing.Swing.ActionListener
import scala.collection.immutable.Seq
import javax.swing.{JFrame, JButton, JPanel, JCheckBox}
import java.awt.event.ActionEvent
import com.rayrobdod.boardGame.{Moved}
import com.rayrobdod.boardGame.RectangularField


/**
 * @author Raymond Dodge
 * @version 2012 Apr 20
 * @version 30 May 2012 - moving the tokens' moving to their starting spaces
 		until after all players are initialized
 * @version 05 Jun 2012 - adding a sleep after tokens move in hopes that they
 		are done moving by the time the sleep ends
 * @version 28 Jun 2012 - adding a few default buttons to the startNewGame frame
 * @version 12 Jul 2012 - removing viewpoint choosers, since PlayerAI decorators can do that now
 * @version 04 Aug 2012 - putting the PlayerAI.initialize(player,field) inside a loop
 * @version 28 Nov 2012 - adding map-selection abilities
 */
object Main extends App
{
	this.startNewGame
	
	def startNewGame
	{
		val okButton = new JButton("OK")
		val aiChooser = new ChooseAIsComponent()
		val mapChooser = new ChooseMapComponent()
		
		val aiChooserFrame = new JFrame("Choose PlayerTypes")
		aiChooserFrame.getContentPane.add({
			val returnValue = new JPanel(new BorderLayout)
			returnValue.add(mapChooser, borderNorth)
			returnValue.add(aiChooser)
			returnValue
		})
		aiChooserFrame.getContentPane.add(okButton, borderSouth)
		aiChooserFrame.getRootPane.setDefaultButton(okButton);

		
		mapChooser.numPlayersList.addListSelectionListener(AiChooserCountChanger)
		object AiChooserCountChanger extends ListSelectionListener {
			def valueChanged(e:ListSelectionEvent) = {
				Option(mapChooser.numPlayersList.getSelectedValue).map{_.intValue}.foreach{(x:Int) =>
					aiChooser.players = x;
					aiChooserFrame.pack();
				}
			}
		}
		
		okButton.addActionListener(ActionListener{(e:ActionEvent) =>
			aiChooserFrame.setVisible(false)
			new Thread(new Runnable{
				def run = buildTeams(
						aiChooser.getAIs,
						Maps.getMap(mapChooser.mapList.getSelectedIndex),
						Maps.startingPositions(mapChooser.mapList.getSelectedIndex, mapChooser.numPlayersList.getSelectedValue))
			}, "build teams").start()
		})
		
		aiChooserFrame.pack()
		aiChooserFrame.setVisible(true)
	}
	
	private def buildTeams(ais:Seq[PlayerAI], field:RectangularField, tokenPositions:Seq[Seq[(Int, Int)]]) =
	{
		val tokenClasses = ais.map{_.buildTeam}
		val canonTokens = tokenClasses.map{_.map{new CannonicalToken(_)}}
		val mirrorTokens = canonTokens.map{_.map{new MirrorToken(_)}}
		
		val canonListTokens = new CannonicalListOfTokens(canonTokens)
		val playerListOfTokens = canonTokens.zip(mirrorTokens).map{(
			(canon:Seq[CannonicalToken], mirror:Seq[MirrorToken]) => {
				new PlayerListOfTokens(canon, mirrorTokens diff Seq(mirror))
			}
		).tupled}
		val allTokens = (canonTokens ++ mirrorTokens).flatten
		
		val players = playerListOfTokens.map(new Player(_))
		ais.zip(players).foreach({(ai:PlayerAI, player:Player) =>
			ai.initialize(player, field)
			// Breaks a lock or something I DON'T KNOW LEAVE ME ALONE
			//player.reactions.+=(ai)
		}.tupled)
		
		canonTokens.flatten.zip(mirrorTokens.flatten).foreach({(hisCannon:CannonicalToken, hisMirror:MirrorToken) => {
			canonTokens.flatten.foreach{(mine:CannonicalToken) => {
				mine.reactions.+=(new hisCannon.BeAttackedReaction(hisMirror))
			}}
		}}.tupled)
	
		players.zip(canonTokens).foreach({(p:Player, ts:Seq[CannonicalToken]) => {
			ts.foreach{(t:CannonicalToken) => {
				p.reactions.+=(t.TurnStartReaction)
				p.reactions.+=(t.AttackReaction)
				p.reactions.+=(t.MoveReaction)
				p.reactions.+=(new t.StatusAct(p.tokens))
			}}
		}}.tupled)
		canonTokens.foreach{(seq:Seq[CannonicalToken]) => {
			UnitAwareSpaceClass.tokens.tokens = UnitAwareSpaceClass.tokens.tokens :+ seq;
		}}
		allTokens.foreach{(x:Token) => {
			x.reactions += new UnselectOtherTokens(x,allTokens) 
		}}
		
		canonTokens.zip(tokenPositions).map(
			{(x:Seq[CannonicalToken], y:Seq[(Int, Int)]) => x.zip(y)}.tupled
		).flatten.foreach({(t:CannonicalToken, p:(Int,Int)) => 
			t ! Moved(field.space(p._1, p._2), true)
		}.tupled)
		
		allTokens.foreach{_.start()}
		players.foreach{_.start()}
		
		// pray that the tokens have moved by the time this sleep is over
		// This seems to solve a null pointer exception in the PlayerAI
		Thread.sleep(1000)
		
		// run, meaning this returns when PlayerTurnCycler returns
		new PlayerTurnCycler(players.zip(ais)).run()
	}
}
