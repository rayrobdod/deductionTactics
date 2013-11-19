package com.rayrobdod.deductionTactics
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
package consoleView

import com.rayrobdod.boardGame.{RectangularField, Space, SpaceClass,
		SpaceClassConstructor, mapValuesFromObjectNameToSpaceClassConstructor}
import java.io.PrintStream
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser

/**
 * 
 * @author Raymond Dodge
 * @version a.5.1
 */
object BoardPrinter{
	private def spaceToString(a:SpaceClass) = a match {
		case NoStandOnSpaceClass()      => scala.Console.BLUE_B
		case ImpassibleSpaceClass()     => scala.Console.YELLOW_B
		case AttackableOnlySpaceClass() => scala.Console.GREEN_B
		case FireRestrictedSpaceClass() => scala.Console.RED_B
		case _ => scala.Console.BLACK_B
	}
	
	def spaceStrings(tokens:ListOfTokens, field:RectangularField, cursor:Option[Space] = None, selected:Option[Token] = None):Seq[Seq[String]] = {
		field.spaces.map{_.map{(space:Space) =>
			val tokenOnSpace = tokens.aliveTokens.flatten.filter{_.currentSpace == space}.headOption
			
			val spaceClassColor = spaceToString(space.typeOfSpace)
			val tokenString = tokenOnSpace.map{tokensToLetters(tokens)}.getOrElse{' '}
			val cursorColor = cursor.filter{_ == space}.map{x => scala.Console.BLINK}.getOrElse("\u001b[25m") // blink off
			val tokenColor = if (tokenOnSpace == selected && tokenOnSpace != None) {scala.Console.BOLD} else {"\u001b[21m"}
			
			cursorColor + spaceClassColor + tokenColor + tokenString
		} :+ "\n"}
	}
	
	// I'd prefer to use the top line, but the consoles are ASCII only
	//val (tl, tr, bl, br, horiz, vert) = ('┏', '┓', '┗', '┛', '━', '┃')
	private val (tl, tr, bl, br, horiz, vert) = (',', '.', '`', '\'', '-', '|')

	def apply(out:PrintStream, tokens:ListOfTokens, field:RectangularField, cursor:Option[Space] = None, selected:Option[Token] = None) {
		val strings = spaceStrings(tokens, field, cursor, selected)
		
		strings.flatten.foreach{ x => System.out.print( x ) }
		out.println( scala.Console.RESET )
	}
}

