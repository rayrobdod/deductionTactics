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
 * @version 2012 Apr 20
 * @version 30 May 2012 - moving the tokens' moving to their starting spaces
 		until after all players are initialized
 * @version 05 Jun 2012 - adding a sleep after tokens move in hopes that they
 		are done moving by the time the sleep ends
 * @version 28 Jun 2012 - adding a few default buttons to the startNewGame frame
 * @version 12 Jul 2012 - removing viewpoint choosers, since PlayerAI decorators can do that now
 * @version 04 Aug 2012 - putting the PlayerAI.initialize(player,field) inside a loop
 * @version 28 Nov 2012 - adding map-selection abilities
 * @version 13 Jan 2013 - limit number of tokens to number of availiable spaces.
 * @version 14 Jun 2013 - Giving the initial frame a menu bar
 * @version 2013 Aug 07 - ripples from rewriting Player
 * @version 2013 Aug 07 - removing scala.swing stuff
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
	
	private def buildTeams(ais:Seq[PlayerAI], field:RectangularField, tokenPositions:Seq[Seq[(Int, Int)]]) =
	{
		val tokenClasses = ais.map{_.buildTeam}
				// limit number of tokens to number of availiable spaces.
				.zip(tokenPositions).map({(x:Seq[CannonicalTokenClass],y:Seq[_]) => x.take(y.size)}.tupled)
		val canonTokens = tokenClasses.map{_.map{new CannonicalToken(_)}}
		val mirrorTokens = canonTokens.map{_.map{new MirrorToken(_)}}
		
		val canonListTokens = new CannonicalListOfTokens(canonTokens)
		val playerListOfTokens = canonTokens.zip(mirrorTokens).map{(
			(canon:Seq[CannonicalToken], mirror:Seq[MirrorToken]) => {
				new PlayerListOfTokens(canon, mirrorTokens diff Seq(mirror))
			}
		).tupled}
		val allTokens = (canonTokens ++ mirrorTokens).flatten
		
		val players = playerListOfTokens.zip(ais).map({new Player(_, _)}.tupled)
		players.foreach({(player:Player) =>
			player.ai.initialize(player, field)
			//   Breaks a lock or something I DON'T KNOW LEAVE ME ALONE
			//player.reactions.+=(ai)
		})
		
		/* canonTokens.flatten.zip(mirrorTokens.flatten).foreach({(hisCannon:CannonicalToken, hisMirror:MirrorToken) =>
			canonTokens.flatten.foreach{(mine:CannonicalToken) =>
				mine.reactions.+=(new hisCannon.BeAttackedReaction(hisMirror))
			}
		}.tupled) */
	
		players.zip(canonTokens).foreach({(p:Player, ts:Seq[CannonicalToken]) => {
			ts.foreach{(t:CannonicalToken) => {
				p.addStartTurnReaction(t.TurnStartReaction)
				p.addStartTurnReaction(new t.StatusAct(p.tokens))
			}}
		}}.tupled)
		canonTokens.foreach{(seq:Seq[CannonicalToken]) => {
			UnitAwareSpaceClass.tokens.tokens = UnitAwareSpaceClass.tokens.tokens :+ seq;
		}}
		allTokens.foreach{(x:Token) => {
			x.addSelectedReaction(new UnselectOtherTokens(x,allTokens)) 
		}}
		
		canonTokens.zip(tokenPositions).map(
			{(x:Seq[CannonicalToken], y:Seq[(Int, Int)]) => x.zip(y)}.tupled
		).flatten.foreach({(t:CannonicalToken, p:(Int,Int)) => 
			t.requestMoveTo(field.space(p._1, p._2))
		}.tupled)
		
		
		// pray that the tokens have moved by the time this sleep is over
		// This seems to solve a null pointer exception in the PlayerAI
		Thread.sleep(1000)
		
		
		// run, meaning this returns when PlayerTurnCycler returns
		new PlayerTurnCycler(players).run()
	}
}
