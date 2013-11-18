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

//import com.rayrobdod.deductionTactics.{PlayerAI, Player,
//		CannonicalTokenClass, Token, RequestAttackForDamage, RequestMove}
import com.rayrobdod.boardGame.{EndOfTurn, Space, TokenMovementCost}
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.collection.mutable.{Map => MMap}
import LoggerInitializer.{blindAttackAILogger => Logger}
import java.util.logging.Level

/**
 * An attempt to use [http://aigamedev.com/open/tutorials/potential-fields/ Potential Fields] in
 * a thing
 * @author Raymond Dodge
 * @version 05 Jun 2012
 */
class PotentialFieldAI extends PlayerAI
{
	def takeTurn(player:Player):Any =
	{
		// secrative state
		
	}
	
	// TODO: when terrain exists
	// doesn't change ever
	def terrainPotential(player:Player):Function1[Space, Int] = Function.const(0);
	
	// the same for each unit
	def enemyUnitAttraction(player:Player):Function1[Space, Int] = {
		val potentialStrength = {(distance:Int) => math.max(100 - (10 * distance), 0)}  
		
		val returnValue = MMap[Space, Int]()
		val enemyTokens:Iterable[Token] = player.tokens.aliveOtherTokens.flatten
		
		object recursivePart extends Function2[Space, Int, Any]
		{
			def apply(space:Space, distance:Int) = { 
				if (returnValue(space) >= potentialStrength(distance))
				{
					// base condition
				}
				else
				{
					returnValue += ((space, potentialStrength(distance)))
					space.adjacentSpaces.foreach{recursivePart(_, distance + 1)}
				}
			}
		}
		
		enemyTokens.foreach{(x:Token) => recursivePart(x.currentSpace, 0)}
		
		returnValue;
	}
	
	// repulsion from enemy unit due to range
	// per unit per turn
	// don't use if trying to hide range
	def enemyUnitRepulsion(player:Player, token:Token):Function1[Space, Int] = {
		val potentialStrength = {(distance:Int) => math.max(-200 + (20 * distance), 0)}  
		
		val returnValue = MMap[Space, Int]()
		val enemyTokens:Iterable[Token] = player.tokens.aliveOtherTokens.flatten
		
		object recursivePart extends Function2[Space, Int, Any]
		{
			def apply(space:Space, distance:Int) = { 
				if (returnValue(space) <= potentialStrength(distance) ||
						distance >= token.tokenClass.range.get)
				{
					// base condition
				}
				else
				{
					returnValue += ((space, potentialStrength(distance)))
					space.adjacentSpaces.foreach{recursivePart(_, distance + 1)}
				}
			}
		}
		
		enemyTokens.foreach{(x:Token) => recursivePart(x.currentSpace, 0)}
		
		returnValue;
	}
	
	
	
	
	
	
	
	

	def buildTeam = randomTeam()
		
	def initialize(player:Player, field:Field) = {
		player.tokens.otherTokens.flatten.foreach{(token:MirrorToken) =>
			token.reactions += new StandardObserveAttacks(token)
			
			val movement = new StandardObserveMovement(token)
			token.reactions += movement
			player.reactions += movement
		}
	}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[BlindAttackAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[BlindAttackAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 23
	
	override def toString = this.getClass.getName
}
