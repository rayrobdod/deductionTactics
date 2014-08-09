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

import com.rayrobdod.boardGame.{RectangularField => Field, Space}
import scala.collection.immutable.Seq
import scala.collection.mutable.{Map, Buffer}
import scala.collection.JavaConversions.iterableAsScalaIterable
import java.util.ServiceLoader
import com.rayrobdod.util.services.ClassServiceLoader



/**
 * An abstract class that  Player will poll when it is taking a turn to
 * determine how to act.
 *  
 * @author Raymond Dodge
 * @version a.6.0
 * @todo trait instead of abstract class? Could it possibly hurt?
 */
abstract class PlayerAI {
	type Memo >: Any
	
	
	/** Generates a team of tokens that this AI would like to use. */
	def buildTeam(size:Int):Seq[TokenClass]
	
	/**
	 * The engine calls this to make a player take its turn.
	 * The turn ends when this method returns to its caller.
	 */
	def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action]
	
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
		memo:Memo
	):Memo
	
	/**
	 * called once at the start of the game to allow the
	 * AI to set up additional listeners or setup an IO
	 * or other such tasks.
	 */
	def initialize(player:Int, initialState:GameState):Memo
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
 * 
 * @author Raymond Dodge
 */
object PlayerAI
{
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
