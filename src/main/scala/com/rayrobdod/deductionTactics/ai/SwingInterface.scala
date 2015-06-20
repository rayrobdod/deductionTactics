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
package ai

import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{Space}
import com.rayrobdod.deductionTactics.{PlayerAI, Token}
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JButton, JFrame, JPanel}
import java.awt.BorderLayout

import com.rayrobdod.deductionTactics.swingView.chooseTokenClasses
import com.rayrobdod.deductionTactics.swingView.narrowTokenClasses
import com.rayrobdod.deductionTactics.swingView.game

import com.rayrobdod.deductionTactics.swingView.{
			MenuBar,
			SellectAttackTypePanel,
			MoveTokenMouseListener,
			InputFrame
}

/**
 * An instance of the PlayerAI service. Needs to be a class, since it
 * needs to be instatible. However, as all instanes are identical,
 * they are are equal to each other
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class SwingInterface extends PlayerAI
{
	val endOfTurnLock = new Object();
	var takeTurnReturnValue:Option[GameState.Action] = None
	
	override def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
		
		val a = memo.asInstanceOf[SwingInterfaceMemo]
		a.currentTokens.value = gameState.tokens
		a.endOfTurnButton.setEnabled(true)
		
		return endOfTurnLock.synchronized{
			while (takeTurnReturnValue == None) { 
				endOfTurnLock.wait()
			}
			
			val retVal = takeTurnReturnValue.get
			takeTurnReturnValue = None
			Seq(retVal)
		}
	}
	
	def initialize(player:Int, initialState:GameState):Memo =
	{
		val tokens = initialState.tokens
		val viewmodel = new game.Top(tokens, player, initialState.board)
		viewmodel.setVisible(true)
		
		val attackTypeSelector = new SellectAttackTypePanel()
		val activeToken = new swingView.SharedActiveTokenProperty()
		activeToken.value = None
		
		def writeGameAction = {(x:GameState.Action) => 
			endOfTurnLock.synchronized {
				takeTurnReturnValue = Option(x)
				endOfTurnLock.notifyAll
			}
		}
		
		val tokensProp = new swingView.ListOfTokensProperty
		tokensProp.value = initialState.tokens
		
		
		
		val endOfTurnButton = new JButton("End Turn")
		endOfTurnButton.addActionListener(new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				endOfTurnLock.synchronized{
					endOfTurnButton.setEnabled(false)
					takeTurnReturnValue = Some(GameState.EndOfTurn)
					endOfTurnLock.notifyAll()
				}
			}
		})
		
		
		
		
		SwingInterfaceMemo(
				base = new SimpleMemo,
				panel = viewmodel,
				attackTypeSelector = attackTypeSelector,
				selectedToken = activeToken,
				currentTokens = tokensProp,
				endOfTurnButton = endOfTurnButton
		)
	}
	
	override def selectTokenClasses(maxSize:Int):Seq[TokenClass] = {
		val buildingLock = new Object()
		val teamBuilder = new chooseTokenClasses.Top(maxSize)
		
		teamBuilder.addNextActionListener(new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized
		{
			teamBuilder.show()
			buildingLock.wait
		}
		
		teamBuilder.results
	}
	
	override def narrowTokenClasses(
				otherPlayersSelectedClasses:Seq[Seq[TokenClass]],
				maxResultSize:Int,
				index:Int
	):Seq[TokenClass] = {
		val buildingLock = new Object()
		val teamBuilder = new swingView.narrowTokenClasses.Top(index, otherPlayersSelectedClasses, maxResultSize)
		
		teamBuilder.addNextActionListener(new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized
		{
			teamBuilder.show()
			buildingLock.wait
		}
		
		teamBuilder.results
	}
	
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = {
		val memo2 = memo.asInstanceOf[SwingInterfaceMemo]
		val panel = memo2.panel
		
/*		action match {
			case GameState.TokenMoveResult(index, s) =>
				val tokenComp = panel.tokenComps(index)
				tokenComp.moveToSpace(s)
				
			case GameState.TokenAttackDamageResult(a, d, e, k) =>
				val tokenComp = panel.tokenComps(d)
				tokenComp.beAttacked(e,k)
				panel.resetTokenPanels(afterState.tokens)
				System.out.println("Token was attacked")
				
				None
			case GameState.TokenAttackStatusResult(a, d, s) =>
				val tokenComp = panel.tokenComps(d)
				tokenComp.beAttacked(s)
				panel.resetTokenPanels(afterState.tokens)
				// TODO
				None
			case GameState.EndOfTurn =>
				None
		}
*/		
		
		memo2.selectedToken.value = {
			// this assumes that the board doesn't change.
			val space = memo2.selectedToken.value.map{_.currentSpace}
			afterState.tokens.tokens.flatten.find{x => Option(x.currentSpace) == space}
		}
		memo2.currentTokens.value = afterState.tokens
		
		memo
	}
	
	// hopefully, animations will work eventually and that will
	// inform a player of what's going on.
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[SwingInterface]}
	override def equals(other:Any):Boolean = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[SwingInterface].canEquals(this)
	}
	// arbitrary number (13)
	override def hashCode:Int = 13
	
	override def toString:String = this.getClass.getName
}

final case class SwingInterfaceMemo (
	base:Memo,
	panel:game.Top,
	attackTypeSelector:SellectAttackTypePanel,
	selectedToken:swingView.SharedActiveTokenProperty,
	currentTokens:swingView.ListOfTokensProperty,
	endOfTurnButton:JButton
) extends Memo {
	override def attacks:Seq[GameState.Result] = base.attacks
	override def suspisions:Map[(Int, Int), TokenClassSuspision] = base.suspisions
	
	override def addAttack(r:GameState.Result):SwingInterfaceMemo =
			this.copy(base.addAttack(r))
	override def updateSuspision(key:(Int, Int), value:TokenClassSuspision):SwingInterfaceMemo =
			this.copy(base.updateSuspision(key, value))
}
