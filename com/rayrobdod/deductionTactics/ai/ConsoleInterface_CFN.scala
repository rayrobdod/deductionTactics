package com.rayrobdod.deductionTactics
package ai

import scala.collection.immutable.Seq
import scala.collection.mutable.{Map => MMap}
import com.rayrobdod.boardGame.{StartOfTurn, EndOfTurn, Moved, Space}
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import java.awt.BorderLayout
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import com.rayrobdod.commonFunctionNotation.Parser.{parse => cfnParse}
import java.text.ParseException
import com.rayrobdod.deductionTactics.ai.ConsoleInterface.prompt

import com.rayrobdod.deductionTactics.consoleView.{
			InfoPrinter,
			BoardPrinter,
			TokenEventPrinter
}

/**
 *
 * @author Raymond Dodge
 * @version 26 Dec 2012
 */
final class ConsoleInterface_CFN extends PlayerAI
{
	private val in = new java.util.Scanner(System.in);
	private val out = System.out;
	private val fields:MMap[Player, Field] = MMap.empty;
	
	def takeTurn(player:Player) {
		val field = fields(player);
		
		var nextCommand:Any = 0;
		while (nextCommand != EndOfTurn) {
			try {
				val functions = ConsoleInterface_CFN.cfnFunctions(field, player);
				
				// artifical delays so that prompt is printed after other information
				// I do not know how to make this lock-based.
				Thread.sleep(100);
				out print prompt;
				
				val nextCommandString = in.nextLine().trim
				nextCommand = cfnParse(nextCommandString, functions)
				
				player ! nextCommand
				
			} catch {
				case e:ParseException => { out.print("Command not understood") }
				case e:ClassCastException => {
					out.print("Cannot perform that action with that item: ")
					out.print(e.getMessage)
				}
			}
		}
	}
	
	def initialize(player:Player, field:Field)
	{
		fields += ((player, field))
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
	
	def canEquals(other:Any) = {other.isInstanceOf[ConsoleInterface_CFN]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[ConsoleInterface_CFN].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 13
	
	override def toString = this.getClass.getName
}

object ConsoleInterface_CFN {
	def cfnFunctions(field:Field, player:Player) =
		NetworkClient.cfnFunctions(field, player) ++ Map(
			"Exit" -> {() => System.exit(0);}
		);
}
