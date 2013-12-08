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

import scala.collection.mutable.{Seq => MSeq}
import scala.collection.immutable.{Seq => ISeq, Map => IMap}

/**
 * @author Raymond Dodge
 * @version 25 Jan 2012
 */
trait ListOfTokens {
	def tokens():ISeq[ISeq[Token]]
	def aliveTokens() = tokens.map{_.filter(ListOfTokens.aliveFilter)}
}

object ListOfTokens {
	
	/**
	 * Determines whether a Token counts as alive.
	 * @version 25 Jan 2012
	 */
	object aliveFilter extends Function1[Token,Boolean] {
		def apply(x:Token):Boolean = x.currentHitpoints > 0
	}
}

/**
 * @author Raymond Dodge
 * @version 19 Jan 2012
 */
class CannonicalListOfTokens(
		val tokens:ISeq[ISeq[CannonicalToken]]
) extends ListOfTokens

/**
 * @author Raymond Dodge
 * @version 27 Nov 2012
 */
class PlayerListOfTokens(
		val myTokens:ISeq[CannonicalToken],
		val otherTokens:ISeq[ISeq[MirrorToken]]
) extends ListOfTokens {
	def tokens():ISeq[ISeq[Token]] = myTokens +: otherTokens
	
	def aliveMyTokens = myTokens.filter(ListOfTokens.aliveFilter)
	def aliveOtherTokens = otherTokens.map{_.filter(ListOfTokens.aliveFilter)}
}

/**
 * A list of tokens that can be mutated.
 * 
 * Exists because [[com.rayrobdod.deductionTactics.UnitAwareSpaceClass]]es
 * need to know where the tokens are despite being made before the tokens are made 
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
class MutableListOfTokens extends ListOfTokens {
	var tokens = ISeq.empty[ISeq[CannonicalToken]]
}
