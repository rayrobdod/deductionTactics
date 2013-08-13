package com.rayrobdod.boardGame

import javax.swing.JPanel
import scala.swing.Panel
import scala.collection.immutable.Set
import com.rayrobdod.boardGame.{Token => BoardGameToken}
import scala.collection.immutable.LinearSeq
import scala.collection.LinearSeqOptimized

/**
 * Noticed that the UnaryMovement Spaces look like linked list nodes
 * So, I made a linked list around the UnaryMovement Spaces.
 * 
 * I haven't grown out of my habit of making useless collections yet…
 * 
 * @author Raymond Dodge
 * @version 09 May 2011
 * @version 14 Jun 2011 - made parametric
 * @version 14 Jun 2011 - fixed isEmpty to report true on empty, not false on empty
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 */
class UnaryMovementSpaceSeq[A <: UnaryMovement](override val headOption:Option[A])
			extends LinearSeq[A] 
{
	import UnaryMovementSpaceSeqLoggerInitializer.{unaryLogger => logger}
	logger.entering("UnaryMovementSpaceSeq", "this(Option[A])", headOption)
	
	def this(head:A) = this(Option(head))
	
	override def head = headOption.get
	override def isEmpty = (headOption == None) // assume no nulls, right?
	override def tail:UnaryMovementSpaceSeq[A] = // TRYTHIS test to see if it is worth making this a lazy val: Infinite Seq is probably possible (e.g. Monopoly)
	{
		logger.entering("UnaryMovementSpaceSeq", "tail()")
		logger.finer(this.head.toString)
		
		val next:Option[Space] = headOption.flatMap(_.nextSpace)
		val nextAsUnary:Option[A] = next.filter(_.isInstanceOf[A]).asInstanceOf[Option[A]]
		
		new UnaryMovementSpaceSeq[A](nextAsUnary)
	}
	
	override def apply(i:Int):A = 
	{
		if (this.isEmpty) throw new IndexOutOfBoundsException("Too high an index called")
		else if (i < 0) throw new IndexOutOfBoundsException("index less than zero called: " + i)
		else if (i == 0) return head
		else return tail.apply(i - 1)
	}
	
	override def length:Int =
	{
		if (this.isEmpty) return 0
		else return (tail.length + 1)
	}
}

private[boardGame] object UnaryMovementSpaceSeqLoggerInitializer
{
	import java.util.logging.{Logger, Level, ConsoleHandler}
	
	val warningConsoleHander = new ConsoleHandler()
	warningConsoleHander.setLevel(Level.WARNING)
	
	val finerConsoleHander = new ConsoleHandler()
	finerConsoleHander.setLevel(Level.FINER)
	
	val unaryLogger = Logger.getLogger("net.verizon.rayrobdod.boardGame.UnaryMovementSpaceSeq")
	unaryLogger.addHandler(finerConsoleHander)
	unaryLogger.setLevel(Level.WARNING)
}
