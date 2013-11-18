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
package com.rayrobdod.deductionTactics

/**
 * 
 * @author Raymond Dodge
 */
package object consoleView
{

	/**
	 * @version 10 Aug 2012
	 * @version 2012 Nov 30 - making use zips instead of Iterators and maps
	 * @throws IllegalArgumentException if tokens has more tokens than this has letters reserved for
	 */
	def tokensToLetters(tokens:ListOfTokens):Map[Token, Char] = {
		val myChars = '0' to '9'
		val enemyChars = ('a' to 'z') ++ ('A' to 'Z')
		
		val returnValue:Map[Token, Char] = tokens match {
			case x:PlayerListOfTokens =>
				(x.myTokens.zip(myChars) ++ x.otherTokens.flatten.zip(enemyChars)).toMap
			case _ =>
				tokens.tokens.flatten.zip(enemyChars).toMap
		}
		
		if (tokens.tokens.flatten.size != returnValue.size)
			throw new IllegalArgumentException("list of tokens contained more tokens than this is capable of supporting.")
		
		return returnValue;
	}
}
