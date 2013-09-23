package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{Space, TokenMovementCost, PhysicalStrikeCost}
import com.rayrobdod.boardGame.{RectangularField => Field}
import scala.collection.immutable.{Map, Set, Seq}
import scala.collection.mutable.{Map => MMap}
import LoggerInitializer.{fieldPotentialAiLogger => Logger}
import java.util.logging.Level

/**
 * An AI that first uses fuzzy logic to determine who, if anyone, to attack,
 * and then uses potential fields to determine how to attack and retreat.
 * @author Raymond Dodge
 */
final class FieldPotentialAI extends PlayerAI
{
	def takeTurn(player:Player) {
		player.tokens.aliveMyTokens.foreach{(myToken:CannonicalToken) =>
			// Stage 1 : decide who if anyone to attack
			val target:Option[MirrorToken] = {
				
				val otherToken = player.tokens.aliveOtherTokens.flatten.map{(x:MirrorToken) =>
					import PotentialFieldAI$FuzzyLogic.shouldEngage;
					
					val a = shouldEngage(myToken, x);
					((x, a))
				}.maxBy{_._2}
				
				Logger.finer("PlayerAI will target someone: " + otherToken._2)
				
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
		
		if (Logger.isLoggable(Level.FINER)) {
			import java.awt.event.{WindowEvent, WindowAdapter}
			import javax.swing.{JFrame, JLabel}
			
			val frame = new JFrame("PotentialFieldAI$RetreatField") 
			frame.getContentPane.setLayout(
				new java.awt.GridLayout(
					field.spaces(0).size,
					field.spaces.size
				)
			)
			
			val token = player.tokens.myTokens(0)
			val labels = field.spaces.flatten.map{(x) => (x, new JLabel("XXXX"))}
			
			labels.foreach{(x) => frame.getContentPane.add(x._2)}
			player.addStartTurnReaction{new Player.StartTurnReactionType() {
				def apply() {
					val c = PotentialFieldAI$RetreatField.priorities(token, player.tokens, field.spaces.size)
					labels.foreach{(x) => x._2.setText(c.getOrElse(x._1, "X").toString)}
				}
			}}
			frame.setVisible(true);
			frame.pack();
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
		// the '1000' is the cost for entering the token's current space. Technicalities.
		private val distance = self.currentSpace.distanceTo(other.currentSpace, self, TokenMovementCost) - 999;
		
		val adjacent   = trapezoidalFunction(distance, -INF, -INF, range, range + .1f);
		val close      = trapezoidalFunction(distance, range, range, speed/2, speed)
		val far        = trapezoidalFunction(distance, speed/2, speed, speed, speed+range)
		val outOfRange = trapezoidalFunction(distance, speed, speed + range, INF, INF);
		
		override def toString() = {
			"Distance[" +
					  "speed: " + speed +
					"; range: " + range +
					"; distance: " + distance +
					"; adjacent: " + adjacent +
					"; close: " + close +
					"; far: " + far +
					"; outOfRange: " + outOfRange +
			"]"
		}
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
			
			// Other things. including herd size, direction, status, 
			
			
			(m_self / strength, m_opponent / strength, m_even / strength, m_unknown / strength);
		}
	}
	
	class Health(self:Token) {
		private val hp = self.currentHitpoints;
		
		val uninjured   = trapezoidalFunction(hp, 128, 192, INF, INF);
		val strong      = trapezoidalFunction(hp, 64, 96, 128, 192)
		val weak        = trapezoidalFunction(hp, 16, 32, 64, 96)
		val bloodied    = trapezoidalFunction(hp, -INF, -INF, 16, 32);
		
		private val sum = (uninjured + strong + weak + bloodied)
		val value = (bloodied * .25f + weak * .5f + strong * .75f + uninjured) / sum
	}
	
	/**
	 * Evaluates x along the trapezoidal function crated by min, peakMin, peakMax and max.
	 * @pre max >= peakMax >= peakMin >= min
	 */
	def trapezoidalFunction(x:Float, min:Float, peakMin:Float, peakMax:Float, max:Float):Float = {
		if (x <= min) { 0 }
		else if (x <= peakMin) { (x - min) / (peakMin - min) }
		else if (x <= peakMax) { 1 }
		else if (x <= max) { (max - x) / (max - peakMax) }
		else { 0 }
	}
	
	
	
	
	def shouldEngage(selfT:CannonicalToken, otherT:MirrorToken):Float = {
		Logger.entering("com.rayrobdod.deductionTactics.ai.PotentialFieldAI$FuzzyLogic", "shouldEngage")
		
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
		
		Logger.finer(distance.toString)
		
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
	def apply(selfT:CannonicalToken, otherT:MirrorToken):Space = {
		Logger.entering("com.rayrobdod.deductionTactics.ai.PotentialFieldAI$AttackField", "apply")
		
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
		
		if (Logger.isLoggable(Level.FINER)) {
			val str = priorities.map{_._2}.foldLeft(""){(str, x) => str + x + ' '}
			Logger.finer(str);
		}
		
		priorities.maxBy{_._2}._1
	}
}

private[ai] object PotentialFieldAI$RetreatField {
	def apply(selfT:CannonicalToken, tokens:PlayerListOfTokens):Space = {
		priorities(selfT, tokens).maxBy{_._2}._1
	}
	
	def priorities(selfT:CannonicalToken, tokens:PlayerListOfTokens):Map[Space, Int] = {
		this.priorities(selfT, tokens, selfT.canMoveThisTurn)
	}
	
	def priorities(selfT:CannonicalToken, tokens:PlayerListOfTokens, range:Int):Map[Space, Int] = {
		Logger.entering("com.rayrobdod.deductionTactics.ai.PotentialFieldAI$RetreatField", "priorities")
		
		val eligibleSpaces:Set[Space] = Option(selfT.currentSpace).map{_.spacesWithin(
				range, selfT, TokenMovementCost
		)}.getOrElse( Set.empty )
		
		val prioritiesEnemy:Seq[Set[(Space, Int)]] = tokens.aliveOtherTokens.flatten.map{(otherT:MirrorToken) =>
			eligibleSpaces.map{(space:Space) => 
				val range = otherT.tokenClass.range.getOrElse(1)
				val speed = otherT.tokenClass.speed.getOrElse(3)
				val distance = otherT.currentSpace.distanceTo(
						space, otherT, TokenMovementCost);
				//
				val pri =
					if (distance <= range) { 0 }
					else if (distance <= range + speed + 1) {
						10 - (((range + speed) - distance))
					} else {
						math.max(10 - (distance - range + speed), 0)
					}
				
				(( space, pri ))
			}
		}
		val prioritiesHerd:Seq[Set[(Space, Int)]] = tokens.aliveMyTokens.map{(otherT:CannonicalToken) =>
			if (otherT == selfT) {
				Set.empty[(Space, Int)]
			} else {
				eligibleSpaces.map{(space:Space) => 
					val distance = space.distanceTo(
							otherT.currentSpace, selfT, TokenMovementCost) - 999;
					//
					val pri = distance match {
						case 1 => 2
						case 2 => 3
						case 3 => 1
						case _ => 0
					}
					
					(( space, pri ))
				}
			}
		}
		
		if (Logger.isLoggable(Level.FINEST)) {
			val str1 = prioritiesEnemy.flatten.map{_._2}.foldLeft("Enemy: "){(str, x) => str + x + ' '}
			Logger.finer(str1);
			
			val str2 = prioritiesHerd.flatten.map{_._2}.foldLeft("Herd:  "){(str, x) => str + x + ' '}
			Logger.finer(str2);
		}
		
		val priorities = (prioritiesEnemy ++ prioritiesHerd).foldLeft(Map.empty[Space, Int]){
			(a:Map[Space, Int], b:Set[(Space, Int)]) => b.foldLeft(a){
				(y:Map[Space, Int], x:(Space, Int)) =>
					y + (( x._1, a.getOrElse(x._1, 0) + x._2 ))
			}
		}
		
		if (Logger.isLoggable(Level.FINER)) {
			val str = priorities.map{_._2}.foldLeft(""){(str, x) => str + x + ' '}
			Logger.finer(str);
		}
		
		priorities;
	}
}
