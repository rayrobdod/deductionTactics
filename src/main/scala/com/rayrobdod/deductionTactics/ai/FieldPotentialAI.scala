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

import com.rayrobdod.boardGame.{Space, RectangularField, StrictRectangularSpace}
import scala.collection.immutable.{Map, Set, Seq}
import LoggerInitializer.{fieldPotentialAiLogger => Logger}
import java.util.logging.Level

/**
 * An AI that first uses fuzzy logic to determine who, if anyone, to attack,
 * and then uses potential fields to determine how to attack and retreat.
 * @version a.6.0
 */
final class FieldPotentialAI extends PlayerAI
{
	/** [[com.rayrobdod.deductionTactics.ai.randomTeam]] */
	override def selectTokenClasses(size:Int):Seq[TokenClass] = randomTeam(size)
	/** chooses a subset of selectedTokenClasses randomly */
	override def narrowTokenClasses(
				selectedClasses:Seq[Seq[TokenClass]],
				maxResultSize:Int,
				myPlayerIndex:Int
	):Seq[TokenClass] = scala.util.Random.shuffle(selectedClasses(myPlayerIndex)).take(maxResultSize)
	
	
	override def takeTurn(player:Int, gameState:GameState, memo:Memo):Seq[GameState.Action] = {
		gameState.tokens.alivePlayerTokens(player).flatMap{(myToken:Token) =>
			// Stage 1 : decide who if anyone to attack
			val target:Option[Token] = {
				
				val otherToken = gameState.tokens.aliveNotPlayerTokens(player).flatten.map{(x:Token) =>
					import PotentialFieldAI$FuzzyLogic.shouldEngage;
					val susp = memo.suspicions(gameState.tokens.indexOf(x))
					
					val a = shouldEngage(myToken, x, susp, gameState.tokens);
					((x, a))
				}.maxBy{_._2}
				
				Logger.finer("PlayerAI will target someone: " + otherToken._2)
				
				Some(otherToken).filter{_._2 > .2f}.map{_._1}
			}
			
			// Stage 2 : pre-attack move
			val move1 = target.map{(x:Token) =>
				GameState.TokenMove(myToken,
					PotentialFieldAI$AttackField(
							myToken,
							x,
							memo.suspicions(gameState.tokens.indexOf(x)),
							gameState.tokens,
							memo.asInstanceOf[SimpleMemoWithDebugWindow].showFieldData
					)
				)
			}.toSeq
			
			// Stage 3 : attack
			val move2 = target.map{(x:Token) =>
				GameState.TokenAttackDamage(myToken, x)
			}.toSeq
			
			// Stage 4 : post-attack move
			val move3 = GameState.TokenMove(myToken,
				PotentialFieldAI$RetreatField(myToken, gameState.tokens, memo.suspicions, player)
			)
			
			move1.toSeq ++ move2.toSeq :+ move3
		} :+ GameState.EndOfTurn
	}
	
	
		
	override def initialize(player:Int, initialState:GameState):Memo = {
		// setup recorders
		
		if (Logger.isLoggable(Level.FINER)) {
			import java.awt.event.{WindowEvent, WindowAdapter}
			import javax.swing.{JFrame, JLabel}
			
			val list = initialState.tokens
			
			val frame:JFrame = new JFrame("PotentialFieldAI$RetreatField") 
			frame.getContentPane.setLayout(
				new java.awt.GridLayout(
					initialState.board.map{_._1._1}.max,
					initialState.board.map{_._1._2}.max
				)
			)
			
			val token = list.alivePlayerTokens(player)(0)
			val labels = initialState.board.map{(x) => (x._2, new JLabel("XXXX"))}.toMap[Space[SpaceClass], JLabel]
			
			labels.foreach{(x) => frame.getContentPane.add(x._2)}
			frame.setVisible(true);
			frame.pack();
			
			
			new SimpleMemoWithDebugWindow(showFieldData = {(a:Space[SpaceClass],c:String) =>
				labels(a).setText(c)
			})
		} else {
			new SimpleMemoWithDebugWindow
		}
	}
	
	override def notifyTurn(
		player:Int,
		action:GameState.Result,
		beforeState:GameState,
		afterState:GameState,
		memo:Memo
	):Memo = memo
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[FieldPotentialAI]}
	override def equals(other:Any):Boolean = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[FieldPotentialAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode:Int = 23
	
	override def toString:String = this.getClass.getName
}

