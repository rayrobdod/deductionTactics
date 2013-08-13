package com.rayrobdod.deductionTactics
// events perfomed by tokens

import scala.swing.event.Event
import com.rayrobdod.boardGame.{Space => BoardGameSpace}
import Elements.Element
import Weaponkinds.Weaponkind
import Directions.Direction
import Statuses.Status

/**
 * A unit is attacking a target to deal damage. 
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
case class AttackForDamage(val target:Token, val element:Element, val kind:Weaponkind, val from:BoardGameSpace) extends Event

/**
 * A unit is attacking a target to inflict a status. 
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
case class AttackForStatus(val target:Token, val status:Status, from:BoardGameSpace) extends Event

// Move is in com.rayrobdod.boardGame

/**
 * A unit has run out of hitpoints
 * @author Raymond Dodge
 * @version 20 Mar 2012
 */
case class Died() extends Event

/**
 * ??? Maybe put in boardgame
 * A unit's stats have changed
 * @author Raymond Dodge
 */
//case class Updated() extends Event

