package com.rayrobdod.deductionTactics
package consoleView

import com.rayrobdod.boardGame.{Space, SpaceClass}
import scala.runtime.{AbstractFunction1 => Function1}

/**
 * @author Raymond Dodge
 * @since 2012 Dec 20
 * @version 2013 Oct 06
 */
class SpaceInfoPrinter(tokens:ListOfTokens) extends Function1[Space,Unit]
{
	private def out = System.out
	
	def apply(space:Space) = {
		val tokenOnSpace = tokens.aliveTokens.flatten.find{_.currentSpace == space};
		val spaceClass:SpaceClass = space.typeOfSpace;
		
		out.print("Type of space: ");
		out.println( spaceClass match {
			case FreePassageSpaceClass()   => "Passible";
			case AllyPassageSpaceClass()   => "Passible if not occupied by enemy";
			case UniPassageSpaceClass()    => "Passible if not occupied";
			case ImpassibleSpaceClass()    => "Impassible";
			case AttackOnlySpaceClass()    => "Impassable, but attackable";
			case FlyingPassageSpaceClass() => "Passible if Flying";
			case FirePassageSpaceClass()   => "Passible if Flying or Fire";
			case _ => "Unknown"
		})
	}
}
