package com.rayrobdod.deductionTactics

import scala.collection.mutable.{Seq => MSeq}
import scala.collection.immutable.{Seq => ISeq, Map => IMap}

/**
 * @author Raymond Dodge
 * @version 19 Jan 2012
 * @version 25 Jan 2012 - implemented aliveTokens
 */
trait ListOfTokens {
	def tokens():ISeq[ISeq[Token]]
	def aliveTokens() = tokens.map{_.filter(ListOfTokens.aliveFilter)}
}

object ListOfTokens {
	object aliveFilter extends Function1[Token,Boolean] {
		def apply(x:Token):Boolean = x.currentHitpoints > 0
	}
}

/**
 * @author Raymond Dodge
 * @version 19 Jan 2012
 */
class CannonicalListOfTokens(
		val tokens:ISeq[ISeq[CannonicalToken]]
) extends ListOfTokens

/**
 * @author Raymond Dodge
 * @version 19 Jan 2012
 * @version 25 Jan 2012 - added aliveMyTokens and aliveOtherTokens
 * @version 27 Nov 2012 - in tokens: prepending myTokens to otherTokens rather than appending myTokens to otherTokens
 */
class PlayerListOfTokens(
		val myTokens:ISeq[CannonicalToken],
		val otherTokens:ISeq[ISeq[MirrorToken]]
) extends ListOfTokens {
	def tokens():ISeq[ISeq[Token]] = myTokens +: otherTokens
	
	def aliveMyTokens = myTokens.filter(ListOfTokens.aliveFilter)
	def aliveOtherTokens = otherTokens.map{_.filter(ListOfTokens.aliveFilter)}
}

/**
 * A list of tokens that can be mutated.
 * 
 * Exists because [[com.rayrobdod.deductionTactics.UnitAwareSpaceClass]]es
 * need to know where the tokens are despite being made before the tokens are made 
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
class MutableListOfTokens extends ListOfTokens {
	var tokens = ISeq.empty[ISeq[CannonicalToken]]
}
