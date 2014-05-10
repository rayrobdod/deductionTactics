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
package com.rayrobdod.deductionTactics.ai

import scala.collection.immutable.Seq
import scala.collection.mutable.{Map => MMap}
import com.rayrobdod.boardGame.{Space}
import com.rayrobdod.deductionTactics.{PlayerAI, Token}
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
 * @version a.5.0
 */
final class SwingInterface extends PlayerAI
{
	val endOfTurnLock = new Object();
	
	def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
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
		
		tokens.otherTokens.flatten.foreach{(x:Token) =>
			val reaction = new HighlightMovableSpacesReaction(x, panel, player.tokens);
			x.selectedReactions_+=(reaction)
			x.moveReactions_+={(x,y) => reaction()}
		}
		tokens.myTokens.foreach{(x:Token) =>
			val reaction = new HighlightMovableSpacesReaction(x, panel, player.tokens);
			x.selectedReactions_+=(reaction)
			x.moveReactions_+={(x,y) => reaction()}
			
			x.tryDamageAttackedReactions_+={(x) => reaction()}
			x.tryStatusAttackedReactions_+={(x) => reaction()}
			
			player.addEndTurnReaction(reaction)
			player.addStartTurnReaction(reaction)
		}
		field.spaces.flatten.foreach{(s:RectangularSpace) => 
			panel.centerpiece.addMouseListenerToSpace(s, new SelectTokenOnSpaceMouseListener(s, player.tokens))
			panel.centerpiece.addMouseListenerToSpace(s, new MoveTokenMouseListener(player, s, attackTypeSelector))
		}
		
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
