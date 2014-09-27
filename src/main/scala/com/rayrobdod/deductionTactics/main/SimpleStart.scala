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
package main

import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.deductionTactics.ai._

/**
 * @since a.6.0
 */
object SimpleStart extends App {
	
	val players = Seq(new BlindAttackAI, new SwingInterface)
	
	val myTokenClass = Some(new TokenClassBlunt("Sample",
			BodyTypes.Humanoid,
			Elements.Fire,
			Weaponkinds.Bladekind,
			Statuses.Burn,
			1, 2,
			Directions.Left,
			Weaponkinds.values.map{(a) => ((a, 1f))}.toMap,
			Statuses.Burn
	))
	
	val initialState = {
		val field = RectangularField( Seq.fill(4,4)(UniPassageSpaceClass.apply) )
		val tokens = new ListOfTokens( Seq(
			Seq(
				new Token(currentSpace = field.space(0,0), tokenClass = myTokenClass),
				new Token(currentSpace = field.space(0,3), tokenClass = myTokenClass)
			),
			Seq(
				new Token(currentSpace = field.space(3,0), tokenClass = myTokenClass),
				new Token(currentSpace = field.space(3,3), tokenClass = myTokenClass)
			)
		))
		
		GameState(field, tokens)
	}
	
	
	new PlayerTurnCycler(players, initialState).run()
	
}
