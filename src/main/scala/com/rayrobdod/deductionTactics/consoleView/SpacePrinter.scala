package com.rayrobdod.deductionTactics
package consoleView

import com.rayrobdod.boardGame.{Space, SpaceClass}
import scala.runtime.{AbstractFunction1 => Function1}

/**
 * @author Raymond Dodge
 * @version 2012 Dec 20
 */
class SpacePrinter(tokens:ListOfTokens) extends Function1[Space,Unit]
{
	private def out = System.out
	private val tokensToLetters = consoleView.tokensToLetters(tokens)

	
	def apply(space:Space) = {
		val tokenOnSpace = tokens.aliveTokens.flatten.find{_.currentSpace == space};
		val spaceClass:SpaceClass = space.typeOfSpace;
		
		out.print("Type of space: ");
		out.println( spaceClass match {
			case PassibleSpaceClass() => "Passible";
			case UnitAwareSpaceClass() => "Passible if not occupied";
			case ImpassibleSpaceClass() => "Impassible";
			case AttackableOnlySpaceClass() => "Impassable, but attackable";
			case NoStandOnSpaceClass() => "Passible if Flying";
			case _ => "Unknown"
		})
		
		tokenOnSpace match {
			case None => {}
			case Some(t:Token) => {
				out.print( "Token on this space: " )
				out.print( tokensToLetters(t) )
			}
		}
	}
}
