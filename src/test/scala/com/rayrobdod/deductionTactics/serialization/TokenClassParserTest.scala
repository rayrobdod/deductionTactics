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
package serialization

import org.scalatest.FunSpec
import scala.collection.immutable.Seq
import com.rayrobdod.json.parser.JsonParser
import com.rayrobdod.json.builder.MinifiedJsonObjectBuilder

class TokenClassParserTest extends FunSpec {
	describe ("TokenClassParser + TokenClassBuilder") {
		it ("is an identity") {
			val src = new TokenClass(
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
				weakStatus = Statuses.Sleep,
				isSpy = false,
				stanceGroup = TokenClass.SingleStanceGroup
			)
			val res = new TokenClassParser(new TokenClassBuilder).parse(src, None).build
			
			assertResult(src){res}
		}
	}
	ignore ("TokenClassParser + JsonBuilder") {
		it ("Eagle") {
			val exp = """{
				"name":"Eagle",
				"element":"Fire",
				"atkWeapon":"Spear",
				"atkStatus":"Blind",
				"body":"Avian",
				"range":1,
				"speed":4,
				"weakStatus":"Sleep",
				"weakDirection":"Left",
				"weakWeapon":{"Whip":1.5,"Blunt":2.0,"Blade":0.75,"Powder":0.5,"Spear":1.0},
				"icon":"/com/rayrobdod/deductionTactics/tokenClasses/birds/Golden Eagle.png"
			}""".replaceAll("""[\n\r\t]""","")
			val src = new TokenClass(
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
				weakStatus = Statuses.Sleep,
				isSpy = false,
				stanceGroup = TokenClass.SingleStanceGroup
			)
			val res = new TokenClassParser(new MinifiedJsonObjectBuilder).parse(src, Some("""/com/rayrobdod/deductionTactics/tokenClasses/birds/Golden Eagle.png"""))
			
			assertResult(exp){res}
		}
		it ("Lefty Batter") {
			val exp = """{
				"name":"Lefty Batter",
				"element":"Sound",
				"atkWeapon":"Blunt",
				"atkStatus":"Neuro",
				"body":"Human",
				"range":1,
				"speed":3,
				"weakStatus":"Burn",
				"weakDirection":"Right",
				"weakWeapon":{"Whip":1.0,"Blunt":0.5,"Blade":2.0,"Powder":1.25,"Spear":1.5}
			}""".replaceAll("""[\n\r\t]""","")
			val src = new TokenClass(
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
			)
			val res = new TokenClassParser(new MinifiedJsonObjectBuilder).parse(src, None)
			
			assertResult(exp){res}
		}
	}
}
