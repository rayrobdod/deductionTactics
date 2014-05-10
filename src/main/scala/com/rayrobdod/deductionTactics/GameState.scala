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

import com.rayrobdod.boardGame.{RectangularField, Space}

/**
 * @since a.6.0
 */
final case class GameState (
	val board:RectangularField[SpaceClass],
	val tokens:ListOfTokens
) {
	
	
		
}

/**
 * @since a.6.0
 */
object GameState {
	sealed class Action
	
	case class TokenMove(token:Token, space:Space[SpaceClass]) extends Action
	case class TokenAttackDamage(attacker:Token, attackee:Token) extends Action
	case class TokenAttackStatus(attacker:Token, attackee:Token) extends Action
	object EndOfTurn extends Action
}
