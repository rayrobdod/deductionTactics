package com.rayrobdod.deductionTactics
package ai

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import com.rayrobdod.boardGame.{RectangularField => Field, Space}
import javax.sound.sampled.AudioSystem

/**
 *
 *
 * @author Raymond Dodge
 * @version 17 Sept 2012
 * @version 2012 Nov 30 - implementing canEquals, equals, hashCode and toString
 * @version 2013 Aug 07 - ripples from rewriting Player
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
