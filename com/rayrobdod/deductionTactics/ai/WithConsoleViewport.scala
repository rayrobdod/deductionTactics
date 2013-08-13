package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{RectangularField => Field, StartOfTurn, EndOfTurn}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event

import com.rayrobdod.deductionTactics.consoleView.{
			BoardPrinter,
			TokenEventPrinter
}

/**
 * A decorator for PlayerAIs. It provides a viewport to a player
 * that might not provide one itself: useful for observing Computer
 * only matches.
 *
 * @author Raymond Dodge
 * @version 10 Aug 2012
 */
final class WithConsoleViewport(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** Forwards command to base */
	def buildTeam = base.buildTeam
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	def initialize(player:Player, field:Field) = {
		base.initialize(player, field)
		
		val boardPrinter = new BoardPrinter(player.tokens, field)
		player.reactions += new Reaction {
			def apply(e:Event) = {boardPrinter.printField()}
			
			def isDefinedAt(e:Event) = {e match {
				case StartOfTurn => true
				case EndOfTurn => true
				case _ => false
			}}
		}
		player.tokens.tokens.flatten.foreach{(token:Token) =>
			token.reactions += new TokenEventPrinter(token, player.tokens)
		}
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithConsoleViewport]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithConsoleViewport].canEquals(this) &&
				this.base == other.asInstanceOf[WithConsoleViewport].base
	}
	override def hashCode = base.hashCode * 7 + 31
	
	override def toString = this.getClass.getName
}
