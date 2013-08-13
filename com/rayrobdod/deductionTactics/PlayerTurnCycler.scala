package com.rayrobdod.deductionTactics

import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import scala.collection.immutable.{Seq, Set, BitSet}
import com.rayrobdod.boardGame.{EndOfTurn, StartOfTurn}

/**
 * @author Raymond Dodge
 * @version 2012 Apr 20
 * @version 2012 May 22 - adding a game ended condition (a player has run out of tokens)
 * @version 2012 Aug 04 - modified to work with more than two players; implementing remainingPlayers
 */
class PlayerTurnCycler(val playersAndAIs:Seq[(Player, PlayerAI)], var timeBetweenTurns:Int = 500) extends Runnable
{
	private val endOfTurnLocks:Seq[Object] = Seq.fill(playersAndAIs.size){new Object()}
	private val players = playersAndAIs.map{_._1}
	private val ais = playersAndAIs.map{_._2}
	private var gameContinues:Boolean = true
	def gameEnded = !gameContinues
	
	def run() =
	{
		players.zip(endOfTurnLocks).foreach({(p:Player, lock:Object) => {
			p.reactions += new EndOfTurnNotification(lock)
		}}.tupled)
		
		var i:Int = 0
		while(gameContinues)
		{
//			System.out.println(i)
			doPlayerTurn(players(i), ais(i), endOfTurnLocks(i))
			i = remainingPlayers.filter{_>i}.headOption.getOrElse{remainingPlayers.head}
//			System.out.println(i)
			
			gameContinues = remainingPlayers.size > 1
		}
		
		remainingPlayers.foreach{players(_) ! Victory}
	}
	
	private def doPlayerTurn(player:Player, ai:PlayerAI, endOfTurnLock:Object)
	{
		endOfTurnLock.synchronized {
			player ! StartOfTurn
			ai.takeTurn(player)
			// Won't work until `player.reactions.+=(ai)` (Main.scala : 71) works.
			//player ! DoAI(player)
			endOfTurnLock.wait
		}
	}
	
	private class EndOfTurnNotification(lock:Object) extends Reaction
	{
		final override def apply(e:Event) = {
			lock.synchronized {
				lock.notifyAll
			}
		}
		
		final override def isDefinedAt(e:Event) = {e match {
			case EndOfTurn => true
			case _ => false
		}}
	}
	
	def remainingPlayers:Set[Int] = {
		val t:ListOfTokens = playersAndAIs(0)._1.tokens
		
		BitSet.empty ++ t.aliveTokens.zipWithIndex.filter{_._1.length != 0}.map{_._2}
	}
}
