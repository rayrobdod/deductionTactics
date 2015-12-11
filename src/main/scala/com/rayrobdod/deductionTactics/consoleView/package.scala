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

import scala.collection.immutable.{Seq, Map}

/**
 * 
 * @author Raymond Dodge
 */
package object consoleView
{

	/**
	 * @throws IllegalArgumentException if tokens has more tokens than this has letters reserved for
	 * @version a.6.0
	 */
	def tokensToLetters(tokens:ListOfTokens, myTeamOpt:Option[Int]):Map[TokenIndex, Char] = {
		val myChars = '0' to '9'
		val enemyChars = ('a' to 'z') ++ ('A' to 'Z')
		
		val tokensToEnemyChars = tokens.tokens.flatten.zip(enemyChars).toMap
		
		if (tokens.tokens.flatten.size != tokensToEnemyChars.size) {
			throw new IllegalArgumentException("list of tokens contained more tokens than this is capable of supporting.")
		}
		
		val returnValue:Map[TokenIndex, Char] = {
			tokens.tokens.zipWithIndex.map({(ts:Seq[Token], i:Int) =>
				ts.zipWithIndex.map({(t:Token, j:Int) =>
					(( ((i,j)), 
						if (myTeamOpt == Option(i)) {
							myChars(j)
						} else {
							tokensToEnemyChars(t)
						}
					))
				}.tupled)
			}.tupled)
		}.flatten.toMap
		
		returnValue
	}
	
	/** @since a.5.1 */
	val controlCursorToTop = "\u001B[1;1H"
	/** @since a.5.1 */
	val controlClearRest = "\u001B[J"
}
