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

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import com.rayrobdod.boardGame.{RectangularField => Field, Token => BoardGameToken, Space}

/**
 * A decorator for PlayerAIs. It prints to console events that happen
 *
 * @author Raymond Dodge
 * @version a.5.0
 */
final class WithConsoleEventPrinting(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** Forwards command to base */
	def buildTeam = base.buildTeam
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	def initialize(player:Player, field:Field) = {
		base.initialize(player, field)
		
		player.tokens.tokens.flatten.zipWithIndex.foreach({(t:Token, i:Int) =>
			import WithConsoleEventPrinting._
			val name = "Token " + i
			
			t.moveReactions_+=( new PrintMove(name) )
			t.selectedReactions_+=( new PrintBeSelected(name) )
			
			t.diedReactions_+=( new PrintDied(name) )
			t.updateReactions_+=( new PrintUpdate(name) )
			t.beDamageAttackedReactions_+=( new PrintDamageAttack(name) )
			t.beStatusAttackedReactions_+=( new PrintStatusAttack(name) )
		}.tupled)
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithConsoleEventPrinting]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithConsoleEventPrinting].canEquals(this) &&
				this.base == other.asInstanceOf[WithConsoleEventPrinting].base
	}
	override def hashCode = base.hashCode * 7 + 43
	
	override def toString = base.toString + " with " + this.getClass.getName
}

object WithConsoleEventPrinting {
	val out = System.out
	import scala.runtime.{AbstractFunction2 => AFunction2, AbstractFunction1 => AFunction1}
	
	final class PrintBeSelected(tokenName:String) extends AFunction1[Boolean, Unit] with BoardGameToken.SelectedReactionType {
		def apply(b:Boolean) = {
			out.print(tokenName)
			out.print(": Selected(")
			out.print(b)
			out.println(")")
		}
	}
	
	final class PrintMove(tokenName:String) extends AFunction2[Space, Boolean, Unit] with BoardGameToken.MoveReactionType {
		def apply(s:Space, b:Boolean) = {
			out.print(tokenName)
			out.println(": Moved(-, -)")
		}
	}
	
	final class PrintUpdate(tokenName:String) extends Function0[Unit] {
		def apply() = {
			out.print(tokenName)
			out.println(": Update")
		}
	}
	
	final class PrintDamageAttack(tokenName:String) extends Token.DamageAttackedReactionType {
		def apply(atkElem:Element, atkKind:Weaponkind, attackerSpace:Space):Unit = {
			out.print(tokenName)
			out.print(": DamageAttack(")
			out.print(atkElem)
			out.print(", ")
			out.print(atkKind)
			out.println(", -)")
		}
	}
	
	final class PrintStatusAttack(tokenName:String) extends Token.StatusAttackedReactionType {
		def apply(atkStatus:Status, attackerSpace:Space):Unit = {
			out.print(tokenName)
			out.print(": StatusAttack(")
			out.print(atkStatus)
			out.println(", -)")
		}
	}
	
	final class PrintDied(tokenName:String) extends Function0[Unit] {
		def apply() = {
			out.print(tokenName)
			out.println(": Died")
		}
	}
}
