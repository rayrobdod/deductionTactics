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
