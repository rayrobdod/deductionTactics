/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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
package consoleView

import com.rayrobdod.boardGame.{RectangularSpace, RectangularIndex, RectangularField}
import java.io.PrintStream

/**
 * 
 * @author Raymond Dodge
 * @version a.6.0
 */
object BoardPrinter{
	/** @version a.5.2 */
	private def spaceToString(a:SpaceClass) = a match {
		case FlyingPassageSpaceClass()  => scala.Console.BLUE_B
		case ImpassibleSpaceClass()     => scala.Console.YELLOW_B
		case AttackOnlySpaceClass()     => scala.Console.GREEN_B
		case FirePassageSpaceClass()    => scala.Console.RED_B
		case _ => scala.Console.BLACK_B
	}
	
	/** @version a.6.0 */
	def spaceStrings(tokens:ListOfTokens, field:RectangularField[SpaceClass], team:Option[Int], cursor:Option[RectangularSpace[SpaceClass]] = None, selected:Option[TokenIndex] = None):Seq[String] = {
		field.mapIndex{x => x}.sortBy{x => (x._1 << 16) + x._2}.map{index:RectangularIndex =>
			val space:RectangularSpace[SpaceClass] = field.space(index).get
			val newLine = (if (index._1 == 0) {"\n"} else {""}) 
			val tokenOnSpace:Option[Token] = tokens.aliveTokens.flatten.filter{_.currentSpace == space}.headOption
			
			val spaceClassColor = spaceToString(space.typeOfSpace)
			val tokenString = tokenOnSpace.map{tokens.indexOf}.map{tokensToLetters(tokens, team)}.getOrElse{' '}
			val cursorColor = cursor.filter{_ == space}.map{x => "\u001b[4m"}.getOrElse("\u001b[24m") // underline
			val tokenColor = if (selected.map{tokens.tokens(_)} == tokenOnSpace) {scala.Console.BOLD} else {"\u001b[21m"}
			
			newLine + scala.Console.RESET + cursorColor + spaceClassColor + tokenColor + tokenString
		}
	}
	
	private val (tl, tr, bl, br, horiz, vert) = (',', '.', '`', '\'', '-', '|')
	
	/** @version a.6.0 */
	def apply(out:PrintStream, tokens:ListOfTokens, field:RectangularField[SpaceClass], team:Option[Int], cursor:Option[RectangularSpace[SpaceClass]] = None, selected:Option[TokenIndex] = None):Unit = {
		val strings = spaceStrings(tokens, field, team, cursor, selected)
		
		strings.foreach{ x => out.print( x ) }
		out.print(scala.Console.RESET)
	}
}

