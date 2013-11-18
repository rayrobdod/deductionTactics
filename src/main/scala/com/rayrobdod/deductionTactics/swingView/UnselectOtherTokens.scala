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
package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.Token
import scala.collection.immutable.Seq

/**
 * @author Raymond Dodge
 * @version 14 Feb 2012
 * @version 03 Nov 2012 - moved from com.rayrobdod.deductionTactics.test to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
 */
class UnselectOtherTokens(token:Token, otherTokens:Seq[Token])
		extends Function1[Boolean, Unit]
{
	def apply(b:Boolean):Unit = if (b) {
		(otherTokens diff Seq[Token](token)).foreach{_.beSelected(false)}
	}
}
