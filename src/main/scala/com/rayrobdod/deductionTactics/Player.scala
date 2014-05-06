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
 * A Deduction Tactics player
 *
 * This is more of a data structure. To control the Player, one would use a PlayerAI.
 * 
 * @author Raymond Dodge
 * @version a.5.0 - no longer relies on actors
 */
final class Player(val tokens:PlayerListOfTokens, val ai:PlayerAI)
{
	def takeTurn() = {
		startTurnReaction.foreach{a => a()}
		ai.takeTurn(this)
		endTurnReaction.foreach{a => a()}
	}
	
	val startTurnReaction:Buffer[Function0[Any]] = Buffer.empty
	def addStartTurnReaction(f:Function0[Any]) = startTurnReaction += f
	
	val endTurnReaction:Buffer[Function0[Any]] = Buffer.empty
	def addEndTurnReaction(f:Function0[Any]) = endTurnReaction += f
	
	val victoryReaction:Buffer[Function0[Any]] = Buffer.empty
	def addVictoryReaction(f:Function0[Any]) = victoryReaction += f
	
	val defeatReaction:Buffer[Function0[Any]] = Buffer.empty
	def addDefeatReaction(f:Function0[Any]) = defeatReaction += f
	
}

object Player {
	type StartTurnReactionType = Function0[Unit]
	type EndTurnReactionType = Function0[Unit]
	type VictoryReactionType = Function0[Unit]
	type DefeatReactionType = Function0[Unit]
}


/**
 * An abstract class that  Player will poll when it is taking a turn to
 * determine how to act.
 *  
 * @author Raymond Dodge
 * @version a.5.0 - no longer relies on scala-swing
 * @todo trait instead of abstract class? Could it possibly hurt?
 */
abstract class PlayerAI
{
	/** Generates a team of tokens that this AI would like to use. */
	def buildTeam:Seq[TokenClass]
	
	/**
	 * The engine calls this to make a player take its turn.
	 * The turn ends when this method returns to its caller.
	 */
	def takeTurn(player:Player):Any
	
	/**
	 * called once at the start of the game to allow the
	 * AI to set up additional listeners or setup an IO
	 * or other such tasks.
	 */
	def initialize(player:Player, field:Field[SpaceClass]):Any
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
	
	final def teamSize:Int = 5
}
