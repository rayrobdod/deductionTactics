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
import com.rayrobdod.deductionTactics.ai.ConsoleInterface.prompt

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
		out print prompt;
		var line = in.nextLine().trim;
		
		while (line != "END TURN") {
			if (line == "EXIT") {System.exit(0);}
			
			try {
				val command = commandParser.parseCommand(line)
				player ! command
			} catch {
				case e:IllegalArgumentException => { out.print(e.getMessage) } 
			}
			
			Thread.sleep(100);
			out print prompt;
			line = in.nextLine().trim;
		}
		
		player ! EndOfTurn;
	}
	
	def initialize(player:Player, field:Field)
	{
		commandParsers += ((player, new CommandParser(player.tokens, field)))
		player.reactions += new InfoPrinter(player.tokens, field)
		
		val boardPrinter = new BoardPrinter(player.tokens, field)
		player.reactions += new ConsoleInterface.PrintFieldAtTurnEnds(boardPrinter)
		
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

object ConsoleInterface {
	final class PrintFieldAtTurnEnds(boardPrinter:BoardPrinter) extends Reaction{
		
		def apply(e:Event) = {boardPrinter.printField()}
		
		def isDefinedAt(e:Event) = {e match {
			case StartOfTurn => true
			case EndOfTurn => true
			case _ => false
		}}
	}
	
	final val prompt = "\n> ";
}
