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

import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.{PlayerAI}
import java.awt.event.{ActionListener, ActionEvent}

import com.rayrobdod.deductionTactics.swingView.chooseTokenClasses
import com.rayrobdod.deductionTactics.swingView.narrowTokenClasses
import com.rayrobdod.deductionTactics.swingView.game


/**
 * An instance of the PlayerAI service. Needs to be a class, since it
 * needs to be instatible. However, as all instanes are identical,
 * they are are equal to each other
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class SwingInterface extends PlayerAI
{
	private[this] val endOfTurnLock = new Object();
	private[this] var takeTurnReturnValue:Option[GameState.Action] = None
	
	override def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
		
		val a = memo.asInstanceOf[SwingInterfaceMemo]
		a.panel.fireTurnStartListeners(gameState, memo)
		
		endOfTurnLock.synchronized{
			while (takeTurnReturnValue == None) { 
				endOfTurnLock.wait()
			}
			
			val retVal = takeTurnReturnValue.get
			takeTurnReturnValue = None
			Seq(retVal)
		}
	}
	
	def initialize(player:Int, initialState:GameState):Memo =
	{
		val tokens = initialState.tokens
		val viewmodel = new game.Top(tokens, player, initialState.board)
		viewmodel.setVisible(true)
		
		
		viewmodel.addActionPerformedListener{(act:GameState.Action) =>
			endOfTurnLock.synchronized {
				takeTurnReturnValue = Option(act)
				endOfTurnLock.notifyAll()
			}
		}
		
		
		SwingInterfaceMemo(
				base = new SimpleMemo,
				panel = viewmodel
		)
	}
	
	override def selectTokenClasses(maxSize:Int):Seq[TokenClass] = {
		val buildingLock = new Object()
		val teamBuilder = new chooseTokenClasses.Top(maxSize)
		
		teamBuilder.addNextActionListener(new ActionListener {
			override def actionPerformed(e:ActionEvent):Unit = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized
		{
			teamBuilder.show()
			buildingLock.wait
		}
		
		teamBuilder.results
	}
	
	override def narrowTokenClasses(
				otherPlayersSelectedClasses:Seq[Seq[TokenClass]],
				maxResultSize:Int,
				index:Int
	):Seq[TokenClass] = {
		val buildingLock = new Object()
		val teamBuilder = new swingView.narrowTokenClasses.Top(index, otherPlayersSelectedClasses, maxResultSize)
		
		teamBuilder.addNextActionListener(new ActionListener {
			override def actionPerformed(e:ActionEvent):Unit = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized
		{
			teamBuilder.show()
			buildingLock.wait
		}
		
		teamBuilder.results
	}
	
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = {
		val memo2 = memo.asInstanceOf[SwingInterfaceMemo]
		val panel = memo2.panel
		
		panel.fireNotificationListeners(action, afterState, memo)
	}
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[SwingInterface]}
	override def equals(other:Any):Boolean = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[SwingInterface].canEquals(this)
	}
	// arbitrary number (13)
	override def hashCode:Int = 13
	
	override def toString:String = this.getClass.getName
}

final case class SwingInterfaceMemo (
	base:Memo,
	panel:game.Top
) extends Memo {
	override def attacks:Seq[GameState.Result] = base.attacks
	override def suspicions:Map[(Int, Int), TokenClassSuspicion] = base.suspicions
	
	override def addAttack(r:GameState.Result):SwingInterfaceMemo =
			this.copy(base.addAttack(r))
	override def updateSuspicion(key:(Int, Int), value:TokenClassSuspicion):SwingInterfaceMemo =
			this.copy(base.updateSuspicion(key, value))
}
