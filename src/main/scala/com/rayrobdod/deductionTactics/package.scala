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
package com.rayrobdod

import com.rayrobdod.boardGame.Space

/**
 * classes for DeductionTactics
 * @author Raymond Dodge
 */
package object deductionTactics
{
	type TokenIndex = Tuple2[Int, Int]
	
	
	
	/* @since a.6.0 */
	final class AttackCostFunction(t:Token, l:ListOfTokens) extends Space.CostFunction[SpaceClass] {
		override def apply(from:Space[_ <: SpaceClass], to:Space[_ <: SpaceClass]):Int = {
			to.typeOfSpace.canAttack(t,l)(from, to)
		}
	}
	/* @since a.6.0 */
	final class MoveToCostFunction(t:Token, l:ListOfTokens) extends Space.CostFunction[SpaceClass] {
		override def apply(from:Space[_ <: SpaceClass], to:Space[_ <: SpaceClass]):Int = {
			to.typeOfSpace.canEnter(t,l)(from, to)
		}
	}
}
