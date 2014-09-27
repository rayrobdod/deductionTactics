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

import scala.util.Random

/**
 * A decorator for PlayerAIs. It intercepts the buildTeam command and creates
 * a random one using the package randomTeam method
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithArbitraryTeam(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	override def takeTurn(player:Int, gameState:GameState, memo:Memo) = base.takeTurn(player, gameState, memo)
	/** Forwards command to base */
	override def initialize(player:Int, initialState:GameState):Memo = base.initialize(player, initialState)
	/** Forwards notify to base */
	override def notifyTurn(player:Int, action:GameState.Result, beforeState:GameState, afterState:GameState, memo:Memo):Memo =
				base.notifyTurn(player, action, beforeState, afterState, memo)
	
	/** chooses a team randomly */
	def buildTeam(size:Int) = {
		import javax.swing.JOptionPane.PLAIN_MESSAGE
		
		val pane = new javax.swing.JOptionPane;
		pane.setWantsInput(true);
		pane.setMessage("Choose a RNG seed");
		pane.setInitialSelectionValue(Random.nextInt);
		pane.selectInitialValue() 
		pane.setMessageType(PLAIN_MESSAGE);
		val dialog = pane.createDialog("WithArbitraryTeam")
		dialog.setVisible(true);
		val input = pane.getInputValue.hashCode
		
		ai.randomTeam(size, new Random(input))
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithArbitraryTeam]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithArbitraryTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithArbitraryTeam].base
	}
	override def hashCode = base.hashCode * 7 + 23
	
	override def toString = base.toString + " with " + this.getClass.getName
}
