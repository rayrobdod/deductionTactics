package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.Token
import scala.collection.immutable.Seq

/**
 * @author Raymond Dodge
 * @version 14 Feb 2012
 * @version 03 Nov 2012 - moved from com.rayrobdod.deductionTactics.test to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
 */
class UnselectOtherTokens(token:Token, otherTokens:Seq[Token])
		extends Function1[Boolean, Unit]
{
	def apply(b:Boolean):Unit = if (b) {
		(otherTokens diff Seq[Token](token)).foreach{_.beSelected(false)}
	}
}
