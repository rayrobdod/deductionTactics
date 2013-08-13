package com.rayrobdod.deductionTactics
package ai

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction

//import com.rayrobdod.deductionTactics.{PlayerAI, Player,
//		CannonicalTokenClass, Token, RequestAttackForDamage, RequestMove}
import com.rayrobdod.boardGame.{EndOfTurn, Space, TokenMovementCost,
			PhysicalStrikeCost}
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.collection.mutable.{MultiMap, Map => MMap, HashMap, Set => MSet}
import LoggerInitializer.{findWeaknessAILogger => Logger}
import java.util.logging.Level
import scala.swing.event.Event
import scala.swing.Reactions.Reaction

/**
 * 
 *
 * @author Raymond Dodge
 * @version 08 Jun 2012
 * @version 27 Jun 2012
 */
class FindWeaknessAI extends PlayerAI
{
	// Book keeping
	
	case class AttackAttributes(
		val attacker:AttackRecord,
		val damageDealt:Int
	)
	
	case class AttackRecord(
		val element:Element,
		val kind:Weaponkind,
		val dir:Seq[Direction],
		val status:Option[Status]
	) extends Event
	
	case class HidingAttributes(
		var status:Boolean = true,
		var speed:Boolean = true,
		var range:Boolean = true
	)
	
	val enemiesAndAttacks = new HashMap[MirrorToken, MSet[AttackAttributes]] with MultiMap[MirrorToken, AttackAttributes]
	val hidingAttributes = new HashMap[CannonicalToken, HidingAttributes]()
	private def showingSpeed(token:CannonicalToken) = {
		if (hidingAttributes(token).speed) {3} else {token.tokenClass.speed.get}
	}
	private def showingRange(token:CannonicalToken) = {
		if (hidingAttributes(token).range) {1} else {token.tokenClass.range.get}
	}
	class DamageTaken(token:MirrorToken) extends Reaction
	{
		private var lastCheck = token.currentHitpoints
		def difference() = {
			val returnValue = lastCheck - token.currentHitpoints
			lastCheck = token.currentHitpoints
			returnValue
		}
		
		def apply(e:Event) = {e match {
			case AttackForDamage(_, elem, kind, from) => {
				val dir = Directions.pathDirections(from, token.currentSpace)
				token ! AttackRecord(elem, kind, dir, token.currentStatus)
			}
			case x:AttackRecord => {
				enemiesAndAttacks addBinding (token, AttackAttributes(x, difference()))
			}
		}}
		
		def isDefinedAt(e:Event) = {e match {
			case x:AttackForDamage => true
			case x:AttackRecord => true
			case _ => false
		}}
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
	
	
	
	// per turn stuff
	def projectedDamageDone(attacker:Token, defender:Token,
			attackerSpace:Space, defenderSpace:Space) = {
		val attackerClass = attacker.tokenClass
		val defenderClass = defender.tokenClass
		val baseDamage = 8
		
		val weaponMultiplier:Float = (defenderClass.weakWeapon.map({(x:Weaponkind, y:Option[Float]) =>
				((Option(x), y))
		}.tupled) + ((None, None))).apply(attackerClass.atkWeapon).getOrElse(1f)
		
		val elementMultiplier:Float = defenderClass.atkElement.map{(x:Element) =>
			x.damageModifier(attackerClass.atkElement.getOrElse(x))
		}.getOrElse(1f)
		val statusMultiplier:Float = if (defenderClass.weakStatus.map{_ == defender.currentStatus}.getOrElse{false}) {2} else {1}
		val dirMultiplier:Float = defenderClass.weakDirection.map{
			_.weaknessMultiplier(Directions.pathDirections(attackerSpace, defenderSpace))
		}.getOrElse(1f)
		
		(baseDamage * weaponMultiplier * elementMultiplier * statusMultiplier * dirMultiplier).intValue 
	}
	
	def takeTurn(player:Player):Any =
	{
		player.tokens.aliveMyTokens.foreach{(token:CannonicalToken) =>
			
			val usedSpeed = showingSpeed(token)
			val maxSpeed = token.tokenClass.speed.get
			
			val usedRange = showingRange(token)
			val maxRange = token.tokenClass.range.get
			
			val currentSpace = token.currentSpace
			val possibleTargets = player.tokens.aliveOtherTokens.flatten.map{Some(_)} :+ None
			
			// Priority: projected damage done - projected retaliation
			val movingTo = moveRangeOf(token).flatMap{(x:Space) =>
				possibleTargets.map{(y:Option[Token]) => (x, y)}
			}.maxBy({(toSpace:Space, target:Option[Token]) =>
				Logger.entering(this.getClass.getName, "takeTurn", Tuple2(toSpace, target))
				
				val targetSpace = target.map{_.currentSpace}
				
				val returnValue =
					(if (currentSpace.distanceTo(toSpace, null, TokenMovementCost) > usedSpeed) {-500} else {0}) +
					targetSpace.map{(x:Space) => if (toSpace.distanceTo(x, null, PhysicalStrikeCost) > usedRange) {-500} else {0}}.getOrElse(0) +
					((
						target.map{projectedDamageDone(token, _, toSpace, targetSpace.get)}.getOrElse(8) * 2 +
						-target.map{projectedDamageDone(_, token, targetSpace.get, toSpace)}.getOrElse(8)
					) * 10 / targetSpace.map{(x:Space) => toSpace.distanceTo(x, null, PhysicalStrikeCost)}.getOrElse(10))
				
				
				Logger.exiting(this.getClass.getName, "takeTurn", returnValue)
				returnValue
				
			}.tupled)
			
			player ! RequestMove(token, movingTo._1)
			movingTo._2.foreach{player ! RequestAttackForDamage(token, _)}
			
		}
		
		player ! EndOfTurn
	}
	
	
	
	
	
	// useless mandatory stuff
	def buildTeam = randomTeam()
	
	def canEquals(other:Any) = {other.isInstanceOf[FindWeaknessAI]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[FindWeaknessAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
