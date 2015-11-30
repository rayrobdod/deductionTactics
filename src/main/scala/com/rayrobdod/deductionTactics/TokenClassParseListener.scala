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

import scala.collection.immutable.{Map, Seq}
import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.json.builder.{Builder, SeqBuilder, MapBuilder}
import java.io.{StringReader, InputStreamReader}



/**
 * A builder for TokenClasses.
 * @since a.4.1
 * @version a.6.1
 */
final case class TokenClassTemplate(
	val nameOpt:Seq[String] = Nil,
	val body:Seq[BodyType] = Nil,
	val atkElement:Seq[Element] = Nil,
	val atkWeapon:Seq[Weaponkind] = Nil,
	val atkStatus:Seq[Status] = Nil,
	val isSpy:Seq[Boolean] = Nil,
	val range:Seq[Int] = Nil,
	val speed:Seq[Int] = Nil,
	val weakDirection:Seq[Direction] = Nil,
	val weakWeapon:Map[Weaponkind,Seq[Float]] = Weaponkinds.values.zipAll(Nil, null, Nil).toMap,
	val weakStatus:Seq[Status] = Nil
) {
	def stances:Int = this.productIterator.flatMap{
		_ match {
			case x:Map[_, Seq[_]] => x.values
			case x:Seq[_] => Seq(x)
		}
	}.map{_.size}.max
	
	private def arbitraryDirection:Direction = {
		val nameHash = this.nameOpt.toList.hashCode
		Directions.values((nameHash % Directions.values.length + Directions.values.length) % Directions.values.length)
	}
	
	/**
	 * @throws IllegalStateException if required fields were not set
	 */
	def build():Seq[TokenClass] = {
		try {
			// B*CKING DELAYED EXECUTION
			weakWeapon.values.foreach{_.foreach{_.doubleValue}}
			
			val stanceGroup = if (stances == 1) {TokenClass.SingleStanceGroup} else {new TokenClass.MultipleStanceGroup}
			
			implicit class GetOrLastSeq[A](x:Seq[A]) {
				def getOrLastOption(i:Int):Option[A] = {x.lift.apply(i).orElse(x.lastOption)}
			}
			
			(0 until stances).map{i:Int =>
				new TokenClass(
					this.nameOpt.getOrLastOption(i).get,
					this.body.getOrLastOption(i).get,
					this.atkElement.getOrLastOption(i).get,
					this.atkWeapon.getOrLastOption(i).get,
					this.atkStatus.getOrLastOption(i).get,
					this.isSpy.getOrLastOption(i).getOrElse(false),
					this.range.getOrLastOption(i).get,
					this.speed.getOrLastOption(i).get,
					this.weakDirection.getOrLastOption(i).getOrElse(arbitraryDirection),
					this.weakWeapon.map{(a) => ((a._1, a._2.getOrLastOption(i).get))},
					this.weakStatus.getOrLastOption(i).get,
					stanceGroup
				)
			}
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
		case "name" => folding.copy(nameOpt = jsonValueAsStringSeq(value))
		case "body" => folding.copy(body = jsonValueAsStringSeq(value).map{BodyTypes.withName _})
		case "element" => folding.copy(atkElement = jsonValueAsStringSeq(value).map{Elements.withName _})
		case "atkWeapon" => folding.copy(atkWeapon = jsonValueAsStringSeq(value).map{Weaponkinds.withName _})
		case "atkStatus" => folding.copy(atkStatus = jsonValueAsStringSeq(value).map{Statuses.withName _})
		case "range" => folding.copy(range = jsonValueAsIntSeq(value))
		case "speed" => folding.copy(speed = jsonValueAsIntSeq(value))
		case "weakStatus" => folding.copy(weakStatus = jsonValueAsStringSeq(value).map{Statuses.withName _})
		case "weakDirection" => {
			if (value != "DontCare") {
				folding.copy(weakDirection = jsonValueAsStringSeq(value).map{Directions.withName _})
			} else {
				folding.copy(weakDirection = Nil)
			}
		}
		case "weakWeapon" => folding.copy(weakWeapon = value.asInstanceOf[Map[_,_]].map{x:(Any,Any) => ((Weaponkinds.withName(x._1.toString), jsonValueAsFloatSeq(x._2)))})
		case "isSpy" => folding.copy(isSpy = jsonValueAsBooleanSeq(value))
		case _ => folding
	}
	override def childBuilder(key:String):Builder[_] = key match {
		case "weakWeapon" => new MapBuilder(Function.const(new SeqBuilder))
		case _ => new SeqBuilder
	}
	override val resultType:Class[TokenClassTemplate] = classOf[TokenClassTemplate]
	
	def jsonValueAsStringSeq(x:Any):Seq[String] = x match {
		case y:String => Seq(y)
		case y:Seq[_] => y.map{_.toString}
	}
	def jsonValueAsIntSeq(x:Any):Seq[Int] = x match {
		case y:Int => Seq(y)
		case y:Long => Seq(y.intValue)
		case y:Seq[_] => y.map{jsonValueAsIntSeq}.flatten
	}
	def jsonValueAsFloatSeq(x:Any):Seq[Float] = x match {
		case y:Int => Seq(y.floatValue)
		case y:Long => Seq(y.floatValue)
		case y:Float => Seq(y)
		case y:Double => Seq(y.floatValue)
		case y:Seq[_] => y.map{jsonValueAsFloatSeq}.flatten
	}
	def jsonValueAsBooleanSeq(x:Any):Seq[Boolean] = x match {
		case y:Boolean => Seq(y)
		case y:Seq[_] => y.map{jsonValueAsBooleanSeq}.flatten
	}
}
