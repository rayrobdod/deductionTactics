package com.rayrobdod.deductionTactics
package ai

//import com.rayrobdod.deductionTactics.{PlayerAI, Player,
//		CannonicalTokenClass, Token, RequestAttackForDamage, RequestMove}
import com.rayrobdod.boardGame.{Space, TokenMovementCost}
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.collection.mutable.PriorityQueue
import LoggerInitializer.{blindAttackAILogger => Logger}
import java.util.logging.Level


/**
 * An AI that, for each of it's tokens, will have the token move towards
 * and attack the closest enemy tokens.
 * 
 * @author Raymond Dodge
 * @version 24 Jan 2012
 * @version 25 Jan 2012 - implemented toString, equals and hashCode
 * @version 27 Jan 2012 - made AI take unit's speed into account
 * @version 27 Jan 2012 - adding observeAttack
 * @version 06 Feb 2012 - added prepareIO 
 * @version 12 Feb 2012 - Did the RequestMove thing, which really simplified things.
 * @version 13 Feb 2012 - making try all possible moves; any illegal ones will be denied.
 * @version 27 Feb 2012 - adding observeStatusAttack
 * @version 21 Mar 2012 - modified reactions for new event model
 * @version 05 Apr 2012 - adding TokenCosts to Space and SpaceClass methods that now require them
 * @version 08 Apr 2012 - changed initialize to use new StandardObserveAttacks and StandardObserveMovement
 * @version 30 Jul 2012 - doesn't use any info from initialize, so removing its implementation
 * @version 2013 Aug 07 - ripples from rewriting Player
 */
class BlindAttackAI extends PlayerAI
{
	/** [[com.rayrobdod.deductionTactics.ai.randomTeam]] */
	def buildTeam = randomTeam()
	
	def takeTurn(player:Player):Any =
	{
		implicit object TokenPairOrdering extends Ordering[(CannonicalToken, MirrorToken)]
		{
			def distance(a:(Token, Token)):Int = distance(a._1, a._2) 
			def distance(a:Token, b:Token):Int = a.currentSpace.distanceTo(b.currentSpace, a, TokenMovementCost)
			
			def compare(a:(CannonicalToken, MirrorToken), b:(CannonicalToken, MirrorToken)) =
			{
				-(distance(a) compareTo distance(b))
			}
		}
		
		val queue = new PriorityQueue ++= player.tokens.aliveMyTokens.flatMap{(myToken:CannonicalToken) =>
			player.tokens.aliveOtherTokens.flatten.map{(hisToken:MirrorToken) =>
			{
				(myToken, hisToken)
			}}
		}
		
		queue.foreach({(myToken:CannonicalToken, hisToken:MirrorToken) =>
			myToken.requestMoveTo(hisToken.currentSpace)
			myToken.tryAttackDamage(hisToken)
		}.tupled)
	}
	
	def initialize(player:Player, field:Field) = {}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[BlindAttackAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[BlindAttackAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
