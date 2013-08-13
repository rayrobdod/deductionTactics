package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction
import scala.collection.immutable.Map

/**
 * A class that mirrors [[com.rayrobdod.deductionTactics.CannonicalTokenClass]] but with all methods being mutable and the
 * slight possiblity of any of the items being [[scala.None]]
 * 
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 19 Jan 2012 - renamed from TokenSuspicions to SuspicionsTokenClass
 * @version 20 Jan 2012 - has an actual icon now, although it doesn't change with the options 
 * @version 14 Feb 2012 - icon has minor change with gueses properties
 * @version 28 Feb 2012 - made the icon function use package#generateGenericIcon
 * @version 05 Jun 2012 - changing weakWeapon from Option[Map[Weaponkind, Float]]
			to Map[Weaponkind, Option[Float]]
 */
class SuspicionsTokenClass extends TokenClass
{
	var body:Option[BodyType] = None
	
	var atkElement:Option[Element] = None
	var atkWeapon:Option[Weaponkind] = None
	var atkStatus:Option[Status] = None
	var range:Option[Int] = None
	var speed:Option[Int] = None
	
	var weakWeapon:Map[Weaponkind,Option[Float]] = Weaponkinds.values.map{((_, None))}.toMap
	var weakStatus:Option[Status] = None
	var weakDirection:Option[Direction] = None
	
	val name = "???"
	def icon = generateGenericIcon(this)
}

object SuspicionsTokenClass {
	// seemingly unused
	val unknownIcon = loadIcon(this.getClass().getResource("/sprites/Gray shirt.png"))
}
