package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.Token
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.BeSelected
import scala.swing.Reactions
import scala.swing.event.Event

/**
 * @author Raymond Dodge
 * @version 14 Feb 2012
 * @version 03 Nov 2012 - moved from com.rayrobdod.deductionTactics.test to com.rayrobdod.deductionTactics.swingView
 */
class UnselectOtherTokens(token:Token, otherTokens:Seq[Token])
		extends Reactions.Reaction
{
	def apply(event:Event) = {
		(otherTokens diff Seq[Token](token)).foreach{_ ! BeSelected(false)}
	}
	
	def isDefinedAt(e:Event) = {e match {
		case BeSelected(true) => true
		case _ => false
	}}
}
