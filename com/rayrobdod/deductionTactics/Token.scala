package com.rayrobdod.deductionTactics

import scala.swing.event.Event
import scala.swing.Reactions.Reaction
import Statuses.Status
import com.rayrobdod.boardGame.{Space => BoardGameSpace,
		Token => BoardGameToken, RectangularSpace, StartOfTurn}

/**
 * 
 * @author Raymond Dodge
 * @version 19 Jan 2012ish
 * @version 12 Feb 2012 - made the canmove/attackthisturn items visible
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
}
