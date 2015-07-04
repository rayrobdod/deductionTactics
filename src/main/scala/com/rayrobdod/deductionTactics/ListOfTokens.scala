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

import scala.collection.immutable.Seq

/**
 * @author Raymond Dodge
 * @version a.6.0
 */
final class ListOfTokens (
	val tokens:Seq[Seq[Token]]
) {
	def aliveTokens():Seq[Seq[Token]] = tokens.map{_.filter(ListOfTokens.aliveFilter)}
	
	/** Since the other ListOfTokens are no longer around to provide this method
	 * @version a.6.0
	 */
	def alivePlayerTokens(player:Int):Seq[Token] = tokens(player).filter(ListOfTokens.aliveFilter)

	/** Since the other ListOfTokens are no longer around to provide this method
	 * @version a.6.0
	 */
	def aliveNotPlayerTokens(player:Int):Seq[Seq[Token]] = {
		tokens.zipWithIndex.filter(_._2 != player).map{_._1}
				.map{_.filter(ListOfTokens.aliveFilter)}
	}
	
	
	/**  @since a.6.0 */
	def indexOf(t:Token):TokenIndex = {
		tokens.zipWithIndex.flatMap{(a) =>
			a._1.zipWithIndex.map{(b) => (( ((a._2, b._2)), b._1 ))}
		}.find{_._2 == t}.map{_._1}.getOrElse{((-1,-1))}
	}
	/**  @since a.6.0 */
	def tokens(i:TokenIndex):Token = tokens(i._1)(i._2)
	
	
	/**  @since a.6.0 */
	def hideTokenClasses(playerNumber:Int):ListOfTokens = {
		
		new ListOfTokens(this.tokens.zipWithIndex.map{(x) =>
			if (x._2 == playerNumber) {
				x._1
			} else {
				x._1.map{(t:Token) => t.copy(tokenClass = None)}
			}
		})
	}
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

