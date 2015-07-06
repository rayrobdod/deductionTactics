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
package com.rayrobdod.deductionTactics.consoleView

import com.rayrobdod.deductionTactics.{
	Token, CannonicalToken, ListOfTokens
}
import com.rayrobdod.boardGame.{Space, RectangularField => Field}
import java.util.regex.{Pattern, Matcher}

/**
 * 
 * 
 * @author Raymond Dodge
 * @version a.5.1
 */
class CommandParser(tokens:ListOfTokens, field:Field) {
	import CommandParser._;
	private val lettersToTokens:Map[Char,Token] = tokensToLetters(tokens).map{_.swap}
	
	def parseCommand(line:String):Unit = {
		val matcher = TOTAL_PATTERN.matcher(line.trim)
		if (! matcher.matches())
				throw new IllegalArgumentException("line doesn't match pattern.");
		
		val subj = convertNoun( matcher.group(1) );
		val verb = matcher.group(2);
		val objt = convertNoun( matcher.group(3) );
		val adje = matcher.group(4);
		
		convertVerb(verb, subj, objt, adje);
	}
	
	private def convertNoun(string:String):Option[Any] = {
		if (string == null) {
			None
		} else if (string == "HELP") {
			Some(SupplyHelp)
		} else if (string.take(6) == "TOKEN ") {
			val tokenId = string.drop(6)
			
			if (! lettersToTokens.contains( tokenId(0) ) )
				throw new IllegalArgumentException(tokenId(0) + " is not a valid token")
			
			// Token
			val returnValue:Some[Token] = Some( lettersToTokens( tokenId(0) ) )
			returnValue
			
		} else if (string.take(6) == "SPACE ") {
			val tokenId = string.drop(6)
			
			val spaceMatcher = SPACE_NUMBER_PATTERN.matcher(tokenId)
			
			if (! spaceMatcher.matches() )
				throw new IllegalArgumentException(tokenId + " is not a valid space")
			
//			System.out.println( spaceMatcher.group(1), spaceMatcher.group(2) );
			
			val firstDim = Integer.parseInt(spaceMatcher.group(1)) - 1;
			val secondDim = (spaceMatcher.group(2).apply(0) - 'A').intValue;
			
//			System.out.println( firstDim, secondDim );

			// Space
			Some( field.space( firstDim, secondDim )) 
			
		} else if (string.take(7) == "PLAYER ") {
			val tokenId = string.drop(7)
			
			// Seq[Token]
			Some( tokens.tokens().apply( Integer.parseInt( tokenId ) ) );
			
		} else {None}
	}
	
	/**
	 * @throws IllegalArgumentException if the arguments supplied don't make sense together
	 */
	private def convertVerb(verb:String, subject:Option[Any], objct:Option[Any], adjective:String):Event = {
		verb match {
			case null => {
				subject match {
					case Some(SupplyHelp) => SupplyHelp
					case _ => throw new IllegalArgumentException("No verb supplied")
				}
			}
			case "INFO" => {
				subject match {
					case Some(SupplyHelp) => SupplyHelp
					case Some(x) => SupplyInfo(x, adjective)
					case None => throw new IllegalArgumentException("No info about ''.")
				}
			}	
			case "ATTACK" => {
				val attacker = subject match {
					case Some(x:CannonicalToken) => x
					case Some(x:Token) => throw new IllegalArgumentException("Cannot ATTACK using a token you don't own")
					case _ => throw new IllegalArgumentException("Cannot ATTACK using one of these")
				}
				val defender = objct match {
					case Some(x:Token) => x
					case _ => throw new IllegalArgumentException("Cannot ATTACK one of these")
				}
				
				adjective match {
					case "FOR DAMAGE" => RequestAttackForDamage(attacker, defender)
					case "FOR STATUS" => RequestAttackForStatus(attacker, defender)
					case _ => throw new IllegalArgumentException("Must attack either 'FOR DAMAGE' or 'FOR STATUS'")
				}
			}
			case "MOVE TO" => {
				val mover = subject match {
					case Some(x:CannonicalToken) => x
					case Some(x:Token) => throw new IllegalArgumentException("Cannot MOVE using a token you don't own")
					case _ => throw new IllegalArgumentException("Cannot MOVE using one of these")
				}
				val movee = objct match {
					case Some(x:Token) => x.currentSpace
					case Some(x:Space) => x
					case _ => throw new IllegalArgumentException("Cannot MOVE one of these")
				}
				
				RequestMove(mover, movee)
			}
		}
	}
}

/**
 * @author Raymond Dodge
 * @version 2012 Dec 07
 */
object CommandParser {
	
	private val NOUN = "(PLAYER \\d|TOKEN \\w|SPACE \\w+|HELP)"
	private val VERB = "(INFO|ATTACK|MOVE TO)"
	private val ADJECTIVE = "(FOR DAMAGE|FOR STATUS|FULL|ABBR)"
	
	// A noun - then optionally a verb - then optionally either a noun or an adjective or a noun then an adjective
	private val TOTAL = NOUN + "(?: " + VERB + "(?: " + NOUN + ")?(?: " + ADJECTIVE + ")?)?";
	private val TOTAL_PATTERN = Pattern.compile(TOTAL)
	
	private val SPACE_NUMBER_PATTERN = Pattern.compile("(\\d+)([A-Za-z]+)")
}
