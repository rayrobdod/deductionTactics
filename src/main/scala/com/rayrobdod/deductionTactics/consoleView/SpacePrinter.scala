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
