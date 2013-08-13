package com.rayrobdod.deductionTactics

import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{EndOfTurn, StartOfTurn}

/**
 * @author Raymond Dodge
 * @version 2012 Apr 20
 * @version 2012 May 22 - adding a game ended condition (a player has run out of tokens)
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
		
		var i:Int = 0;
		while(gameContinues)
		{
			doPlayerTurn(players(i), ais(i), endOfTurnLocks(i))
			i = (i + 1) % players.length
			
			gameContinues = !players(i).tokens.aliveMyTokens.isEmpty
		}
	}
	
	private def doPlayerTurn(player:Player, ai:PlayerAI, endOfTurnLock:Object)
	{
		endOfTurnLock.synchronized {
			player ! StartOfTurn
			ai.takeTurn(player)
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
}
