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

import com.rayrobdod.deductionTactics.{
	Token, CannonicalToken,ListOfTokens
}
import com.rayrobdod.boardGame.{Space, RectangularField => Field}


/**
 * 
 * @author Raymond Dodge
 * @version 2012 Dec 08
 * @version 2012 Dec 17 - now gets the noun from a SupplyInfo rather than getting a Tuple2
 * @version 2012 Dec 20 - added support for SpacePrinter
 * @version 2013 Aug 07 - ripples from rewriting Player
 */
class InfoPrinter(allTokens:ListOfTokens, field:Field) {

	private val out:java.io.PrintStream = System.out
	private val spacePrinter = new SpacePrinter(allTokens)
	
	def supplyHelp:Unit = {
		out.println(InfoPrinter.help)
	}
	
	def supplyInfo(a:Any) = {
		a match {
			case x:Token => { TokenPrinter(x) }
			case x:Space => { spacePrinter(x) }
			case _ => out.print(InfoPrinter.unkownItem)
		}
		out.write('\n');
	}
}


/**
 * 
 * @author Raymond Dodge
 * @version 2012 Dec 08
 * @version 2012 Dec 13 - implemented about
 * @version 2012 Dec 18 - working on help message
 */
object InfoPrinter {
	val unkownItem = "No information on this availiable."
	val help = """general command format: noun verb noun adjective
All items are in uppercase, except for space indexies.
-----
HELP - prints this help message
EXIT - causes a hard quit of the program and process
noun INFO - prints info about the subject (TOKEN #|SPACE ##)
TOKEN # MOVE TO (TOKEN #|SPACE ##) - moves a token as close to the object as possible
TOKEN # ATTACK TOKEN # FOR (DAMAGE|STATUS) - causes the subject attack the object in the specified manner

"""
	
	
	import com.rayrobdod.deductionTactics.{TITLE => appName, VERSION => version}
	val runningOn = "Java version: " + System.getProperty("java.version")
	
	val about = appName + " " + version + "\n" + runningOn
	/// credits?
}
