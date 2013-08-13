package com.rayrobdod.boardGame

import com.rayrobdod.math.distribution.{FiniteDiscreeteProbabilityDistribution, 
		BinomialDistribution => B, DiscreteUniformDistribution => U}
import scala.collection.immutable.Set
import scala.swing.event.Event
import scala.swing.Reactions
import scala.actors.{Actor, DaemonActor}
import java.util.logging.{Logger, Level, ConsoleHandler}

case object RollDie extends Event

/**
 * A die. Is given a probability distribution. Can be rolled 
 * with a {@link RollDie} event.
 * @author Raymond Dodge
 * @version ?? May 2011
 * @version 14 Jun 2011 - rewrote #act to use a {@link Reactions}
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 */
abstract class Die extends DaemonActor
{ 
	def probabilityDistribution:FiniteDiscreeteProbabilityDistribution
	
	/** A list of things this can do in reponse to recieving an Event in a #! */
	var action:PartialFunction[Event, Int] = QuickReplyRollDieReaction
	
	override final def act()
	{
		loop { receive
		{
			case x:Event => 
			{
				if (action.isDefinedAt(x)) {reply(action(x))} else {}
			}
		}}
	}
	
	/**
	 * A reaction that replies to a RollDie reaction. Is returns a random value
	 */
	object QuickReplyRollDieReaction extends PartialFunction[Event, Int]
	{
		override def apply(e:Event) = {e match
		{
			case RollDie => probabilityDistribution.rand().floor.intValue
			case _ =>  throw new IllegalArgumentException("Event not a RollDie event")
		}}
		
		override def isDefinedAt(e:Event) = {e match
		{
			case RollDie => true
			case _ => false
		}}
	}
	
	override def toString =
	{
		this.getClass.getName + " [distribution: " +  probabilityDistribution + ", action: " + action + "]"
	}
}

trait OneToSizeValues extends Die
{
	def size:Int
}

trait FixedLow extends OneToSizeValues
{
	final override def probabilityDistribution() = B(size,0)
}

trait FixedHigh extends OneToSizeValues
{
	final override def probabilityDistribution() = B(size,1)
}

trait ProbablyLow extends OneToSizeValues
{
	final override def probabilityDistribution() = B(size, 1./3)
}

trait ProbablyHigh extends OneToSizeValues
{
	final override def probabilityDistribution() = B(size, 2/3)
}

trait Unfixed extends OneToSizeValues
{
	final override def probabilityDistribution() = U(1 to size)
}

object DiceSetLoggerInitializer
{
	val warningConsoleHander = new ConsoleHandler()
	warningConsoleHander.setLevel(Level.WARNING)
	
	val finerConsoleHander = new ConsoleHandler()
	finerConsoleHander.setLevel(Level.FINER)
	
	val linkedLogger = Logger.getLogger("net.verizon.rayrobdod.boardGame.LinkedComponent")
	linkedLogger.addHandler(finerConsoleHander)
	linkedLogger.setLevel(Level.WARNING)
}
