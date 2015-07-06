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

import java.io.InputStream

/** @since a.6.0 */
final class TokenSelector (
	val player:Int,
	var currentState:GameState,
	val inStream:InputStream,
	val reprint:Function1[Option[TokenIndex], Unit]
) extends Runnable {
	var shouldStop:Boolean = false
	
	def run() {
		while (! shouldStop) {
			val c = inStream.read.toChar
			val charToTokenIndex:Map[Char, TokenIndex] = tokensToLetters(currentState.tokens, Some(player)).map{_.swap}.toMap
			if (charToTokenIndex.contains(c)) {
				val tokenIndex = charToTokenIndex.get(c)
				reprint(tokenIndex)
			} else if (' ' == c) {
				val tokenIndex = None
				reprint(tokenIndex)
			}
		}
	}
}

/**
 * So that all the TokenMouseListeners can agree on who the active token is
 * @since a.6.0
 * @todo utilities? JavaFX Property?
 */
final class SharedActiveTokenProperty {
	var value:Option[TokenIndex] = None
}

/**
 * So that all the TokenMouseListeners can agree on who the active token is
 * @since a.6.0
 * @todo utilities? JavaFX Property?
 */
final class SharedGameStateProperty (
	var value:GameState
)
