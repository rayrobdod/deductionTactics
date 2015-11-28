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
package serialization

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.BodyType
import Directions.Direction

import java.nio.file.{Path, Files}
import scala.collection.immutable.{Map, Seq}

/**
 * 
 * 
 * @author Raymond Dodge
 * @since a.6.0
 */
object GenerateBasicTokens
{
	private case class ElementAttributes(unitName:String, atkStatus:Status)
	private case class WeaponkindAttributes(unitName:String, weakStatus:Status, weakWeapon:Map[Weaponkind, Double])
	private object TokenClassOrdering extends Ordering[TokenClass] {
		def compare(a:TokenClass, b:TokenClass) = {
			if (a.atkElement != b.atkElement) {
				a.atkElement.id compareTo b.atkElement.id
			} else if (a.atkWeapon != b.atkWeapon) {
				a.atkWeapon.id compareTo b.atkWeapon.id
			} else {
				0
			}
		}
	}
	
	private val elements = Map(
		Elements.Light    -> ElementAttributes(unitName = "Shining", atkStatus = Statuses.Blind),
		Elements.Electric -> ElementAttributes(unitName = "Static",  atkStatus = Statuses.Neuro),
		Elements.Fire     -> ElementAttributes(unitName = "Flaming", atkStatus = Statuses.Burn),
		Elements.Frost    -> ElementAttributes(unitName = "Frosty",  atkStatus = Statuses.Sleep),
		Elements.Sound    -> ElementAttributes(unitName = "Sonic",   atkStatus = Statuses.Confuse)
	)
	private val weapons = Map(
		Weaponkinds.Bladekind  -> WeaponkindAttributes(unitName = "Swordsman", weakStatus = Statuses.Sleep,   weakWeapon = Map(Weaponkinds.Bladekind -> .5 ,Weaponkinds.Bluntkind -> .75,Weaponkinds.Spearkind -> 1.5,Weaponkinds.Whipkind -> 2  ,Weaponkinds.Powderkind -> 1  )),
		Weaponkinds.Bluntkind  -> WeaponkindAttributes(unitName = "Clubsman",  weakStatus = Statuses.Burn,    weakWeapon = Map(Weaponkinds.Bladekind -> 1.5,Weaponkinds.Bluntkind -> .5 ,Weaponkinds.Spearkind -> 2  ,Weaponkinds.Whipkind -> 1  ,Weaponkinds.Powderkind -> .75)),
		Weaponkinds.Spearkind  -> WeaponkindAttributes(unitName = "Pikeman",   weakStatus = Statuses.Blind,   weakWeapon = Map(Weaponkinds.Bladekind -> 2  ,Weaponkinds.Bluntkind -> 1  ,Weaponkinds.Spearkind -> .5 ,Weaponkinds.Whipkind -> .75,Weaponkinds.Powderkind -> 1.5)),
		Weaponkinds.Whipkind   -> WeaponkindAttributes(unitName = "Whipman",   weakStatus = Statuses.Confuse, weakWeapon = Map(Weaponkinds.Bladekind -> 1  ,Weaponkinds.Bluntkind -> 1.5,Weaponkinds.Spearkind -> .75,Weaponkinds.Whipkind -> .5 ,Weaponkinds.Powderkind -> 2  )),
		Weaponkinds.Powderkind -> WeaponkindAttributes(unitName = "Powderman", weakStatus = Statuses.Neuro,   weakWeapon = Map(Weaponkinds.Bladekind -> .75,Weaponkinds.Bluntkind -> 2  ,Weaponkinds.Spearkind -> 1  ,Weaponkinds.Whipkind -> 1.5,Weaponkinds.Powderkind -> .5 ))
	)
	private val DontCare = new Direction(-1, "DontCare", {(a) => Some(a)})
	
	val classes = {
		elements.map({( a:Element, b:ElementAttributes ) =>
		weapons.map({( c:Weaponkind, d:WeaponkindAttributes) =>
			
			new TokenClassBlunt(
				name = b.unitName + " " + d.unitName,
				atkElement = a,
				atkWeapon = c,
				atkStatus = b.atkStatus,
				weakWeapon = d.weakWeapon.mapValues{(x) => x.toFloat},
				weakStatus = d.weakStatus,
			
				body = BodyTypes.Humanoid,
				range = 1,
				speed = 3,
				weakDirection = DontCare
			)
		}.tupled)}.tupled).flatten.toSeq.sorted(TokenClassOrdering)
	}
	def nameToIcon(base:Path)(className:String):Option[String] = {
		val pack = """com/rayrobdod/deductionTactics/tokenClasses/basic/"""
		val retVal = pack + className + ".png"
		
		val location:Option[Path] = Some(base resolve retVal)
		location.filter{ Files.exists(_) }.map{(x) => retVal}
	}
}
