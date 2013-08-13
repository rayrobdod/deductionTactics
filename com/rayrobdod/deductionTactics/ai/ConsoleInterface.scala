package com.rayrobdod.deductionTactics.ai

import scala.collection.immutable.Seq
import scala.collection.mutable.{Map => MMap}
import com.rayrobdod.boardGame.{StartOfTurn, EndOfTurn, Moved, Space}
import com.rayrobdod.deductionTactics.{PlayerAI, Player, Token, RequestMove, Victory}
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import java.awt.BorderLayout
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event

import com.rayrobdod.deductionTactics.consoleView.{
		CommandParser,
			InfoPrinter,
			BoardPrinter,
			TokenEventPrinter
}

/**
 *
 * @author Raymond Dodge
 * @version 08 Dec 2012
 */
final class ConsoleInterface extends PlayerAI
{
	private val in = new java.util.Scanner(System.in);
	private val out = System.out;
	private val commandParsers:MMap[Player, CommandParser] = MMap.empty;
	
	def takeTurn(player:Player) {
		val commandParser = commandParsers(player);
		
		// artifical delays so that prompt is printed after other information
		// I do not know how to make this lock-based.
		Thread.sleep(100);
		out.print("\n> ");
		var line = in.nextLine();
		
		while (line != "END TURN") {
			if (line == "EXIT") {System.exit(0);}
			
			try {
				val command = commandParser.parseCommand(line)
				player ! command
			} catch {
				case e:IllegalArgumentException => { out.print(e.getMessage) } 
			}
			
			Thread.sleep(100);
			out.print("\n> ");
			line = in.nextLine();
		}
		
		player ! EndOfTurn;
	}
	
	def initialize(player:Player, field:Field)
	{
		commandParsers += ((player, new CommandParser(player.tokens, field)))
		player.reactions += new InfoPrinter(player.tokens, field)
		
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
	
	def buildTeam = {
		// TODO: actual prompts
		randomTeam()
	}
	
	def canEquals(other:Any) = {other.isInstanceOf[ConsoleInterface]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[ConsoleInterface].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 13
	
	override def toString = this.getClass.getName
}
