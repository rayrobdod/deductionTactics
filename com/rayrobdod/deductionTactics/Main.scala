package com.rayrobdod.deductionTactics
package main

import view._
import test.UnselectOtherTokens
import java.awt.BorderLayout
import java.awt.BorderLayout.{SOUTH => borderSouth}
import scala.swing.Swing.ActionListener
import scala.collection.immutable.Seq
import javax.swing.{JFrame, JButton, JPanel, JCheckBox}
import java.awt.event.ActionEvent
import com.rayrobdod.boardGame.{Moved}

/**
 * @author Raymond Dodge
 * @version 2012 Apr 20
 * @version 30 May 2012 - moving the tokens' moving to their starting spaces
 		until after all players are initialized
 * @version 05 Jun 2012 - adding a sleep after tokens move in hopes that they
 		are done moving by the time the sleep ends
 * @version 28 Jun 2012 - adding a few default buttons to the startNewGame frame
 * @version 12 Jul 2012 - removing viewpoint choosers, since PlayerAI decorators can do that now
 */
object Main extends App
{
	this.startNewGame
	
	def startNewGame
	{
		val okButton = new JButton("OK")
		val aiChooser = new ChooseAIsComponent()
		
		val aiChooserFrame = new JFrame("Choose PlayerTypes")
		aiChooserFrame.getContentPane.add(aiChooser)
		aiChooserFrame.getContentPane.add(okButton, borderSouth)
		aiChooserFrame.getRootPane.setDefaultButton(okButton);

		
		okButton.addActionListener(ActionListener{(e:ActionEvent) =>
			aiChooserFrame.setVisible(false)
			new Thread(new Runnable{
				def run = buildTeams(aiChooser.getAIs)
			}, "build teams").start()
		})
		
		aiChooserFrame.pack()
		aiChooserFrame.setVisible(true)
	}
	
	private def buildTeams(ais:Seq[PlayerAI])
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
		
		val field = generateField
				
		val players = playerListOfTokens.map(new Player(_))
		ais(0).initialize(players(0), field)
		ais(1).initialize(players(1), field)
		
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
		
		// TODO: make dynamic and map based
		canonTokens(0)(0) ! Moved(field.space(1,1),true)
		canonTokens(0)(1) ! Moved(field.space(1,3),true)
		canonTokens(0)(2) ! Moved(field.space(1,5),true)
		canonTokens(0)(3) ! Moved(field.space(1,7),true)
		canonTokens(0)(4) ! Moved(field.space(1,9),true)
		canonTokens(1)(0) ! Moved(field.space(8,1),true)
		canonTokens(1)(1) ! Moved(field.space(8,3),true)
		canonTokens(1)(2) ! Moved(field.space(8,5),true)
		canonTokens(1)(3) ! Moved(field.space(8,7),true)
		canonTokens(1)(4) ! Moved(field.space(8,9),true)
		
		allTokens.foreach{_.start()}
		players.foreach{_.start()}
		
		// pray that the tokens have moved by the time this sleep is over
		// TODO: Figure out what the delay is for, if anything
		Thread.sleep(1000)
		
		// run, meaning this returns when PlayerTurnCycler returns
		new PlayerTurnCycler(players.zip(ais)).run()
	}
}
