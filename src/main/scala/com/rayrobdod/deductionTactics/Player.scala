/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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

import scala.collection.immutable.Seq



/**
 * An abstract class that  Player will poll when it is taking a turn to
 * determine how to act.
 * 
 * @version a.6.0
 * @todo trait instead of abstract class? Could it possibly hurt?
 */
abstract class PlayerAI {
	
	/**
	 * Selects a set of classes from all avaliable classes
	 * 
	 * @since a.6.0
	 * @param maxResultSize the maximum allowed size of the return value
	 */
	def selectTokenClasses(maxResultSize:Int):Seq[TokenClass]
	
	/**
	 * Selects a set of classes from selectedTokenClasses
	 *
	 * @since a.6.0
	 * @param selectedTokenClasses the token classes from which the return value may contain
	 * @param maxResultSize the maximum allowed size of the return value
	 * @param otherPlayersSelectedClasses other player's token classes
	 * @return a subset of selectedTokenClasses
	 */
	def narrowTokenClasses(
			selectedClasses:Seq[Seq[TokenClass]],
			maxResultSize:Int,
			myIndexInSelectedClasses:Int
	):Seq[TokenClass]
	
	/**
	 * The engine calls this to make a player take its turn.
	 * The turn ends when this method returns to its caller.
	 * @version a.6.0
	 */
	def takeTurn(player:Int, gameState:GameState, memo:ai.Memo):Seq[GameState.Action]
	
	/**
	 * Notification of what another player did on its turn.
	 * 
	 * @since a.6.0
	 * @param player the player who took an action
	 * @param action the Action taken
	 * @param beforeState the state before the action was taken
	 * @param afterState the state after the action was taken
	 * @param memo the memo
	 */
	def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:ai.Memo
	):ai.Memo
	
	/**
	 * called once at the start of the game to allow the
	 * AI to set up additional listeners or setup an IO
	 * or other such tasks.
	 * 
	 * @version a.6.0
	 */
	def initialize(player:Int, initialState:GameState):ai.Memo
}

/**
 * Provides a list of PlayerAI
 * 
 * There are two types of PlayerAI.
 */
object PlayerAI {
	
	val basePlayerAIs:Seq[PlayerAI] = Seq(
		/// Interfaces the player can use
		new com.rayrobdod.deductionTactics.ai.SwingInterface,
		new com.rayrobdod.deductionTactics.ai.ConsoleInterface,
		
		/// Computer opponents
		new com.rayrobdod.deductionTactics.ai.BlindAttackAI,
		new com.rayrobdod.deductionTactics.ai.SleepAbuserAI,
		new com.rayrobdod.deductionTactics.ai.FieldPotentialAI
		
		/// A network client that recieves commands over the net
		
		/// cheating computer opponents; basically things that can exploit known bugs
	)
	
	val decoratorPlayerAIs:Seq[PlayerAI => PlayerAI] = Seq(
		/// Helpers
		{base:PlayerAI => new com.rayrobdod.deductionTactics.ai.WithAutorecord(base)},
		
		/// Team Choosing algorithm overrides
		{base:PlayerAI => new com.rayrobdod.deductionTactics.ai.WithRandomTeam(base)},
		{base:PlayerAI => new com.rayrobdod.deductionTactics.ai.WithArbitraryTeam(base)},
		
		/// user interface stuff
		{base:PlayerAI => new com.rayrobdod.deductionTactics.ai.WithSound(base)},
		{base:PlayerAI => new com.rayrobdod.deductionTactics.ai.WithSwingViewport(base)},
		{base:PlayerAI => new com.rayrobdod.deductionTactics.ai.WithConsoleEventPrinting(base)}
	)
}
