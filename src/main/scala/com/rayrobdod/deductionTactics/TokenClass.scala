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
 * @version a.6.1
 * 
 * @constructor
 * @param name A class's name
 * @param body A class's bodytype.
 * @param atkElement The element a unit attacks with. Also determines it's defenses against elements.
 * @param atkWeapon The weapon a unit attacks with.
 * @param atkStatus The status a unit attacks with.
 * @param range How far away from itself a unit can attack.
 * @param speed How far a unit can move in one turn.
 * @param weakDirection When a unit is attacked from this direction, the attack is strongest
 * @param weakWeapon The weaknesses when a unit is attacked form a type of weapon
 * @param weakStatus When a unit is attacked while suffering this status, the attack is strongest
 */
final case class TokenClass(
	val name:String,
	val body:BodyType,
	val atkElement:Element,
	val atkWeapon:Weaponkind,
	val atkStatus:Status,
	val range:Int,
	val speed:Int,
	val weakDirection:Direction,
	val weakWeapon:Map[Weaponkind,Float],
	val weakStatus:Status
)



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
					CannonicalTokenClassFromBinary(dis)
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