private[ai] object PotentialFieldAI$FuzzyLogic {
	import java.lang.Float.{POSITIVE_INFINITY => INF}
	
	class Distance(self:Token, other:Token, list:ListOfTokens) {
		private val speed:Float = self.tokenClass.map{_.speed}.get;
		private val range:Float = self.tokenClass.map{_.range}.get;
		// the '1000' is the cost for entering the token's current space. Technicalities.
		private val distance = self.currentSpace.distanceTo(other.currentSpace, new MoveToCostFunction(self, list)) - 999;
		
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
	
	class Advantage(selfT:Token, otherT:Token, otherClass:TokenClassSuspicion) {
		private val selfClass = selfT.tokenClass.get;
		
		val (self:Float, opponent:Float, even:Float, unknown:Float) = {
			var strength:Float = 3;
			var m_self     = 0f;
			var m_opponent = 0f;
			var m_even     = 0f;
			var m_unknown  = 0f;
			
			if (otherClass.atkElement.isDefined) {
				val elemAd = selfClass.atkElement.damageModifier(otherClass.atkElement.get)
				
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
			
			if (otherClass.weakWeapon(selfClass.atkWeapon).isDefined) {
				val weapAd = otherClass.weakWeapon(selfClass.atkWeapon).get
				
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
				val weapAd = selfClass.weakWeapon(otherClass.atkWeapon.get)
				
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
	
	
	
	
	def shouldEngage(selfT:Token, otherT:Token, otherSusp:TokenClassSuspicion, list:ListOfTokens):Float = {
		Logger.entering("com.rayrobdod.deductionTactics.ai.PotentialFieldAI$FuzzyLogic", "shouldEngage")
		
		val distance = new Distance(selfT, otherT, list);
		val advantage = new Advantage(selfT, otherT, otherSusp);
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
	def apply(selfT:Token, otherT:Token, otherSusp:TokenClassSuspicion, tokens:ListOfTokens, showFieldData:Function2[Space[SpaceClass], String, Any] = {(a,b) => }):Space[SpaceClass] = {
		Logger.entering("com.rayrobdod.deductionTactics.ai.PotentialFieldAI$AttackField", "apply")
		
		val eligibleSpaces:Set[Space[SpaceClass]] = otherT.currentSpace.spacesWithin(
				selfT.tokenClass.get.range, new AttackCostFunction(selfT, tokens)
		)
		
		val priorities:Set[(Space[SpaceClass], Float)] = eligibleSpaces.map{(space:Space[SpaceClass]) =>
			val b = if (otherSusp.weakDirection.isDefined) {
				otherSusp.weakDirection.get.weaknessMultiplier(
					Directions.pathDirections(
							selfT.currentSpace.asInstanceOf[StrictRectangularSpace[SpaceClass]],
							otherT.currentSpace.asInstanceOf[StrictRectangularSpace[SpaceClass]],
							selfT,
							tokens
					)
				)
			} else {
				// figure out the weakDirection
				1;
			}
			
			val c:Float = b * selfT.currentSpace.distanceTo(
				otherT.currentSpace, new AttackCostFunction(selfT, tokens)
			);
			
			((space, c));
		}
		
		if (Logger.isLoggable(Level.FINER)) {
			priorities.map{a => ((a._1, a._2.toString))}.foreach{showFieldData.tupled}
			
			val str = priorities.map{_._2}.foldLeft(""){(str, x) => str + x + ' '}
			Logger.finer(str);
		}
		
		priorities.maxBy{_._2}._1
	}
}

private[ai] object PotentialFieldAI$RetreatField {
	def apply(selfT:Token, tokens:ListOfTokens, susps:Map[(Int, Int), TokenClassSuspicion], player:Int):Space[SpaceClass] = {
		priorities(selfT, tokens, susps, player).maxBy{_._2}._1
	}
	
	def priorities(selfT:Token, tokens:ListOfTokens, susps:Map[(Int, Int), TokenClassSuspicion], player:Int):Map[Space[SpaceClass], Int] = {
		this.priorities(selfT, tokens, susps, player, selfT.canMoveThisTurn)
	}
	
	def priorities(selfT:Token, tokens:ListOfTokens, susps:Map[(Int, Int), TokenClassSuspicion], player:Int, range:Int):Map[Space[SpaceClass], Int] = {
		Logger.entering("com.rayrobdod.deductionTactics.ai.PotentialFieldAI$RetreatField", "priorities")
		
		val eligibleSpaces:Set[Space[SpaceClass]] = moveRangeOf(selfT, tokens)
		
		val prioritiesEnemy:Seq[Set[(Space[SpaceClass], Int)]] = tokens.aliveNotPlayerTokens(player).flatten.map{(otherT:Token) =>
			eligibleSpaces.map{(space:Space[SpaceClass]) =>
				val otherSusp = susps(tokens.indexOf(otherT))
				
				val range = otherSusp.range.getOrElse(1)
				val speed = otherSusp.speed.getOrElse(3)
				val distance = otherT.currentSpace.distanceTo(
						space, new MoveToCostFunction(selfT, tokens));
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
		val prioritiesHerd:Seq[Set[(Space[SpaceClass], Int)]] = tokens.alivePlayerTokens(player).map{(otherT:Token) =>
			if (otherT == selfT) {
				Set.empty[(Space[SpaceClass], Int)]
			} else {
				eligibleSpaces.map{(space:Space[SpaceClass]) => 
					val distance = space.distanceTo(
							otherT.currentSpace, new MoveToCostFunction(selfT, tokens)) - 999;
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
		
		val priorities = (prioritiesEnemy ++ prioritiesHerd).foldLeft(Map.empty[Space[SpaceClass], Int]){
			(a:Map[Space[SpaceClass], Int], b:Set[(Space[SpaceClass], Int)]) => b.foldLeft(a){
				(y:Map[Space[SpaceClass], Int], x:(Space[SpaceClass], Int)) =>
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


/** @version a.6.0 */
final class SimpleMemoWithDebugWindow(
	val attacks:Seq[GameState.Result] = Nil,
	val suspicions:Map[(Int, Int), TokenClassSuspicion] = Map.empty.withDefaultValue(new TokenClassSuspicion),
	val showFieldData:Function2[Space[SpaceClass], String, Any] = {(a,c) => }
) extends Memo {
	def addAttack(r:GameState.Result):SimpleMemoWithDebugWindow =
			new SimpleMemoWithDebugWindow(r +: attacks, suspicions, showFieldData)
	def updateSuspicion(key:(Int, Int), value:TokenClassSuspicion):SimpleMemoWithDebugWindow =
			new SimpleMemoWithDebugWindow(attacks, suspicions + ((key, value)), showFieldData)
}
