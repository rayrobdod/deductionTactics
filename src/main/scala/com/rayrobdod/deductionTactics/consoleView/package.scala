package com.rayrobdod.deductionTactics

/**
 * 
 * @author Raymond Dodge
 */
package object consoleView
{

	/**
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
	
	/** @since a.5.1 */
	val controlCursorToTop = "\u001B[1;1H"
	/** @since a.5.1 */
	val controlClearRest = "\u001B[J"
}
