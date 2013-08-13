package com.rayrobdod.deductionTactics
// events performed by Players

import scala.swing.event.Event
import com.rayrobdod.boardGame.{Space => BoardGameSpace}
import Elements.Element
import Weaponkinds.Weaponkind
import Directions.Direction
import Statuses.Status

/**
 * A player is requesting that a unit attack to deal damage
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
case class RequestAttackForDamage(val attacker:CannonicalToken, val target:Token) extends Event

/**
 * A player is requesting that a unit attack to deal damage
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
case class RequestAttackForStatus(val attacker:CannonicalToken, val target:Token) extends Event

/**
 * A player is requesting that a unit move
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
case class RequestMove(val attacker:CannonicalToken, val to:BoardGameSpace) extends Event

// StartOfTurn and EndOfTurn are in com.rayrobdod.boardGame

/**
 * Sent to a player at the end of a game to indicate that it has won
 * @author Raymond Dodge
 * @version 4 Aug 2012
 */
case object Victory extends Event
