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

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import com.rayrobdod.boardGame.{RectangularField => Field, Space}
import javax.sound.sampled.AudioSystem

/**
 * Only has one sound so far...
 *
 * @author Raymond Dodge
 * @version a.5.0
 */
final class WithSound(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** Forwards command to base */
	def buildTeam = base.buildTeam
	
	/** */
	def initialize(player:Player, field:Field) = {
		base.initialize(player, field)
		
		player.tokens.tokens.flatten.foreach{
				_.beDamageAttackedReactions_+=(WithSound.AttackSoundReaction)
		}
	}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithRandomTeam]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithRandomTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithRandomTeam].base
	}
	override def hashCode = base.hashCode * 7 + 37
	
	override def toString = base.toString + " with " + this.getClass.getName

}

/**
 * A container for stateless anonymous inner classes
 *
 * @author Raymond Dodge
 * @version 17 Sept 2012
 * @version 2013 Aug 07 - ripples from rewriting Player
 */
object WithSound
{
	val PREFIX = "/com/rayrobdod/deductionTactics/soundView/"
	
	/**
	 * A reaction that plays an audio clip when an action occurs.
	 */
	object AttackSoundReaction extends Token.DamageAttackedReactionType {
		def apply(e:Element, k:Weaponkind, s:Space):Unit = {
			// TODO: sound depends on how hard the hit was?
			// TODO: sound depends on element and kind of attack
			// TODO: give some units a unique attack sound (very ?)
			
			val fileName = PREFIX + "Hits/Hit.wav"
			val file = this.getClass().getResource(fileName)
			
			val audioIS = AudioSystem.getAudioInputStream(file)
			val audioClip = AudioSystem.getClip()
			
			audioClip.open(audioIS)
			
			audioClip.start()
		}
	}
}
