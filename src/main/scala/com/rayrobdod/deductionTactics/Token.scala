package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import scala.collection.mutable.Buffer
import com.rayrobdod.boardGame.{Space,
		Token => BoardGameToken}

/**
 * 
 * @author Raymond Dodge
 * @version 19 Jan 2012ish
 * @version 12 Feb 2012 - made the canmove/attackthisturn items visible
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
 */
trait Token extends BoardGameToken
{
	def currentHitpoints:Int
	def currentStatus:Option[Status]
	def currentStatusTurnsLeft:Int
	def tokenClass:TokenClass
	
	def canMoveThisTurn:Int
	def canAttackThisTurn:Boolean
	
	final val maximumHitpoints:Int = 256
	final val baseDamage:Int = 8
	
	
	/**  figure out how to not need these */
	def beAttacked(elem:Element, kind:Weaponkind, from:Space);
	/**  figure out how to not need these */
	def beAttacked(status:Status, from:Space);
	
	
	
	
	
	
	
	private val diedReactions:Buffer[() => Unit] = Buffer.empty;
	def diedReactions_+=(a:() => Unit) = {diedReactions += a}
	def diedReactions_-=(a:() => Unit) = {diedReactions -= a}
	protected def triggerDiedReactions() = {diedReactions.foreach{a => a()}}
	
	private val updateReactions:Buffer[() => Unit] = Buffer.empty;
	def updateReactions_+=(a:() => Unit) = {updateReactions += a}
	def updateReactions_-=(a:() => Unit) = {updateReactions -= a}
	protected def triggerUpdateReactions() = {updateReactions.foreach{a => a()}}
	
	private val beDamageAttackedReactions:Buffer[Token.DamageAttackedReactionType] = Buffer.empty;
	def beDamageAttackedReactions_+=(a:Token.DamageAttackedReactionType) = {beDamageAttackedReactions += a}
	def beDamageAttackedReactions_-=(a:Token.DamageAttackedReactionType) = {beDamageAttackedReactions -= a}
	protected def triggerBeDamageAttackedReactions(b:Element, c:Weaponkind, d:Space) = {beDamageAttackedReactions.foreach{a => a(b,c,d)}}

	private val beStatusAttackedReactions:Buffer[Token.StatusAttackedReactionType] = Buffer.empty;
	def beStatusAttackedReactions_+=(a:Token.StatusAttackedReactionType) = {beStatusAttackedReactions += a}
	def beStatusAttackedReactions_-=(a:Token.StatusAttackedReactionType) = {beStatusAttackedReactions -= a}
	protected def triggerBeStatusAttackedReactions(a:Status, b:Space) = {beStatusAttackedReactions.foreach{c => c(a,b)}}
}

object Token {
	trait DamageAttackedReactionType {
		def apply(atkElem:Element, atkKind:Weaponkind, attackerSpace:Space):Unit;
	}
	
	trait StatusAttackedReactionType {
		def apply(atkStatus:Status, attackerSpace:Space):Unit;
	}
}
