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
 * @version 03 Jul 2012 - renamed from HumanAutorecordAI to SwingInterfaceWithAutorecord
 */
class SwingInterfaceWithAutorecord extends PlayerAI
{
	val playerButtons = MMap[Player, JButton]()
	
	def takeTurn(player:Player)
	{
		playerButtons(player).setEnabled(true)
	}
	
	def initialize(player:Player, field:Field)
	{
		// set up interface
		val interface = new SwingInterface;
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
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[SwingInterfaceWithAutorecord]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[SwingInterfaceWithAutorecord].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 21
	
	override def toString = this.getClass.getName
}
