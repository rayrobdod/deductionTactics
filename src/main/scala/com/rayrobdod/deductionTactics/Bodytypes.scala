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

/**
 * An enumeration for the Bodytypes
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 29 Jul 2012 - adding Avian  as a value
 * @version 2013 Jun 14 - adding Gerbil as a value
 */
object BodyTypes extends Enumeration {
	val Humanoid = Value("Human")
	val Avian    = Value("Avian")
	val Gerbil   = Value("Gerbillinae")
}
