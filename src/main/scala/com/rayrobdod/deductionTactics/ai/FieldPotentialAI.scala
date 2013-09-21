package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{Space, TokenMovementCost, PhysicalStrikeCost}
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace => Space}
import scala.collection.immutable.{Map, Set, Seq}
import scala.collection.mutable.{Map => MMap}
import LoggerInitializer.{blindAttackAILogger => Logger}
import java.util.logging.Level

/**
 * An AI that first uses fuzzy logic to determine who, if anyone, to attack,
 * and then uses potential fields to determine how to attack and retreat.
 * @author Raymond Dodge
 */
class FieldPotentialAI extends PlayerAI
{
	def takeTurn(player:Player) {
		player.tokens.aliveMyTokens.foreach{(myToken:CannonicalToken) =>
			// Stage 1 : decide who if anyone to attack
			val target:Option[MirrorToken] = {
				
				val otherToken = player.tokens.aliveOtherTokens.flatten.map{(x:MirrorToken) =>
					import PotentialFieldAI$FuzzyLogic.shouldEngage;
					
					((x, shouldEngage(myToken, x) ))
				}.maxBy{_._2}
				
				Some(otherToken).filter{_._2 > .2f}.map{_._1}
			}
			
			// Stage 2 : pre-attack move
			target.foreach{(x:MirrorToken) =>
				myToken.requestMoveTo(
					PotentialFieldAI$AttackField(myToken, x)
				)
			}
			
			// Stage 3 : attack
			target.foreach{(x:MirrorToken) => 
				myToken.tryAttackDamage(x)
			}
			
			// Stage 4 : post-attack move
			myToken.requestMoveTo(
				PotentialFieldAI$RetreatField(myToken, player.tokens)
			)
		}
	}
	
	
	def buildTeam = randomTeam()
		
