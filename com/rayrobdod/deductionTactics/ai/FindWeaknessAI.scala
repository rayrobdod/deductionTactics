package com.rayrobdod.deductionTactics
package ai

//import com.rayrobdod.deductionTactics.{PlayerAI, Player,
//		CannonicalTokenClass, Token, RequestAttackForDamage, RequestMove}
import Directions.Direction
import com.rayrobdod.boardGame.{EndOfTurn, Space, TokenMovementCost}
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.collection.mutable.{MultiMap, Map => MMap, HashMap, Set => MSet}
//import LoggerInitializer.{blindAttackAILogger => Logger}
import java.util.logging.Level

/**
 * 
 *
 * @author Raymond Dodge
 * @version 08 Jun 2012
 */
class FindWeaknessAI extends PlayerAI
{
	case class AttackAttributes(
		val damageDealt:Int,
		val attacker:AttackRecord
	)
	
	case class AttackRecord(
		val element:Element,
		val kind:Weaponkind,
		val dir:Direction
	) extends Event
	
	case class HidingAttributes(
		var status:Boolean = true,
		var speed:Boolean = true,
		var range:Boolean = true
	)
	
	val enemiesAndAttacks = new HashMap[MirrorToken, MSet[AttackAttributes]] with MultiMap[MirrorToken, AttackAttributes]
	val hidingAttributes = new HashMap[CannonicalToken, HidingAttributes]()
	private def showingSpeed(token:CannonicalToken) = {
		if (hidingAttributes(token).speed) {3} else {token.tokenClass.speed}
	}
	private def showingRange(token:CannonicalToken) = {
		if (hidingAttributes(token).range) {1} else {token.tokenClass.range}
	}
	class DamageTaken(token:MirrorToken) extends Reaction = {
		private var lastCheck = token.currentHitPoints
		def difference() = {
			val returnValue = lastCheck - token.currentHitPoints
			lastCheck = token.currentHitPoints
			returnValue
		}
		
		def apply(e:Event) => {e match {
			case AttackForDamage(_, elem, kind, from) => {
				val dir = ??? 
				token ! AttackRecord(elem, kind, dir)
			}
			case AttackRecord(elem, kind, dir) => {
				enemiesAndAttacks 
			}
		}}
		
		def 
	}
	
	def initialize(player:Player, field:Field) = {
		player.tokens.otherTokens.flatten.foreach{(token:MirrorToken) =>
			token.reactions += new StandardObserveAttacks(token)
			
			val movement = new StandardObserveMovement(token)
			token.reactions += movement
			player.reactions += movement
			
			enemiesAndAttacks += ((token, MSet.empty))
		}
		
		player.tokens.myTokens.foreach{(token:CannonicalToken) =>
			hidingAttributes += ((token, HidingAttributes(true,
					token.tokenClass.speed.get > 3, token.tokenClass.range.get > 1)))
		}
	}
	
	def takeTurn(player:Player):Any =
	{
		
	}
	
	
	
	
	
	
	def buildTeam = randomTeam()
	
	def canEquals(other:Any) = {other.isInstanceOf[BlindAttackAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[BlindAttackAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
