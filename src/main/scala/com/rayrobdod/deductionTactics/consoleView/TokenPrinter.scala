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
package com.rayrobdod.deductionTactics.consoleView

import com.rayrobdod.deductionTactics.ai.TokenClassSuspicion
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.Token
import scala.runtime.{AbstractFunction2 => Function2}

/**
 * @version a.6.0
 */
object TokenPrinter extends Function2[Token, TokenClassSuspicion, Unit]
{
	private def out = System.out
	
	
	def apply(token:Token, susp:TokenClassSuspicion):Unit = {
		out.print(token.currentHitpoints);
		out.print("/");
		out.print(Token.maximumHitpoints);
		out.print("  ");
		out.print(token.currentStatus.name);
		out.print(' ');
		out.println(token.currentStatusTurnsLeft)
		
		token.tokenClass match {
			case Some(x) => TokenClassPrinter(x)
			case None => TokenClassPrinter(susp)
		}
	}
}
