package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{RectangularField => Field, StartOfTurn, EndOfTurn}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import javax.sound.sampled.AudioSystem

/**
 *
 *
 * @author Raymond Dodge
 * @version 17 Sept 2012
 * @version 2012 Nov 30 - implementing canEquals, equals, hashCode and toString
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
		
		player.tokens.tokens.flatten.foreach{_.reactions += WithSound.AttackSoundReaction}
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
 */
object WithSound
{
	val PREFIX = "/com/rayrobdod/deductionTactics/soundView/"
	
	/**
	 * A reaction that plays an audio clip when an action occurs.
	 */
	object AttackSoundReaction extends Reaction
	{
		def apply(e:Event) = {e match {
			case AttackForDamage(_, element, kind, _) => {
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
			case _ => {}
		}}
		
		def isDefinedAt(e:Event) = {e match {
			case x:AttackForDamage => true
			case x:AttackForStatus => true
			case _ => false
		}}
	}
}
