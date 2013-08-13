package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{RectangularField => Field}
import javax.swing.JFrame
import com.rayrobdod.deductionTactics.swingView.BoardGamePanel

/**
 * A decorator for PlayerAIs. It provides a viewport to a player
 * that might not provide one itself: useful for observing Computer
 * only matches.
 *
 * @author Raymond Dodge
 * @version 09 Jul 2012
 * @version 2012 Nov 30 - modifying toString to include the base
 */
final class WithSwingViewport(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** Forwards command to base */
	def buildTeam = base.buildTeam
	
	/** Forwards command to base, then creates a new JFrame with a BoardGamePanel */
	def initialize(player:Player, field:Field) = {
		base.initialize(player, field)
		
		new JFrame("Deduction Tactics - Observer"){
			add(new BoardGamePanel(player.tokens, field))
			pack()
			setVisible(true)
		}
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithRandomTeam]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithRandomTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithRandomTeam].base
	}
	override def hashCode = base.hashCode * 7 + 23
	
	override def toString = base.toString + " with " + this.getClass.getName
}
