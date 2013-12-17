/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

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
import BodyTypes.{Value => BodyType}
import Directions.Direction

import java.net.URL
import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.decoders.ToScalaCollectionJSONDecoder
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.collection.JavaConversions.mapAsJavaMap

/**
 * A supposed-to-be-immutable trait defining a token's class
 * @author Raymond Dodge
 * @version a.5.0
 */
trait CannonicalTokenClass extends TokenClass
{
	def name:String
	
	def body:Some[BodyType]
	def atkElement:Some[Element]
	def atkWeapon:Some[Weaponkind]
	def atkStatus:Some[Status]
	def range:Some[Int]
	def speed:Some[Int]
	
	def weakDirection:Some[Direction]
	def weakWeapon:Map[Weaponkind,Some[Float]]
	def weakStatus:Some[Status]
	
	override def toString = "CannonicalTokenClass{name:" + name + "; ...}"
	
}

/**
 * A CannonicalTokenClass that has all of its values defined
 * directly in the constructor
 * @author Raymond Dodge
 * @version a.5.0
 */
final class CannonicalTokenClassBlunt(
	override val name:String,
	
	override val body:Some[BodyType],
	override val atkElement:Some[Element],
	override val atkWeapon:Some[Weaponkind],
	override val atkStatus:Some[Status],
	override val range:Some[Int],
	override val speed:Some[Int],
	
	override val weakDirection:Some[Direction],
	override val weakWeapon:Map[Weaponkind,Some[Float]],
	override val weakStatus:Some[Status]
) extends CannonicalTokenClass

/**
 * Loads tokens as a service.
 * 
 * @author Raymond Dodge
 * @version a.5.3
 */
object CannonicalTokenClass
{
	val SERVICE = "com.rayrobdod.deductionTactics.TokenClass"
	
	val allKnown:ISeq[CannonicalTokenClass] =
	{
		import scala.collection.JavaConversions.iterableAsScalaIterable
		import com.rayrobdod.util.services.ResourcesServiceLoader
		import java.nio.charset.StandardCharsets.UTF_8
		
		val a:Seq[URL] = new ResourcesServiceLoader(SERVICE).toSeq
		
		// Binary version
		val b:Seq[Seq[CannonicalTokenClass]] = a.map{(jsonPath:URL) =>
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
				val jsonReader = new java.io.InputStreamReader(jsonPath.openStream(), UTF_8)
				
				val l = new ToScalaCollection(CannonicalTokenClassDecoder)
				JSONParser.parse(l, jsonReader)
				jsonReader.close()
				l.resultSeq
			}
		}
		val e = b.flatten
		
		
		ISeq.empty ++ e
	}
}
