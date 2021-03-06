/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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

import javax.sound.sampled.AudioSystem

/**
 * Only has one sound so far...
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithSound(val base:PlayerAI) extends DecoratorPlayerAI(base)
{
	private val PREFIX = "/com/rayrobdod/deductionTactics/soundView/"
	
	override def notifyTurn(player:Int, action:GameState.Result, beforeState:GameState, afterState:GameState, memo:Memo):Memo = {
		// TODO: sound depends on how hard the hit was?
		// TODO: sound depends on element and kind of attack
		// TODO: give some units a unique attack sound (very ?)
		action match {
			case x:GameState.TokenAttackDamageResult => {
				val fileName = PREFIX + "Hits/Hit.wav"
				val file = this.getClass().getResource(fileName)
				
				val audioIS = AudioSystem.getAudioInputStream(file)
				val audioClip = AudioSystem.getClip()
				
				audioClip.open(audioIS)
				
				audioClip.start()
			}
			case _ => {}
		}
		
		base.notifyTurn(player, action, beforeState, afterState, memo)
	}
	
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[WithSound]}
	override def equals(other:Any):Boolean = {
		this.canEquals(other) && other.asInstanceOf[WithSound].canEquals(this) &&
				this.base == other.asInstanceOf[WithSound].base
	}
	override def hashCode:Int = base.hashCode * 7 + 37
	
	override def toString:String = base.toString + " with " + this.getClass.getName

}
