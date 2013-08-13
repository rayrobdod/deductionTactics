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
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 19 Jan 2012 - changing parameters from Seq[Token] to PlayerListOfTokens
 * @version 27 Jan 2012 - added ForwardAttackObservations
 * @version 12 Feb 2012 - made forward EndOfTurn to tokens too
 * @version 20 Mar 2012 - modified reactions for new event model
 * @version 01 Jun 2012 - disabled the debug version of the Reactions thing
 * @version 2013 Aug 07 - complete rewrite to remove the Actor aspect
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
 * An abstract class that provides an interface for ais to play this game.
 * Because that was easiest, this is a Reaction.
 * 
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 23 Aug 2011 - added buildTeam, and nuked TeamBuilder class
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 27 Jan 2012 - added observeAttack 
 * @version 06 Feb 2012 - added prepareIO 
 * @version 27 Feb 2012 - adding observeStatusAttack
 * @version 20 Mar 2012 - modified reactions for new event model
 * @version 2013 Jun 07 - no longer implements scala.swing.Reactions.Reaction
 */
abstract class PlayerAI
{
	/** create a team of tokens */
	def buildTeam:Seq[CannonicalTokenClass]
	/**
	 * Called once at the beginning of each turn.
	 *  
	 * @post the last command is {@code player ! EndTurn}
	 */
	def takeTurn(player:Player):Any
	/**
	 * called once at the start of the game to allow the
	 * AI to set up additional listeners or setup an IO
	 * or other such tasks.
	 */
	def initialize(player:Player, field:Field):Any
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
 * @version 21 Aug 2011
 * @version 24 Aug 2011 made service loader refer to class, not companion object
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 20 Mar 2012 - added StandardObserveAttacks, to move out of the PlayerAI class
 * @version 25 Mar 2012 - moved StandardObserveAttacks to com.rayrobdod.deductionTactics.ai
 * @version 10 Jul 2012 - replacing long Class.forName("com. ... .PlayerAI").asInstanceOf[Class[PlayerAI]] with classOf[PlayerAI]
 * @version 12 Jul 2012 - renaming serviceLoader and serviceSeq to baseServiceLoader and baseServiceSeq;
 			adding decoratorServiceLoader and decoratorServiceSeq
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
