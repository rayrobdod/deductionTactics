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

import com.rayrobdod.boardGame.{Space, RectangularField => Field}
import javax.swing.JFrame
import com.rayrobdod.deductionTactics.swingView.{BoardGamePanel, MenuBar, HighlightMovableSpacesLayer}

/**
 * A decorator for PlayerAIs. It provides a viewport to a player
 * that might not provide one itself: useful for observing Computer
 * only matches.
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithSwingViewport(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	override def takeTurn(player:Int, gameState:GameState, memo:Memo) = {
		base.takeTurn(player, gameState, memo.asInstanceOf[SwingInterfaceMemo].base)
		
	}
	/** Forwards command to base */
	override def buildTeam(size:Int) = base.buildTeam(size)
	
	
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	def initialize(player:Int, initialState:GameState):Memo =
	{
		val tokens = initialState.tokens
		val panel = new BoardGamePanel(tokens, player, initialState.board)
		val frame = new JFrame("Deduction Tactics")		
		frame.setJMenuBar(new MenuBar)
		frame.getContentPane add panel
		
		val activeToken = new swingView.SharedActiveTokenProperty()
		activeToken.value = None
		
		val hilightLayer = new HighlightMovableSpacesLayer(panel.centerpiece)
		panel.centerpiece.add(hilightLayer, 0)
		
		
		val tokensProp = new swingView.ListOfTokensProperty
		tokensProp.value = initialState.tokens
		
		
		
		
		
		frame.pack()
		frame.validate()
		frame.setVisible(true)
		
		SwingInterfaceMemo(
				base = base.initialize(player, initialState), 
				panel = panel,
				hilightLayer = hilightLayer,
				attackTypeSelector = new swingView.SellectAttackTypePanel(),
				selectedToken = activeToken,
				currentTokens = tokensProp,
				endOfTurnButton = new javax.swing.JButton("XXXX")
		)
	}
	
	
	
	/**  */
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = {
		val memo2 = memo.asInstanceOf[SwingInterfaceMemo]
		val newMemoBase = base.notifyTurn(player, action, beforeState, afterState, memo2.base)
		val panel = memo2.panel
		
		action match {
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
		
		
		memo2.selectedToken.value = {
			// this assumes that the board doesn't change.
			val space = memo2.selectedToken.value.map{_.currentSpace}
			afterState.tokens.tokens.flatten.find{_.currentSpace == space}
		}
		memo2.currentTokens.value = afterState.tokens
		memo2.hilightLayer.update(memo2.selectedToken.value, afterState.tokens, afterState.board)
		
		new SwingInterfaceMemo(
			newMemoBase,
			memo2.panel,
			memo2.hilightLayer,
			memo2.attackTypeSelector,
			memo2.selectedToken,
			memo2.currentTokens,
			memo2.endOfTurnButton
		)
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithSwingViewport]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithSwingViewport].canEquals(this) &&
				this.base == other.asInstanceOf[WithSwingViewport].base
	}
	override def hashCode = base.hashCode * 7 + 23
	
	override def toString = base.toString + " with " + this.getClass.getName
}
