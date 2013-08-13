package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{RectangularField => Field}

/**
 * A decorator for PlayerAIs. It intercepts the buildTeam command and creates
 * a random one using the package randomTeam method
 *
 * @author Raymond Dodge
 * @version 09 Jul 2012
 * @version 2012 Nov 30 - modifying toString to include the base
 */
final class WithRandomTeam(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** chooses a team randomly */
	def buildTeam = randomTeam()
	
	/** Forwards command to base */
	def initialize(player:Player, field:Field) = base.initialize(player, field)
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithRandomTeam]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithRandomTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithRandomTeam].base
	}
	override def hashCode = base.hashCode * 7 + 23
	
	override def toString = base.toString + " with " + this.getClass.getName
}
