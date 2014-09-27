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
import BodyTypes.BodyType
import Directions.Direction
import com.rayrobdod.deductionTactics.{TokenClass => CannonicalTokenClass}

import scala.collection.Seq
import scala.collection.immutable.{Map, Seq => ISeq}
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsJavaMap

/**
 * A Token Class that gets its values from a Map.
 * @author Raymond Dodge
 * @version a.5.0
 */
final class CannonicalTokenClassFromMap(map:Map[String,Any]) extends CannonicalTokenClass
{
	override def name = map("name").toString
	
	override def body = BodyTypes.withName(map("body").toString)
	
	override def atkElement = Elements.withName(map("element").toString)
	override def atkWeapon = Weaponkinds.withName(map("atkWeapon").toString)
	override def atkStatus = Statuses.withName(map("atkStatus").toString)
	override def range = asInt(map("range"))
	override def speed = asInt(map("speed"))
	override def weakDirection = asDirection(map("weakDirection"))
	override def weakWeapon = asWeakWeaponMap(map("weakWeapon"))
	override def weakStatus = Statuses.withName(map("weakStatus").toString)
	
	
	
	
	private def asInt(any:Any):Int = {any match {
		case x:Int => x
		case x:Integer => x
		case x:String => Integer.parseInt(x)
		case x:Any =>  Integer.parseInt(x.toString)
	}}
	
	private def asFloat(any:Any):Float = {any match {
		case x:Int => x.floatValue
		case x:Long => x.floatValue
		case x:Float => x
		case x:Double => x.floatValue
		case x:String => java.lang.Float.parseFloat(x)
		case x:Any => java.lang.Float.parseFloat(x.toString)
	}}
	
	private def asWeakWeaponMap(any:Any):Map[Weaponkind, Float] = {any match {
		case x:scala.collection.Map[_,_] => Map.empty ++ x
		case x:java.util.Map[_,_] => Map.empty ++ mapAsScalaMap(x)
//		case _ => Map.empty
	}}.map{(kindValue:Pair[_,_]) => ((Weaponkinds.withName(kindValue._1.toString), asFloat(kindValue._2)))}
	
	private def arbitraryDirection:Direction = {
		val nameHash = this.name.hashCode
		Directions.values((nameHash % Directions.values.length + Directions.values.length) % Directions.values.length)
	}
	
	private def asDirection(any:Any):Direction = {any match {
		case x:Direction => x
		case "DontCare" => arbitraryDirection
		case x:String => Directions.withName(x)
		case x:Any => asDirection(x.toString)
	}}
}
