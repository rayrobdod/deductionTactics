package com.rayrobdod.deductionTactics

import scala.collection.immutable.{Seq, Set, BitSet}

/**
 * @author Raymond Dodge
 * @version 2012 Apr 20
 * @version 2012 May 22 - adding a game ended condition (a player has run out of tokens)
 * @version 2012 Aug 04 - modified to work with more than two players; implementing remainingPlayers
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
//			System.out.println(i)
			players(i).takeTurn()
			i = remainingPlayers.filter{_>i}.headOption.getOrElse{remainingPlayers.head}
//			System.out.println(i)
			
			gameContinues = remainingPlayers.size > 1
		}
		
		remainingPlayers.foreach{players(_).victoryReaction.map{a => a()}}
	}
	
	def remainingPlayers:Set[Int] = {
		val t:ListOfTokens = players(0).tokens
		
		BitSet.empty ++ t.aliveTokens.zipWithIndex.filter{_._1.length != 0}.map{_._2}
	}
}
