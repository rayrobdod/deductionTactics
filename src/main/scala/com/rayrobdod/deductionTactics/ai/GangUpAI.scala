/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.rayrobdod.deductionTactics
package ai

import Statuses.Status
import com.rayrobdod.boardGame.{RectangularField, Space}
import scala.collection.mutable.{Map => MMap}
import LoggerInitializer.{blindAttackAILogger => Logger}
import java.util.logging.Level

/**
 * An AI that will focus on one token and have all of its
 * tokens attack that one.
 * 
 * @author Raymond Dodge
 * @version a.5.0
 */
class GangUpAI extends PlayerAI
{
	def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
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
		myTokens.foreach{(myToken:Token) => myToken.requestMoveTo(currentTarget.currentSpace)}
		
		// attack selected token
		myTokens.foreach{(myToken:Token) =>
			if (currentTarget.currentStatus == None &&
						shouldUseStatus(myToken.tokenClass.atkStatus.get))
				myToken.tryAttackStatus(currentTarget)
			else
				myToken.tryAttackDamage(currentTarget)
		}
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
			spacesAndTokensCanReach.filter{_._2.size == 1}.foreach({(s:Space, ts:Seq[Token]) =>
				ts.head.requestMoveTo(s)
			}.tupled)
			return true
		}
		else
		{
			spacesAndTokensCanReach.foreach({(s:Space, ts:Seq[Token]) =>
				ts.head.requestMoveTo(s)
			}.tupled)
			return true
		}
	}
	
	private def tokensThatCanReachSpace(space:Space, tokens:Seq[Token]) =
	{
		tokens.filter{(t:Token) => (t.currentSpace.spacesWithin(t.canMoveThisTurn, t, TokenMovementCost) - t.currentSpace).contains(space)}
	}
	
	
	
	
	def buildTeam(size:Int) = randomTeam(size)
	def initialize(player:Int, initialState:GameState):Memo = {
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
