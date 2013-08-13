package com.rayrobdod.boardGame

import scala.swing.Reactions
import scala.swing.Reactions.Reaction
import scala.swing.event.Event

/**
 * 
 * @version ?? ??? ????
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 */
trait TemporaryReaction extends Reaction
{
	var _isDone = false
	def isDone = _isDone
	
	def takingOverFor:Reaction
	def reactions:Reactions
	
	// TODO come up with a better name
	final def missionComplete() =
	{
		reactions -= this
//		takingOverFor.foreach{reactions += _}
		reactions += takingOverFor
		_isDone = true
	}
}
