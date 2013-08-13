package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{Space, TokenMovementCost}
import com.rayrobdod.boardGame.{RectangularField => Field}


/**
 * An AI that blatantly cheats. This needs to not work.
 * 
 * @author Raymond Dodge
 * @version 2013 Aug 07
 */
class RepeatAttackCheater extends PlayerAI
{
	/** [[com.rayrobdod.deductionTactics.ai.randomTeam]] */
	def buildTeam = randomTeam()
	
	def takeTurn(player:Player):Any = {
		val otherTokens = player.tokens.otherTokens.flatten
		
		otherTokens.foreach{(x:MirrorToken) =>
			while (x.currentHitpoints > 0) {
				x.beAttacked(Elements.Fire, Weaponkinds.Bluntkind, aSpace)
			}
		}
	}
	
	var aSpace:Space = null
	def initialize(player:Player, field:Field) = {
		aSpace = field.space(0,0);
	}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[RepeatAttackCheater]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[RepeatAttackCheater].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
