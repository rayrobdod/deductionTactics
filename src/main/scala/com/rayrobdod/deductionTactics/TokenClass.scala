/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.BodyType
import Directions.Direction

import scala.collection.immutable.{Seq, Map}

/**
 * A description of the attributes of a unit.
 * 
 * @author Raymond Dodge
 * @version a.6.0
 */
trait TokenClass
{
	/** A class's name */
	def name:String
	
	/** A class's bodytype. */
	def body:BodyType
	
	/** The element a unit attacks with. Also determines it's defenses against elements. */
	def atkElement:Element
	/** The weapon a unit attacks with. */
	def atkWeapon:Weaponkind
	/** The status a unit attacks with. */
	def atkStatus:Status
	/** How far away from itself a unit can attack. */
	def range:Int
	/** How far a unit can move in one turn. */
	def speed:Int
	
	/** When a unit is attacked from this direction, the attack is strongest */
	def weakDirection:Direction
	/** The weaknesses when a unit is attacked form a type of weapon */
	def weakWeapon:Map[Weaponkind,Float]
	/** When a unit is attacked while suffering this status, the attack is strongest */
	def weakStatus:Status
	
	
	/////////////////////////// Object Overrides //////////////////////////////
	
	override def toString:String = {
		this.getClass.getName + " " +
			"{ name: " + name +
			"; body: " + body +
			"; atkElement: " + atkElement +
			"; atkWeapon: " + atkWeapon +
			"; atkStatus: " + atkStatus +
			"; range: " + range +
			"; speed: " + speed +
			"; weakDirection: " + weakDirection +
			"; weakWeapon: " + weakWeapon +
			"; weakStatus: " + weakStatus +
			" }";
	}
	
	override def hashCode:Int = {
		(body.id << 28) + (atkElement.id) << 24 + (atkWeapon.id << 20) + (atkStatus.id << 16) +
		(range << 12) + (speed << 8) + (weakDirection.id << 4) + (weakStatus.id << 0)
	}
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[TokenClass]}
	
	override def equals(other:Any):Boolean = {
		if (! (this.canEquals(other))) { return false; }
		val other2 = other.asInstanceOf[TokenClass]
		if (! (other2.canEquals(this))) { return false; }
		
		other2.name == this.name &&
				other2.body == this.body &&
				other2.atkElement == this.atkElement &&
				other2.atkWeapon == this.atkWeapon &&
				other2.atkStatus == this.atkStatus &&
				other2.range == this.range &&
				other2.speed == this.speed &&
				other2.weakDirection == this.weakDirection &&
				other2.weakWeapon == this.weakWeapon &&
				other2.weakStatus == this.weakStatus
	}
}




/**
 * A TokenClass that has all of its values defined
 * directly in the constructor
 * @author Raymond Dodge
 * @version a.6.0
 */
final class TokenClassBlunt(
	override val name:String,
	
	override val body:BodyType,
	override val atkElement:Element,
	override val atkWeapon:Weaponkind,
	override val atkStatus:Status,
	override val range:Int,
	override val speed:Int,
	
	override val weakDirection:Direction,
	override val weakWeapon:Map[Weaponkind,Float],
	override val weakStatus:Status
) extends TokenClass





/**
 * Loads token classes as a service.
 * 
 * @author Raymond Dodge
 * @version a.6.0
 */
object TokenClass
{
	val SERVICE = "com.rayrobdod.deductionTactics.TokenClass"
	
	val allKnown:Seq[TokenClass] =
	{
		import scala.collection.JavaConversions.iterableAsScalaIterable
		import com.rayrobdod.json.parser.JsonParser
		import com.rayrobdod.json.builder.SeqBuilder
		import com.rayrobdod.util.services.ResourcesServiceLoader
		import java.nio.charset.StandardCharsets.UTF_8
		import java.net.URL
		
		val a:Seq[URL] = Seq.empty ++ new ResourcesServiceLoader(SERVICE)
		
		// Binary version
		val b:Seq[Seq[TokenClass]] = a.map{(jsonPath:URL) =>
			if (jsonPath.toString.endsWith(".rrd-dt-tokenClass")) {
			
				val is = jsonPath.openStream()
				val dis = new java.io.DataInputStream(is)
				
				val count = dis.readShort()
				
				val retVal = (1 to count).map{(a) =>
					new CannonicalTokenClassFromBinary(dis)
				}
				dis.close();
				retVal
			} else { // assume JSON
				var jsonReader:java.io.Reader = new java.io.StringReader("[]")
				try {
					val jsonReader = new java.io.InputStreamReader(jsonPath.openStream(), UTF_8)
					
					new JsonParser(new SeqBuilder(new TokenClassBuilder)).parse(jsonReader).map{_.asInstanceOf[CannonicalTokenClassTemplate].build}
				} finally {
					jsonReader.close()
				}
			}
		}
		val e = b.flatten
		
		
		Seq.empty ++ e
	}
}
