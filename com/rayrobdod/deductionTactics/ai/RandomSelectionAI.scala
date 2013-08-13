package com.rayrobdod.deductionTactics
package ai

//import com.rayrobdod.deductionTactics.{PlayerAI, Player,
//		CannonicalTokenClass, Token, Attack, RequestMove}
import com.rayrobdod.boardGame.{EndOfTurn, Space}
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.util.Random

/**
 * A AI that chooses a team randomly, but with no duplicates.
 * Does not have a turn AI yet.
 * 
 * @author Raymond Dodge
 * @version 24 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.ai
			to com.rayrobdod.deductionTactics.ai
 * @version 20 Jan 2012
 * @version 25 Jan 2012 - made use the list of alive tokens instead of the list of all tokens
 * @version 27 Jan 2012 - adding observeAttack
 * @version 06 Feb 2012 - added prepareIO 
 * @version 27 Feb 2012 - adding observeStatusAttack
 * @version 25 Mar 2012 - modified reactions for new event model
 * @version 08 Apr 2012 - changed initialize to use new StandardObserveAttacks and StandardObserveMovement
 */
class RandomSelectionAI extends PlayerAI
{
	def takeTurn(player:Player):Any =
	{
		val movingToken:CannonicalToken = Random.shuffle(player.tokens.aliveMyTokens).head
		
		{
			val currentSpace = movingToken.currentSpace
			val borderSpaces = currentSpace.adjacentSpaces
			val validBorderSpaces = borderSpaces.filter{(s:Space) => player.tokens.tokens.forall{_.forall{_.currentSpace != s}}}
			val moveToSpace = Random.shuffle(validBorderSpaces).head
			player ! RequestMove(movingToken, moveToSpace)
		}
		{
			val currentSpace = movingToken.currentSpace
			val borderSpaces = currentSpace.adjacentSpaces
			val attackable = player.tokens.aliveOtherTokens.flatten{_.filter{(t:Token) => borderSpaces.filter{t.currentSpace == _}.size > 0}}
			val attacking = Random.shuffle(attackable).headOption
			
			attacking.foreach{player ! RequestAttackForDamage(movingToken, _)}
		}
		
		player ! EndOfTurn
	}
	
	def initialize(player:Player, field:Field) = {
		player.tokens.otherTokens.flatten.foreach{(token:MirrorToken) =>
			token.reactions += new StandardObserveAttacks(token)
			
			val movement = new StandardObserveMovement(token)
			token.reactions += movement
			player.reactions += movement
		}
	}
	def buildTeam = randomTeam()
	
	
	def canEquals(other:Any) = {other.isInstanceOf[RandomSelectionAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[RandomSelectionAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 17
	
	override def toString = this.getClass.getName
}
