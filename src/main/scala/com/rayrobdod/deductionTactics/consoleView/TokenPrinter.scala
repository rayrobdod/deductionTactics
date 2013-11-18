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

import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.{Token}
import scala.runtime.{AbstractFunction1 => Function1}

/**
 * @author Raymond Dodge
 * @version 2012 Dec 08
 */
object TokenPrinter extends Function1[Token,Unit]
{
	private def out = System.out
	
	
	def apply(token:Token) = {
		out.print(token.currentHitpoints);
		out.print("/256  ");
		out.print(token.currentStatus.map{_.name}.getOrElse{"No Status"});
		out.print(' ');
		out.println(token.currentStatusTurnsLeft)
		
		TokenClassPrinter(token.tokenClass)
	}
}
