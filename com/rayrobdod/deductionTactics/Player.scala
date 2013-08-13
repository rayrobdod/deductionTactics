package com.rayrobdod.deductionTactics

import scala.actors.Actor
import scala.swing.Reactions
import scala.swing.event.Event
import com.rayrobdod.boardGame.{StartOfTurn, EndOfTurn, RectangularField => Field}
import scala.collection.immutable.Seq
import scala.collection.mutable.Map
import scala.collection.JavaConversions.iterableAsScalaIterable
import java.util.ServiceLoader

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
 * * @version 20 Mar 2012 - modified reactions for new event model
 */
final class Player(val tokens:PlayerListOfTokens) extends Actor
{
	override def act()
	{
		loop { react {
			case x:Event => reactions(x)
		}}
	}
	
	/** A list of things this can do in reponse to recieving an Event in a #! */
//	val reactions:Reactions = new Reactions.Impl
	val reactions:Reactions = new com.rayrobdod.tmp.ReactionsWithToString
	reactions.+=(TurnStartReaction)
	object TurnStartReaction extends Reactions.Reaction
	{
		override def apply(e:Event) = {e match {
			case StartOfTurn => {
				Player.this ! DoAI(Player.this)
			}
		}}
		override def isDefinedAt(e:Event) = {e match {
			case StartOfTurn => true
			case EndOfTurn => false
			case _ => false
		}}
	}
}

/**
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 */
case class DoAI(player:Player) extends Event
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
 */
abstract class PlayerAI extends Reactions.Reaction
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
	
	final override def apply(e:Event) = {e match {
		case DoAI(x) => this.takeTurn(x)
		case _ => {}
	}}
	final override def isDefinedAt(e:Event) = {e match {
		case DoAI(_) => true
		case _ => false
	}}
}

/**
 * A service provider for getting player AIs
 * 
 * This uses (a) file(s) in the classpath at
 * "/META-INF/services/com.rayrobdod.deductionTactics.PlayerAI" that
 * contains lists of classes that extends [[com.rayrobdod.deductionTactics.PlayerAI]].
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 24 Aug 2011 made service loader refer to class, not companion object
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 20 Mar 2012 - added StandardObserveAttacks, to move out of the PlayerAI class
 * @version 25 Mar 2012 - moved StandardObserveAttacks to com.rayrobdod.deductionTactics.ai
 */
object PlayerAI
{
	/** A service loader that lists the known PlayerAIs */
	val serviceLoader = ServiceLoader.load[PlayerAI](
			Class.forName("com.rayrobdod.deductionTactics.PlayerAI").asInstanceOf[Class[PlayerAI]])
	
	/** The values from the serviceLoader, turned into a Seq for convenience */
	def serviceSeq:Seq[PlayerAI] = {
		Seq.empty ++ iterableAsScalaIterable(serviceLoader)
	}
	
	final def teamSize:Int = 5
}
