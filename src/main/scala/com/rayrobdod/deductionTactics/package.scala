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

import com.rayrobdod.boardGame.{Space => BoardGameSpace}

/**
 * classes for DeductionTactics
 * @author Raymond Dodge
 */
package object deductionTactics
{
	type TokenIndex = Tuple2[Int, Int]
	
	/** Returns a formatted version number for this package based on a found MANIFEST.MF */
	def VERSION:String = {
		val v = java.lang.Package.getPackage("com.rayrobdod.deductionTactics").getImplementationVersion();
		
		// Manifest doesn't like alpha chars in version numbers
		if (v != null) v else "Unversioned";
	}
	
	/** A title for use in About dialogs */
	def TITLE:String = "Deduction Tactics" //java.lang.Package.getPackage("com.rayrobdod.deductionTactics").getImplementationTitle();
	
	
	
	
	/* @since a.6.0 */
	final class AttackCostFunction(t:Token, l:ListOfTokens) extends BoardGameSpace.CostFunction[SpaceClass] {
		override def apply(from:BoardGameSpace[_ <: SpaceClass], to:BoardGameSpace[_ <: SpaceClass]):Int = {
			to.typeOfSpace.canAttack(t,l)(from, to)
		}
	}
	/* @since a.6.0 */
	final class MoveToCostFunction(t:Token, l:ListOfTokens) extends BoardGameSpace.CostFunction[SpaceClass] {
		override def apply(from:BoardGameSpace[_ <: SpaceClass], to:BoardGameSpace[_ <: SpaceClass]):Int = {
			to.typeOfSpace.canEnter(t,l)(from, to)
		}
	}
}
