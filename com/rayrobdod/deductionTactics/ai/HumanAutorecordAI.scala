package com.rayrobdod.deductionTactics
package ai

import scala.collection.mutable.{Map => MMap}
import com.rayrobdod.boardGame.{RectangularField => Field}
import javax.swing.{JButton}

/**
 * A slight modification of a HumanAI that 
 * uses the same recoding algorythims as the Computer AIs
 *
 * @author Raymond Dodge
 * @version ???
 */
class HumanAutorecordAI extends PlayerAI
{
	val playerButtons = MMap[Player, JButton]()
	
	def takeTurn(player:Player)
	{
		playerButtons(player).setEnabled(true)
	}
	
	def initialize(player:Player, field:Field)
	{
		// set up interface
		val interface = new HumanAI;
		interface.initialize(player, field)
		playerButtons ++= interface.playerButtons
		
		// setup recorders
		player.tokens.otherTokens.flatten.foreach{(token:MirrorToken) =>
			token.reactions += new StandardObserveAttacks(token)
			
			val movement = new StandardObserveMovement(token)
			token.reactions += movement
			player.reactions += movement
		}
	}
	
	def buildTeam = HumanAI.buildTeam
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[HumanAutorecordAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[HumanAutorecordAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 21
	
	override def toString = this.getClass.getName
}
