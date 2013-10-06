package com.rayrobdod.deductionTactics.ai

import com.rayrobdod.deductionTactics.{PlayerAI, Player, Token}
import com.rayrobdod.boardGame.{RectangularField => Field}
import com.rayrobdod.deductionTactics.consoleView.BoardNavigator

/**
 *
 * @author Raymond Dodge
 * @version 2013 Oct 05
 */
final class ConsoleInterface extends PlayerAI
{
	private val endOfTurnLock = new Object();
	
	def takeTurn(player:Player) {
		endOfTurnLock.synchronized( endOfTurnLock.wait() );
	}
	
	def initialize(player:Player, field:Field) {
		val runner = new BoardNavigator(player.tokens, field)
		
		player.addVictoryReaction(runner.EndOfGameListener)
		player.addDefeatReaction (runner.EndOfGameListener)
		
		runner.addEndOfTurnReaction({() =>
			endOfTurnLock.synchronized( endOfTurnLock.notifyAll() );
		})
		player.tokens.tokens.flatten.foreach{(t:Token) => 
			t.selectedReactions_+=(new runner.SelectedListener(t))
		}
		
		new Thread(runner, "ConsoleInterface").start()
	}
	
	def buildTeam = {
		// TODO: actual prompts
		randomTeam()
	}
	
	
	
	
	// use default equals
	
	// arbitrary number (17)
	override def hashCode = 13
	
	override def toString = this.getClass.getName
}
