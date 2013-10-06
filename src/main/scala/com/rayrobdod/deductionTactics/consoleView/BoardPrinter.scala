package com.rayrobdod.deductionTactics
package consoleView

import com.rayrobdod.boardGame.{RectangularField, Space, SpaceClass,
		SpaceClassConstructor, mapValuesFromObjectNameToSpaceClassConstructor}
import java.io.PrintStream
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser

/**
 * 
 * @author Raymond Dodge
 * @version 2013 Oct 05 Nuking
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

