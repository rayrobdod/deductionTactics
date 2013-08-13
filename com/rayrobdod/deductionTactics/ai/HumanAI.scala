package com.rayrobdod.deductionTactics.ai

import scala.collection.immutable.Seq
import scala.collection.mutable.{Map => MMap}
import com.rayrobdod.boardGame.{StartOfTurn, EndOfTurn, Moved, Space}
import com.rayrobdod.deductionTactics.{PlayerAI, Player, Token, RequestMove}
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import java.awt.BorderLayout
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace}

import com.rayrobdod.deductionTactics.view.{
			BoardGamePanel,
			ShowHumanSuspicionsPanelMouseListener,
			HighlightMovableSpacesReaction,
			TeamBuilderPanel,
			MoveTokenMouseListener,
			MenuBar,
			SellectAttackTypePanel,
			SelectTokenOnSpaceMouseListener
}

/**
 * An instance of the PlayerAI service. Needs to be a class, since it
 * needs to be instatible. However, as all instanes are identical,
 * they are are equal to each other
 *
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics.ai
 * @version 06 Feb 2012 - added prepareIO 
 * @version 11 Feb 2012 - playing with fieldComp manipulation in prepareIO
 * @version 27 Feb 2012 - adding observeStatusAttack
 * @version 06 Apr 2012 - incorporating SellectAttackTypePanel into initialization;
			now can choose between status and damage attacks.
 * @version 30 May 2012 - now repaints frame after modifying it
 */
sealed class HumanAI extends PlayerAI
{
	val playerButtons = MMap[Player, JButton]() 
	
	def takeTurn(player:Player)
	{
		playerButtons(player).setEnabled(true)
	}
	
	def initialize(player:Player, field:Field)
	{
		val tokens = player.tokens
		val panel = new BoardGamePanel(tokens, field)
		val frame = new JFrame("Deduction Tactics")		
		frame.setJMenuBar(new MenuBar)
		frame.getContentPane add panel
		
		tokens.tokens.flatten.foreach{(x:Token) => x ! Moved(x.currentSpace, true)}
		
		val attackTypeSelector = new SellectAttackTypePanel()

		tokens.tokens.flatten.foreach{(x:Token) => x.reactions += new HighlightMovableSpacesReaction(x, panel, player.tokens)}
		panel.fieldComp.spaceLabelMap.foreach({(s:RectangularSpace, c:JLabel) => 
			c.addMouseListener(new SelectTokenOnSpaceMouseListener(s, player.tokens))
			c.addMouseListener(new MoveTokenMouseListener(player, s, attackTypeSelector))
		}.tupled)
		
		val endOfTurnButton = new JButton("End Turn")
		playerButtons += ((player, endOfTurnButton))
		endOfTurnButton.addActionListener(scala.swing.Swing.ActionListener{(e:ActionEvent) => 
			endOfTurnButton.setEnabled(false)
			player ! EndOfTurn
		})
		
		val southPanel = new JPanel()
		southPanel.add(attackTypeSelector)
		southPanel.add(endOfTurnButton)
		frame.getContentPane.add(southPanel, BorderLayout.SOUTH)
		
		frame.pack()
		frame.validate()
		frame.setVisible(true)
	}
	
	def buildTeam = {
		val buildingLock = new Object()
		val teamBuilder = new TeamBuilderPanel()
		val okButton = new JButton("OK")
		
		val frame = new JFrame() {
			add(teamBuilder)
			add(new JPanel(){add(okButton)}, BorderLayout.SOUTH)
			setTitle("Choose Team")
			setSize(400, 600)
			setVisible(true)
		}
		
		okButton.addActionListener(new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized {buildingLock.wait}
		
		frame.setVisible(false)
		teamBuilder.currentSelection
	}
	
	// hopefully, animations will work eventually and that will
	// inform a player of what's going on.
	
	def canEquals(other:Any) = {other.isInstanceOf[HumanAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[HumanAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 13
	
	override def toString = this.getClass.getName
}

/**
 * An object that extends the class and provides no further
 * funtionality.
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics.ai
 */
object HumanAI extends HumanAI
