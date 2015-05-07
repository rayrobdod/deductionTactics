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

import scala.collection.immutable.Seq
import scala.collection.JavaConversions.iterableAsScalaIterable



/**
 * An abstract class that  Player will poll when it is taking a turn to
 * determine how to act.
 * 
 * @version a.6.0
 * @todo trait instead of abstract class? Could it possibly hurt?
 */
abstract class PlayerAI {
	
	/** Selects a set of classes from selectedTokenClasses */
	def selectTokenClasses(maxResultSize:Int):Seq[TokenClass]
	
	/**
	 * Selects a set of classes from selectedTokenClasses
	 *
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
	 */
	def takeTurn(player:Int, gameState:GameState, memo:ai.Memo):Seq[GameState.Action]
	
	/**
	 * Notification of what another player did on its turn.
	 * 
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
	 */
	def initialize(player:Int, initialState:GameState):ai.Memo
}

/**
 * A service provider for getting player AIs
 * 
 * There are two types of PlayerAI.
 
 * The bases are listed in (a) file(s) in the classpath at
 * "/META-INF/services/com.rayrobdod.deductionTactics.PlayerAI".
 * The bases extends PlayerAI and have a no-arg constructor
 
 * The decorators are listed in (a) file(s) in the classpath at
 * "/META-INF/services/com.rayrobdod.deductionTactics.PlayerAIDecorator".
 * The bases extends PlayerAI and have a one PlayerAI arg constructor
 */
object PlayerAI
{
	import java.util.ServiceLoader
	import com.rayrobdod.util.services.ClassServiceLoader
	
	/** A service loader that lists the known PlayerAIs */
	val baseServiceLoader = ServiceLoader.load[PlayerAI](classOf[PlayerAI])
	
	/** The values from the serviceLoader, turned into a Seq for convenience */
	def baseServiceSeq:Seq[PlayerAI] = {
		Seq.empty ++ iterableAsScalaIterable(baseServiceLoader)
	}
	
	/** A service loader that lists the known PlayerAI decorators */
	val decoratorServiceLoader = new ClassServiceLoader[PlayerAI](classOf[PlayerAI], "com.rayrobdod.deductionTactics.PlayerAIDecorator")
	
	def decoratorServiceSeq:Seq[Class[_ <: PlayerAI]] = {
		Seq.empty ++ iterableAsScalaIterable(decoratorServiceLoader)
	}
}
