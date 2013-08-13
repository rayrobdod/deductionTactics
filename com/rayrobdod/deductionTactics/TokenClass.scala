package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction
import javax.swing.Icon
import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import java.io.{StringReader, InputStreamReader}
import scala.collection.JavaConversions.mapAsScalaMap

/**
 * A description of the attributes of a unit.
 * 
 * A [[com.rayrobdod.deductionTactics.CannonicalTokenClass]] is immutable and represents what the game
 * knows a unit to be like, while a [[com.rayrobdod.deductionTactics.SuspicionsTokenClass]] is a
 * player's guess at what a unit is like.
 * 
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 05 Jun 2012 - changing weakWeapon from Option[Map[Weaponkind, Float]]
			to Map[Weaponkind, Option[Float]]
 */
trait TokenClass
{
	/** A class's name */
	def name:String
	/** An icon representing this class */
	def icon:Icon
	
	/** A class's bodytype. Currently nonfunctional */
	def body:Option[BodyType]
	
	/** The element a unit attacks with. Also determines it's defenses against elements. */
	def atkElement:Option[Element]
	/** The weapon a unit attacks with. */
	def atkWeapon:Option[Weaponkind]
	/** The status a unit attacks with. */
	def atkStatus:Option[Status]
	/** How far away from itself a unit can attack. */
	def range:Option[Int]
	/** How far a unit can move in one turn. */
	def speed:Option[Int]
	
	/** When a unit is attacked from this direction, the attack is strongest */
	def weakDirection:Option[Direction]
	/** The weaknesses when a unit is attacked form a type of weapon */
	def weakWeapon:Map[Weaponkind,Option[Float]]
	/** When a unit is attacked while suffering this status, the attack is strongest */
	def weakStatus:Option[Status]
}
