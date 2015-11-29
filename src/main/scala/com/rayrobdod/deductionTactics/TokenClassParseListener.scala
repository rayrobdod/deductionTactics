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

import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.json.builder.{Builder, MapBuilder}
import java.io.{StringReader, InputStreamReader}



/**
 * A builder for TokenClasses.
 * @since a.4.1
 * @version a.6.1
 */
final case class TokenClassTemplate(
	val nameOpt:Option[String] = None,
	val body:Option[BodyType] = None,
	val atkElement:Option[Element] = None,
	val atkWeapon:Option[Weaponkind] = None,
	val atkStatus:Option[Status] = None,
	val range:Option[Int] = None,
	val speed:Option[Int] = None,
	val weakDirection:Option[Direction] = None,
	val weakWeapon:Map[Weaponkind,Option[Float]] = Weaponkinds.values.zipAll(Nil, null, None).toMap,
	val weakStatus:Option[Status] = None
) {
	def name:String = nameOpt.getOrElse("???")
	
	private def arbitraryDirection:Direction = {
		val nameHash = this.name.hashCode
		Directions.values((nameHash % Directions.values.length + Directions.values.length) % Directions.values.length)
	}
	
	/**
	 * @throws IllegalStateException if required fields were not set
	 */
	def build():TokenClass = {
		try {
			// B*CKING DELAYED EXECUTION
			weakWeapon.values.foreach{_.get}
			
			new TokenClass (
				TokenClassTemplate.this.name,
				TokenClassTemplate.this.body.get,
				TokenClassTemplate.this.atkElement.get,
				TokenClassTemplate.this.atkWeapon.get,
				TokenClassTemplate.this.atkStatus.get,
				TokenClassTemplate.this.range.get,
				TokenClassTemplate.this.speed.get,
				TokenClassTemplate.this.weakDirection.getOrElse(arbitraryDirection),
				TokenClassTemplate.this.weakWeapon.map{(a) => ((a._1, a._2.get))},
				TokenClassTemplate.this.weakStatus.get
			)
		} catch {
			// TODO: be more specific?
			case e:java.util.NoSuchElementException => throw new IllegalStateException(
					"Not all information gotten about subject: " + this.toString, e)
					
		}
	}
}



final class TokenClassBuilder extends Builder[TokenClassTemplate] {
	override val init:TokenClassTemplate = new TokenClassTemplate()
	override def apply(folding:TokenClassTemplate, key:String, value:Any):TokenClassTemplate = key match {
		case "name" => folding.copy(nameOpt = Some(value.toString))
		case "body" => folding.copy(body = Some(BodyTypes.withName(value.toString)))
		case "element" => folding.copy(atkElement = Some(Elements.withName(value.toString)))
		case "atkWeapon" => folding.copy(atkWeapon = Some(Weaponkinds.withName(value.toString)))
		case "atkStatus" => folding.copy(atkStatus = Some(Statuses.withName(value.toString)))
		case "range" => folding.copy(range = Some(value.toString.toInt))
		case "speed" => folding.copy(speed = Some(value.toString.toInt))
		case "weakStatus" => folding.copy(weakStatus = Some(Statuses.withName(value.toString)))
		case "weakDirection" => {
			if (value != "DontCare") {
				folding.copy(weakDirection = Some(Directions.withName(value.toString)))
			} else {
				folding.copy(weakDirection = None)
			}
		}
		case "weakWeapon" => folding.copy(weakWeapon = value.asInstanceOf[Map[_,_]].map{x:(Any,Any) => ((Weaponkinds.withName(x._1.toString), Option(x._2.toString.toFloat)))})
		case _ => folding
	}
	override def childBuilder(key:String):Builder[_] = new MapBuilder
	override val resultType:Class[TokenClassTemplate] = classOf[TokenClassTemplate]
}