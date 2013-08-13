package com.rayrobdod.deductionTactics

/**
 * 
 * @author Raymond Dodge
 */
package object consoleView
{

	/**
	 * @version 10 Aug 2012
	 * @throws IllegalArgumentException if tokens has more tokens than this has letters reserved for
	 */
	def tokensToLetters(tokens:ListOfTokens):Map[Token, Char] = {
		try {
			val myChars = (('0' to '9')).toIterator
			val enemyChars = (('a' to 'z') ++ ('A' to 'Z')).toIterator
			
			tokens.tokens.flatMap{_.map{_ match {
				case x:MirrorToken => ((x, enemyChars.next))
				case x:CannonicalToken => ((x, myChars.next))
			}}}.toMap
		}
		catch
		{
			case x:NoSuchElementException =>
				throw new IllegalArgumentException("list of tokens contained more tokens than this is capable of supporting.", x)
		}
	}
}
