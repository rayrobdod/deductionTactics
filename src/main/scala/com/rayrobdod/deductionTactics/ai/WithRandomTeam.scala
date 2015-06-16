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

import scala.collection.immutable.Seq

/**
 * A decorator for PlayerAIs. It intercepts the buildTeam command and creates
 * a random one using the package randomTeam method
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithRandomTeam(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	override def takeTurn(player:Int, gameState:GameState, memo:Memo) = base.takeTurn(player, gameState, memo)
	/** chooses a team randomly */
	override def buildTeam(size:Int):Seq[TokenClass] = randomTeam(size)
	
	/** Forwards command to base */
	override def initialize(player:Int, initialState:GameState):Memo = base.initialize(player, initialState)
	/** Forwards notify to base */
	override def notifyTurn(player:Int, action:GameState.Result, beforeState:GameState, afterState:GameState, memo:Memo):Memo =
				base.notifyTurn(player, action, beforeState, afterState, memo)
	
	
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[WithRandomTeam]}
	override def equals(other:Any):Boolean = {
		this.canEquals(other) && other.asInstanceOf[WithRandomTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithRandomTeam].base
	}
	override def hashCode:Int = base.hashCode * 7 + 23
	
	override def toString:String = base.toString + " with " + this.getClass.getName
}
