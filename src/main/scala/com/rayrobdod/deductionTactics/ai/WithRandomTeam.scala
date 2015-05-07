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
package ai

import scala.util.Random
import scala.collection.immutable.Seq

/**
 * A decorator for PlayerAIs. It intercepts the buildTeam command and creates
 * a random one using the package randomTeam method
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithRandomTeam(val base:PlayerAI) extends DecoratorPlayerAI(base)
{
	/** chooses a team randomly */
	override def selectTokenClasses(size:Int) = randomTeam(size)
	/** chooses a subset of selectedTokenClasses randomly */
	override def narrowTokenClasses(
				selectedTokenClasses:Seq[Seq[TokenClass]],
				maxResultSize:Int,
				index:Int
	):Seq[TokenClass] = Random.shuffle(selectedTokenClasses(index)).take(maxResultSize)
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithRandomTeam]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithRandomTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithRandomTeam].base
	}
	override def hashCode = base.hashCode * 7 + 23
	
	override def toString = base.toString + " with " + this.getClass.getName
}
