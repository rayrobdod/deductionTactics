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

import org.scalatest.FunSpec
import scala.collection.immutable.Seq
import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.deductionTactics.Statuses._

class TokenClassFromJsonTest extends FunSpec {
	describe ("TokenClass + Json Parsing") {
		it ("Eagle") {
			val src = """{
				"name":"Eagle",
				"element":"Fire",
				"atkWeapon":"Spear",
				"atkStatus":"Blind",
				"range":1,
				"speed":4,
				"body":"Avian",
				"weakWeapon":{"blade":0.75,"blunt":2,"spear":1,"whip":1.5,"powder":0.5},
				"weakStatus":"Sleep",
				"weakDirection":"DontCare",
				"icon":"/com/rayrobdod/deductionTactics/tokenClasses/birds/Golden Eagle.png"
			}"""
			val exp = new TokenClassBlunt(
				name = "Eagle",
				body = BodyTypes.Avian,
				atkElement = Elements.Fire,
				atkWeapon = Weaponkinds.Spearkind,
				atkStatus = Statuses.Blind,
				range = 1,
				speed = 4,
				weakDirection = Directions.Left,
				weakWeapon = Map(
					Weaponkinds.Bladekind -> .75f,
					Weaponkinds.Bluntkind -> 2f,
					Weaponkinds.Spearkind -> 1f,
					Weaponkinds.Whipkind -> 1.5f,
					Weaponkinds.Powderkind -> .5f
				),
				weakStatus = Statuses.Sleep
			)
			val res = new JsonParser(new TokenClassBuilder).parse(src).build
			
			assertResult(exp){res}
		}
		it ("Lefty Batter") {
			val src = """{
				"comment":"not sure how important direction it - it seems like the first thing someone would find out, after attacking elements",
				"name":"Lefty Batter",
				"element":"Sound",
				"atkWeapon":"Blunt",
				"atkStatus":"Neuro",
				"range":1,
				"speed":3,
				"body":"Human",
				"weakWeapon":{"blade":2,"blunt":0.5,"spear":1.5,"whip":1,"powder":1.25},
				"weakStatus":"Burn",
				"weakDirection":"Right",
				"iconc":"" 
			}"""
			val exp = new TokenClassBlunt(
				name = "Lefty Batter",
				body = BodyTypes.Humanoid,
				atkElement = Elements.Sound,
				atkWeapon = Weaponkinds.Bluntkind,
				atkStatus = Statuses.Neuro,
				range = 1,
				speed = 3,
				weakDirection = Directions.Right,
				weakWeapon = Map(
					Weaponkinds.Bladekind -> 2f,
					Weaponkinds.Bluntkind -> .5f,
					Weaponkinds.Spearkind -> 1.5f,
					Weaponkinds.Whipkind -> 1f,
					Weaponkinds.Powderkind -> 1.25f
				),
				weakStatus = Statuses.Burn
			)
			val res = new JsonParser(new TokenClassBuilder).parse(src).build
			
			assertResult(exp){res}
		}
	}
}


