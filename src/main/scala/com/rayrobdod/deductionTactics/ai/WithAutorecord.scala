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


/**
 * A decorator for PlayerAIs. It adds a set of listeners to the tokens and
 * player that can help determine various things about the tokens.
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithAutorecord(val base:PlayerAI) extends DecoratorPlayerAI(base)
{
	/** responds to actions */
	override def notifyTurn(player:Int, action:GameState.Result, beforeState:GameState, afterState:GameState, memo:Memo):Memo = {
		val a = base.notifyTurn(player, action, beforeState, afterState, memo)
		val b = a.addAttack(action)
		
		action match {
			case GameState.TokenAttackDamageResult(attackerIndex, _, elem, kind) => {
				
				val oldSusp = b.suspisions.get(attackerIndex).getOrElse(new TokenClassSuspision)
				val newSusp = oldSusp.copy(atkElement = Some(elem), atkWeapon = Some(kind))
				
				b.updateSuspision(attackerIndex, newSusp)
			}
			case GameState.TokenAttackStatusResult(attackerIndex, _, status) => {
				
				val oldSusp = b.suspisions.get(attackerIndex).getOrElse(new TokenClassSuspision)
				val newSusp = oldSusp.copy(atkStatus = Some(status))
				
				b.updateSuspision(attackerIndex, newSusp)
			}
			case _ => {b}
		}
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithAutorecord]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithAutorecord].canEquals(this) &&
				this.base == other.asInstanceOf[WithAutorecord].base
	}
	override def hashCode = base.hashCode * 13 + 19
	
	override def toString = base.toString + " with " + this.getClass.getName
}