	def initialize(player:Player, field:Field) = {
		player.tokens.otherTokens.flatten.foreach{(token:MirrorToken) =>
			token.beDamageAttackedReactions_+=(new StandardObserveAttacks(token, player.tokens))
			token.beStatusAttackedReactions_+=(new StandardObserveAttacks(token, player.tokens))
			
			val movement = new StandardObserveMovement(token)
			token.moveReactions_+=(movement)
			player.addStartTurnReaction(movement)
		}
	}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[FieldPotentialAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[FieldPotentialAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 23
	
	override def toString = this.getClass.getName
}

private[ai] object PotentialFieldAI$FuzzyLogic {
	import java.lang.Float.{POSITIVE_INFINITY => INF}
	
	class Distance(self:CannonicalToken, other:MirrorToken) {
		private val speed:Float = self.tokenClass.speed.get;
		private val range:Float = self.tokenClass.range.get;
		private val distance = self.currentSpace.distanceTo(other.currentSpace, self, TokenMovementCost);
		
		val adjacent   = trapezoidalFunction(distance, -INF, -INF, 0, range);
		val close      = trapezoidalFunction(distance, .5f, math.min(range + .5f, speed/2), speed/2, speed)
		val far        = trapezoidalFunction(distance, speed/2, speed, speed, speed+range)
		val outOfRange = trapezoidalFunction(distance, speed, speed + range, INF, INF);
	}
	
	class Advantage(selfT:CannonicalToken, otherT:MirrorToken) {
		private val selfClass = selfT.tokenClass;
		private val otherClass = otherT.tokenClass;
		
		val (self:Float, opponent:Float, even:Float, unknown:Float) = {
			var strength:Float = 3;
			var m_self     = 0f;
			var m_opponent = 0f;
			var m_even     = 0f;
			var m_unknown  = 0f;
			
			if (otherClass.atkElement.isDefined) {
				val elemAd = selfClass.atkElement.get.damageModifier(otherClass.atkElement.get)
				
				if (elemAd >= 1f) {
					m_self = m_self + elemAd - 1f;
					m_even = m_even + 2f - elemAd;
				} else {
					m_opponent = m_opponent + 2f - (2 * elemAd);
					m_even = m_even + (2 * elemAd) - 1f;
				}
			} else {
				m_unknown = m_unknown + 1;
			}
			
			if (otherClass.weakWeapon(selfClass.atkWeapon.get).isDefined) {
				val weapAd = otherClass.weakWeapon(selfClass.atkWeapon.get).get
				
				if (weapAd >= 1f) {
					m_self = m_self + weapAd - 1f;
					m_even = m_even + 2f - weapAd;
				} else {
					m_opponent = m_opponent + 2f - (2 * weapAd);
					m_even = m_even + (2 * weapAd) - 1f;
				}
			} else {
				m_unknown = m_unknown + 1;
			}
			
			if (otherClass.atkWeapon.isDefined) {
				val weapAd = selfClass.weakWeapon(otherClass.atkWeapon.get).get
				
				if (weapAd >= 1f) {
					m_opponent = m_opponent + weapAd - 1f;
					m_even = m_even + 2f - weapAd;
				} else {
					m_self = m_self + 2f - (2 * weapAd);
					m_even = m_even + (2 * weapAd) - 1f;
				}
			} else {
				m_unknown = m_unknown + 1;
			}
			
			// Other things. including t
			
			
			(m_self / strength, m_opponent / strength, m_even / strength, m_unknown / strength);
		}
	}
	
	class Health(self:Token) {
		private val hp = self.currentHitpoints;
		
		val uninjured   = trapezoidalFunction(hp, 128, 192, INF, INF);
		val strong      = trapezoidalFunction(hp, 64, 96, 128, 192)
		val weak        = trapezoidalFunction(hp, 16, 32, 64, 96)
		val bloodied    = trapezoidalFunction(hp, -INF, -INF, 16, 32);
		
		
		val value = bloodied * .25f + weak * .5f + strong * .75f + uninjured
	}
	
	/**
	 * Evaluates x along the trapezoidal function crated by min, peakMin, peakMax and max.
	 * @pre max >= peakMax >= peakMin >= min
	 */
	def trapezoidalFunction(x:Float, min:Float, peakMin:Float, peakMax:Float, max:Float):Float = {
		if (x < min) { 0 }
		else if (x < peakMin) { (x - min) / (peakMin - min) }
		else if (x < peakMax) { 1 }
		else if (x < max) { (x - peakMax) / (max - peakMax) }
		else { 0 }
	}
	
	
	
	
	def shouldEngage(selfT:CannonicalToken, otherT:MirrorToken):Float = {
		val distance = new Distance(selfT, otherT);
		val advantage = new Advantage(selfT, otherT);
		val selfHealth = new Health(selfT);
		val otherHealth = new Health(otherT);
		
		implicit def floatToFuzzyLogic(f:Float) = new FuzzyLogic(f)
		class FuzzyLogic(f:Float) {
			def unary_! = 1f - f;
			def &&(o:Float) = f * o;
			def ||(o:Float) = math.max(f, o);
		}
		
		/** unit thinks it can beat the other unit */
		val confidence = (
				selfHealth.value + advantage.self +
				advantage.even / 2 +
				advantage.unknown / 3
		) - otherHealth.value;
		
		
		/* Attack if is Adjacent or
		 * is Close and slight confidence or
		 * is Far and strong confidence
		 */
		(
			(distance.adjacent) ||
			(distance.close && confidence) ||
			(distance.far && confidence / 2)
		)
	}
}

private[ai] object PotentialFieldAI$AttackField {
	def apply(selfT:CannonicalToken, otherT:MirrorToken) = {
		
		val eligibleSpaces:Set[Space] = otherT.currentSpace.spacesWithin(
				selfT.tokenClass.range.get, selfT, PhysicalStrikeCost
		)
		
		val priorities:Set[(Space, Float)] = eligibleSpaces.map{(space:Space) =>
			val b = if (otherT.tokenClass.weakDirection.isDefined) {
				otherT.tokenClass.weakDirection.get.weaknessMultiplier(
					Directions.pathDirections(selfT.currentSpace, otherT.currentSpace)
				)
			} else {
				// figure out the weakDirection
				1;
			}
			
			val c:Float = b * selfT.currentSpace.distanceTo(
				otherT.currentSpace, selfT, PhysicalStrikeCost
			);
			
			((space, c));
		}
		
		priorities.maxBy{_._2}._1
	}
}

private[ai] object PotentialFieldAI$RetreatField {
	def apply(selfT:CannonicalToken, tokens:PlayerListOfTokens) = {
		
		val eligibleSpaces:Set[Space] = selfT.currentSpace.spacesWithin(
				selfT.canMoveThisTurn, selfT, TokenMovementCost
		)
		
		val prioritiesEnemy:Seq[Set[(Space, Int)]] = tokens.aliveOtherTokens.flatten.map{(otherT:MirrorToken) =>
			eligibleSpaces.map{(space:Space) => 
				val range = otherT.tokenClass.range.getOrElse(1)
				val speed = otherT.tokenClass.speed.getOrElse(3)
				val distance = otherT.currentSpace.distanceTo(
						space, otherT, TokenMovementCost);
				//
				val pri =
					if (distance <= range) { 0 }
					else if (distance <= range + speed) {
						12 - (((range + speed) - distance * 12) / (range + speed))
					} else {
						math.max(distance - range + speed, 0)
					}
				
				(( space, pri ))
			}
		}
		val prioritiesHerd:Seq[Set[(Space, Int)]] = tokens.aliveMyTokens.map{(otherT:CannonicalToken) =>
			eligibleSpaces.map{(space:Space) => 
				val distance = space.distanceTo(
						otherT.currentSpace, selfT, TokenMovementCost);
				//
				val pri = distance match {
					case 1 => 4
					case 2 => 7
					case 3 => 1
					case _ => 0
				}
				
				(( space, pri ))
			}
		}
		
		val priorities = (prioritiesEnemy ++ prioritiesHerd).foldLeft(Map.empty[Space, Int]){
			(a:Map[Space, Int], b:Set[(Space, Int)]) => a ++ b
		}
		
		priorities.maxBy{_._2}._1
	}
}
