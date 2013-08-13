package com.rayrobdod.deductionTactics
package ai

import Statuses.Status
import com.rayrobdod.boardGame.{EndOfTurn, Space, TokenMovementCost}
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.collection.mutable.{Map => MMap}
import LoggerInitializer.{blindAttackAILogger => Logger}
import java.util.logging.Level

/**
 * An AI that will focus on one token and have all of its
 * tokens attack that one.
 * 
 * @author Raymond Dodge
 * @version 30 Jul 2012
 */
class GangUpAI extends PlayerAI
{
	/** The current target */
	private val currentTargetMap = MMap.empty[Player, MirrorToken]
	
	def takeTurn(player:Player):Any =
	{
		if (currentTargetMap(player).currentHitpoints < 0)
		{
			// TRYTHIS: Choose betterly, maybe?
			currentTargetMap.update(player, player.tokens.aliveOtherTokens.flatten.head)
		}
		
		val currentTarget = currentTargetMap(player)
		val myTokens = player.tokens.aliveMyTokens
		val adjacentSpaces = currentTarget.currentSpace.adjacentSpaces
		
		//move tokens to surround opponent token
		// TODO: surround faster 
		// TODO: find out weakness to strike harder
		var continue = true
		while(continue) {
			continue = moveATokenToASpace(adjacentSpaces.toSet, player)
		}
		myTokens.foreach{(myToken:CannonicalToken) => player ! RequestMove(myToken, currentTarget.currentSpace)}
		
		// attack selected token
		myTokens.foreach{(myToken:CannonicalToken) =>
			if (currentTarget.currentStatus == None &&
						shouldUseStatus(myToken.tokenClass.atkStatus.get))
				player ! RequestAttackForStatus(myToken, currentTarget)
			else
				player ! RequestAttackForDamage(myToken, currentTarget)
		}
		
		player ! EndOfTurn
	}
	
	/** True if a token should use a status on a token */
	private def shouldUseStatus(s:Status):Boolean = {
		import Statuses._
		
		(s match {
			case Sleep => -3 // token already can't move since surrounded
			case Burn => 3
			case Blind => 3
			case Confuse => 1 // will attack one of my tokens randomly
			case Neuro => 2 // will attack one of my tokens randomly
			case Snake => 1 // token already can't move since surrounded
			case Heal => -1
		}) > 0
	}
	
	private def moveATokenToASpace(targetSpaces:Set[Space], player:Player):Boolean =
	{
		val myTokens = player.tokens.aliveMyTokens
		val spacesAndTokensCanReach = targetSpaces.map{(s:Space) => (( s, tokensThatCanReachSpace(s, myTokens) ))}.filter{_._2.size != 0}
		
		if (spacesAndTokensCanReach.isEmpty) return false
		if (spacesAndTokensCanReach.exists{_._2.size == 1})
		{
			spacesAndTokensCanReach.filter{_._2.size == 1}.foreach({(s:Space, ts:Seq[CannonicalToken]) =>
				player ! RequestMove(ts.head, s)
			}.tupled)
			return true
		}
		else
		{
			spacesAndTokensCanReach.foreach({(s:Space, ts:Seq[CannonicalToken]) =>
				player ! RequestMove(ts.head, s)
			}.tupled)
			return true
		}
	}
	
	private def tokensThatCanReachSpace(space:Space, tokens:Seq[CannonicalToken]) =
	{
		tokens.filter{(t:Token) => (t.currentSpace.spacesWithin(t.canMoveThisTurn, t, TokenMovementCost) - t.currentSpace).contains(space)}
	}
	
	
	
	
	def buildTeam = randomTeam()
	def initialize(player:Player, field:Field) = {
		currentTargetMap.update(player, player.tokens.otherTokens.flatten.head)
	}
	
	def canEquals(other:Any) = {other.isInstanceOf[GangUpAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[GangUpAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode =  41
	
	override def toString = this.getClass.getName //+ "[" + currentTargetMap + "]"
}
