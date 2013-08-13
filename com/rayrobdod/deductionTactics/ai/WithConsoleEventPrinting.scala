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
 * @version 2013 Aug 07
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
			
			t.addMoveReaction( new PrintMove(name) )
			t.addSelectedReaction( new PrintBeSelected(name) )
			
			t.addDiedReaction( new PrintDied(name) )
			t.addUpdateReaction( new PrintUpdate(name) )
			t.addDamageAttackedReaction( new PrintDamageAttack(name) )
			t.addStatusAttackedReaction( new PrintStatusAttack(name) )
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
	
	final class PrintBeSelected(tokenName:String) extends BoardGameToken.SelectedReactionType {
		def apply(b:Boolean) = {
			out.print(tokenName)
			out.print(": Selected(")
			out.print(b)
			out.println(")")
		}
	}
	
	final class PrintMove(tokenName:String) extends BoardGameToken.MoveReactionType {
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
