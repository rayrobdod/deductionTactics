package com.rayrobdod.deductionTactics
package consoleView.ansiEscape

import Elements.Element

import com.rayrobdod.boardGame.{RectangularField, Space, SpaceClass,
		SpaceClassConstructor, mapValuesFromObjectNameToSpaceClassConstructor}
import java.io.InputStreamReader

class SpacePrinter(tokens:ListOfTokens) extends Function1[Space, String]
{
	private val out:java.io.OutputStream = System.out;
	private val tokensToLetters = consoleView.tokensToLetters(tokens)
	
	def apply(sc:Space) = {
		val tokensOnSpace = tokens.tokens.flatten.filter{_.currentSpace == sc}.headOption
		def tokenChar = tokensOnSpace.map{tokensToLetters}
		def tokenColor = "3" + ElementToColor(tokensOnSpace)
	}
}

object ElementToColor // extends Function1[Element, Char]
{
	def apply(e:Element):Char = e match{
		case Elements.Light    => ColorCodes.white
		case Elements.Electric => ColorCodes.yellow
		case Elements.Fire     => ColorCodes.red
		case Elements.Frost    => ColorCodes.blue
		case Elements.Sound    => ColorCodes.green
	}
}

// ANSI escape code colors
object ColorCodes // something something Enumeration
{
	val black   = '0'
	val red     = '1'
	val green   = '2'
	val yellow  = '3'
	val blue    = '4'
	val magenta = '5'
	val cyan    = '6'
	val white   = '7'
}
