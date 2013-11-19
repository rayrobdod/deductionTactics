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

import scala.collection.immutable.{Seq, Set, BitSet}

/**
 * Iterates through the players, handing out turns to each in turn.
 * 
 * This is a Runnable and does not create its own thread.
 * @param players the players of the game
 * @param timeBetweenTurns a delay between turns incase the turns are moving too quickly.
 * 
 * @author Raymond Dodge
 * @version a.5.0
 */
class PlayerTurnCycler(val players:Seq[Player], var timeBetweenTurns:Int = 500) extends Runnable
{
	private var gameContinues:Boolean = true
	def gameEnded = !gameContinues
	
	def run() =
	{
		var i:Int = 0
		while(gameContinues)
		{
			players(i).takeTurn()
			i = remainingPlayers.filter{_>i}.headOption.getOrElse{remainingPlayers.head}
			
			gameContinues = remainingPlayers.size > 1
		}
		
		remainingPlayers.foreach{players(_).victoryReaction.map{a => a()}}
	}
	
	def remainingPlayers:Set[Int] = {
		val t:ListOfTokens = players(0).tokens
		
		BitSet.empty ++ t.aliveTokens.zipWithIndex.filter{_._1.length != 0}.map{_._2}
	}
}
