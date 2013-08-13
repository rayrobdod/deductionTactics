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
