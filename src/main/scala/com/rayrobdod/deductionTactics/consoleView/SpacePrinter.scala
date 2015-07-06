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

import com.rayrobdod.boardGame.{Space}

/**
 * @author Raymond Dodge
 * @since 2012 Dec 20
 * @version a.6.0
 */
object SpaceInfoPrinter
{
	private def out = System.out
	
	def apply(space:Space[SpaceClass]):Unit = {
		val spaceClass:SpaceClass = space.typeOfSpace;
		
		out.print("Type of space: ");
		out.println( spaceClass match {
			case FreePassageSpaceClass()   => "Passible";
			case AllyPassageSpaceClass()   => "Passible if not occupied by enemy";
			case UniPassageSpaceClass()    => "Passible if not occupied";
			case ImpassibleSpaceClass()    => "Impassible";
			case AttackOnlySpaceClass()    => "Impassable, but attackable";
			case FlyingPassageSpaceClass() => "Passible if Flying";
			case FirePassageSpaceClass()   => "Passible if Fire";
			case SlowPassageSpaceClass()   => "Passible, slowly"
			case _ => "Unknown"
		})
	}
}
