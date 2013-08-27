package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{RectangularField => Field}

/**
 * A decorator for PlayerAIs. It adds a set of listeners to the tokens and
 * player that can help determine various things about the tokens.
 *
 * @author Raymond Dodge
 * @version ???
 * @version 03 Jul 2012 - renamed from HumanAutorecordAI to SwingInterfaceWithAutorecord
 * @version 09 Jul 2012 - renaming from SwingInterfaceWithAutorecord to WithAutorecord; making a decorator
 * @version 2012 Nov 30 - modifying toString to include the base
 * @version 2013 Aug 07 - ripples from rewriting Player
 */
final class WithAutorecord(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** Forwards command to base */
	def buildTeam = base.buildTeam
	
	/** Forwards command to base, then adds listeners as needed */
	def initialize(player:Player, field:Field)
	{
		// set up interface
		base.initialize(player, field)
		
		// setup recorders
		player.tokens.otherTokens.flatten.foreach{(token:MirrorToken) =>
			token.beDamageAttackedReactions_+=(new StandardObserveAttacks(token, player.tokens))
			token.beStatusAttackedReactions_+=(new StandardObserveAttacks(token, player.tokens))
			
			val movement = new StandardObserveMovement(token)
			token.moveReactions_+=(movement)
			player.addStartTurnReaction(movement)
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
