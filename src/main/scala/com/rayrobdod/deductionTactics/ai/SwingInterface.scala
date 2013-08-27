package com.rayrobdod.deductionTactics.ai

import scala.collection.immutable.Seq
import scala.collection.mutable.{Map => MMap}
import com.rayrobdod.boardGame.{Space}
import com.rayrobdod.deductionTactics.{PlayerAI, Player, Token}
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import java.awt.BorderLayout
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace}

import com.rayrobdod.deductionTactics.swingView.{
			BoardGamePanel,
			ShowHumanSuspicionsPanelMouseListener,
			HighlightMovableSpacesReaction,
			TeamBuilderPanel,
			MoveTokenMouseListener,
			MenuBar,
			SellectAttackTypePanel,
			SelectTokenOnSpaceMouseListener,
			InputFrame
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
 * @version 03 Jul 2012 - renamed from HumanAI to SwingInterface
 * @version 04 Aug 2012 - replacing an annonymous inner class with an instance of InputFrame
 * @version 04 Aug 2012 - failed attempt to make a victory display
 * @version 2013 Aug 07 - ripples from rewriting Player
 */
sealed class SwingInterface extends PlayerAI
{
	val playerButtons = MMap[Player, JButton]()
	val endOfTurnLock = new Object();
	
	def takeTurn(player:Player) {
		playerButtons(player).setEnabled(true)
		
		endOfTurnLock.synchronized( endOfTurnLock.wait() );
	}
	
	def initialize(player:Player, field:Field)
	{
		val tokens = player.tokens
		val panel = new BoardGamePanel(tokens, field)
		val frame = new JFrame("Deduction Tactics")		
		frame.setJMenuBar(new MenuBar)
		frame.getContentPane add panel
		
		val attackTypeSelector = new SellectAttackTypePanel()
		
		tokens.tokens.flatten.foreach{(x:Token) =>
			val reaction = new HighlightMovableSpacesReaction(x, panel, player.tokens);
			x.selectedReactions_+=(reaction)
			x.moveReactions_+=(reaction)
		}
		panel.centerpiece.spaceLabelMap.foreach({(s:RectangularSpace, c:JLabel) => 
			c.addMouseListener(new SelectTokenOnSpaceMouseListener(s, player.tokens))
			c.addMouseListener(new MoveTokenMouseListener(player, s, attackTypeSelector))
		}.tupled)
		
		val endOfTurnButton = new JButton("End Turn")
		playerButtons += ((player, endOfTurnButton))
		endOfTurnButton.addActionListener(new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				endOfTurnButton.setEnabled(false)
				endOfTurnLock.synchronized( endOfTurnLock.notifyAll() );
			}
		})
		
		player.addVictoryReaction{() =>
			// TODO: make work
			val label = new JLabel("Victor!")
			
			panel.centerpiece add label
			label.setLocation(200, 200)
			label.setFont(label.getFont.deriveFont(24f))
			panel.centerpiece.repaint()
		}
		
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
		
		val frame = new InputFrame("Choose Team", teamBuilder, new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized
		{
			frame.setVisible(true)
			buildingLock.wait
		}
		
		frame.setVisible(false)
		teamBuilder.currentSelection
	}
	
	// hopefully, animations will work eventually and that will
	// inform a player of what's going on.
	
	def canEquals(other:Any) = {other.isInstanceOf[SwingInterface]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[SwingInterface].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 13
	
	override def toString = this.getClass.getName
}
