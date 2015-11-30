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
			val exp = Seq(new TokenClass(
				name = "Eagle",
				body = BodyTypes.Avian,
				atkElement = Elements.Fire,
				atkWeapon = Weaponkinds.Spearkind,
				atkStatus = Statuses.Blind,
				range = 1,
				speed = 4,
				weakDirection = Directions.Up,
				weakWeapon = Map(
					Weaponkinds.Bladekind -> .75f,
					Weaponkinds.Bluntkind -> 2f,
					Weaponkinds.Spearkind -> 1f,
					Weaponkinds.Whipkind -> 1.5f,
					Weaponkinds.Powderkind -> .5f
				),
				weakStatus = Statuses.Sleep,
				isSpy = false,
				stanceGroup = TokenClass.SingleStanceGroup
			))
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
			val exp = Seq(new TokenClass(
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
				weakStatus = Statuses.Burn,
				isSpy = false,
				stanceGroup = TokenClass.SingleStanceGroup
			))
			val res = new JsonParser(new TokenClassBuilder).parse(src).build
			
			assertResult(exp){res}
		}
		it ("Stances") {
			val myStanceGroup = new TokenClass.MultipleStanceGroup
			
			val src = """{
				"name":["Ranger (sword)", "Ranger (arrow)"],
				"element":"Electric",
				"atkWeapon":["Blade", "Spear"],
				"atkStatus":"Blind",
				"range":[1,2],
				"speed":3,
				"body":"Human",
				"weakWeapon":{"blade":1,"blunt":0.75,"spear":2,"whip":1.5,"powder":0.5},
				"weakStatus":"Neuro",
				"weakDirection":"Down"
			}"""
			val exp = Seq(new TokenClass(
				name = "Ranger (sword)",
				body = BodyTypes.Humanoid,
				atkElement = Elements.Electric,
				atkWeapon = Weaponkinds.Bladekind,
				atkStatus = Statuses.Blind,
				range = 1,
				speed = 3,
				weakDirection = Directions.Down,
				weakWeapon = Map(
					Weaponkinds.Bladekind -> 1f,
					Weaponkinds.Bluntkind -> .75f,
					Weaponkinds.Spearkind -> 2f,
					Weaponkinds.Whipkind -> 1.5f,
					Weaponkinds.Powderkind -> 0.5f
				),
				weakStatus = Statuses.Neuro,
				isSpy = false,
				stanceGroup = myStanceGroup
			), new TokenClass(
				name = "Ranger (arrow)",
				body = BodyTypes.Humanoid,
				atkElement = Elements.Electric,
				atkWeapon = Weaponkinds.Spearkind,
				atkStatus = Statuses.Blind,
				range = 2,
				speed = 3,
				weakDirection = Directions.Down,
				weakWeapon = Map(
					Weaponkinds.Bladekind -> 1f,
					Weaponkinds.Bluntkind -> .75f,
					Weaponkinds.Spearkind -> 2f,
					Weaponkinds.Whipkind -> 1.5f,
					Weaponkinds.Powderkind -> 0.5f
				),
				weakStatus = Statuses.Neuro,
				isSpy = false,
				stanceGroup = myStanceGroup
			))
			val res = new JsonParser(new TokenClassBuilder).parse(src).build
			
			assert(res(0).stanceGroup == res(1).stanceGroup)
			assertResult(exp){res.map{_.copy(stanceGroup = myStanceGroup)}}
		}
		it ("Stances (weakweapon)") {
			val myStanceGroup = new TokenClass.MultipleStanceGroup
			
			val src = """{
				"name":"Ranger (arrow)",
				"element":"Electric",
				"atkWeapon":"Spear",
				"atkStatus":"Blind",
				"range":2,
				"speed":3,
				"body":"Human",
				"weakWeapon":{"blade":1,"blunt":0.75,"spear":[0.5,2],"whip":1.5,"powder":[2,0.5]},
				"weakStatus":"Neuro",
				"weakDirection":"Down"
			}"""
			val exp = Seq(new TokenClass(
				name = "Ranger (arrow)",
				body = BodyTypes.Humanoid,
				atkElement = Elements.Electric,
				atkWeapon = Weaponkinds.Spearkind,
				atkStatus = Statuses.Blind,
				range = 2,
				speed = 3,
				weakDirection = Directions.Down,
				weakWeapon = Map(
					Weaponkinds.Bladekind -> 1f,
					Weaponkinds.Bluntkind -> .75f,
					Weaponkinds.Spearkind -> 0.5f,
					Weaponkinds.Whipkind -> 1.5f,
					Weaponkinds.Powderkind -> 2f
				),
				weakStatus = Statuses.Neuro,
				isSpy = false,
				stanceGroup = myStanceGroup
			), new TokenClass(
				name = "Ranger (arrow)",
				body = BodyTypes.Humanoid,
				atkElement = Elements.Electric,
				atkWeapon = Weaponkinds.Spearkind,
				atkStatus = Statuses.Blind,
				range = 2,
				speed = 3,
				weakDirection = Directions.Down,
				weakWeapon = Map(
					Weaponkinds.Bladekind -> 1f,
					Weaponkinds.Bluntkind -> .75f,
					Weaponkinds.Spearkind -> 2f,
					Weaponkinds.Whipkind -> 1.5f,
					Weaponkinds.Powderkind -> 0.5f
				),
				weakStatus = Statuses.Neuro,
				isSpy = false,
				stanceGroup = myStanceGroup
			))
			val res = new JsonParser(new TokenClassBuilder).parse(src).build
			
			assert(res(0).stanceGroup == res(1).stanceGroup)
			assertResult(exp){res.map{_.copy(stanceGroup = myStanceGroup)}}
		}
	}
}


