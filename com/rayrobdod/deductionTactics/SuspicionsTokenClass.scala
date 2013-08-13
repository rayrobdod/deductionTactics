package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction
import scala.collection.immutable.Map
import javax.swing.Icon

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
 * @version 12 Jun 2012 - changed name from val to var, and implementing a icon_= function
 * @version 28 Jus 2012 - implementing hashCode and equals
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
	
	var name = "???"
	private var setIcon:Option[Icon] = None
	def icon_=(icon:Option[Icon]) {setIcon = icon}
	def icon = setIcon.getOrElse(generateGenericIcon(this))
	
	override def hashCode:Int = {
		body.hashCode +
		atkElement.hashCode +
		atkWeapon.hashCode +
		atkStatus.hashCode +
		range.hashCode +
		speed.hashCode +
		weakWeapon.hashCode +
		weakStatus.hashCode +
		weakDirection.hashCode +
		name.hashCode
	}
	def canEquals(other:Any) = {other.isInstanceOf[SuspicionsTokenClass]}
	override def equals(other:Any) = {
		if (this.canEquals(other) && other.asInstanceOf[SuspicionsTokenClass].canEquals(this))
		{
			val other2 = other.asInstanceOf[SuspicionsTokenClass]
			
			this.body == other2.body &&
			this.atkElement == other2.atkElement &&
			this.atkWeapon == other2.atkWeapon &&
			this.atkStatus == other2.atkStatus &&
			this.range == other2.range &&
			this.speed == other2.speed &&
			this.weakWeapon == other2.weakWeapon &&
			this.weakStatus == other2.weakStatus &&
			this.weakDirection == other2.weakDirection &&
			this.name == other2.name
		}
		else false
	}
}

object SuspicionsTokenClass {
	// seemingly unused
	val unknownIcon = loadIcon(this.getClass().getResource("/sprites/Gray shirt.png"))
}
