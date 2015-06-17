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

import org.scalatest.{FunSuite, FunSpec}
import org.scalatest.prop.PropertyChecks
import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.Statuses._
import java.nio.charset.StandardCharsets.UTF_8

class TokenClassFromBinaryTest extends FunSpec {
	import TokenClassFromBinary.HexArrayStringConverter;
	
	describe ("TokenClass + Binary Parsing (Happy)") {
		it ("Eagle") {
			val src = new java.io.DataInputStream(
				new java.io.ByteArrayInputStream((
					"Eagle".getBytes(UTF_8) ++ Seq.fill(15){0} ++
					Seq(Elements.Fire.id, Weaponkinds.Spearkind.id, Statuses.Blind.id,
							BodyTypes.Avian.id, 1, 4, Statuses.Sleep.id, Directions.Left.id) ++
					Seq(0.75f, 2f, 1f, 1.5f, 0.5f).map{java.lang.Float.floatToIntBits _}.flatMap{x => java.nio.ByteBuffer.allocate(4).putInt(x).array()} ++
					Seq.fill(CannonicalTokenClassFromBinary.imageLocLength){0}
				).map{
					case x:Byte => x
					case x:Int => x.byteValue
				})
			)
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
			val res = new CannonicalTokenClassFromBinary(src)
			
			assertResult(exp){res}
		}
		it ("Eagle (hardcoded)") {
			val src = new java.io.DataInputStream(
				new java.io.ByteArrayInputStream(hexArray"""
					4561676c65 0000000000 0000000000 0000000000
					0202020101 0400003f40 0000400000 003f800000
					3fc000003f 0000000000 0000000000 0000000000
					0000000000 0000000000 0000000000 0000000000
					0000000000 0000000000 0000000000 0000000000
					0000000000 0000000000 0000000000 0000000000
					0000000000 000000"""
				)
			)
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
			val res = new CannonicalTokenClassFromBinary(src)
			
			assertResult(exp){res}
		}
		it ("Lefty Batter") {
			val src = new java.io.DataInputStream(
				new java.io.ByteArrayInputStream((
					"Lefty Batter".getBytes(UTF_8) ++ Seq.fill(8){0} ++
					Seq(Elements.Sound.id, Weaponkinds.Bluntkind.id, Statuses.Neuro.id,
							BodyTypes.Humanoid.id, 1, 3, Statuses.Burn.id, Directions.Right.id) ++
					Seq(2f, .5f, 1.5f, 1f, 1.25f).map{java.lang.Float.floatToIntBits _}.flatMap{x => java.nio.ByteBuffer.allocate(4).putInt(x).array()} ++
					Seq.fill(CannonicalTokenClassFromBinary.imageLocLength){0}
				).map{
					case x:Byte => x
					case x:Int => x.byteValue
				})
			)
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
			val res = new CannonicalTokenClassFromBinary(src)
			
			assertResult(exp){res}
		}
	}
	describe ("TokenClass + Binary Parsing (Unhappy)") {
		
		it ("Illegal weaponkind") {
			val src = new java.io.DataInputStream(
				new java.io.ByteArrayInputStream((
					"Lefty Batter".getBytes(UTF_8) ++ Seq.fill(8){0} ++
					Seq(Elements.Sound.id, 5, Statuses.Neuro.id,
							BodyTypes.Humanoid.id, 1, 3, Statuses.Burn.id, Directions.Right.id) ++
					Seq(2f, .5f, 1.5f, 1f, 1.25f).map{java.lang.Float.floatToIntBits _}.flatMap{x => java.nio.ByteBuffer.allocate(4).putInt(x).array()} ++
					Seq.fill(CannonicalTokenClassFromBinary.imageLocLength){0}
				).map{
					case x:Byte => x
					case x:Int => x.byteValue
				})
			)
			intercept[IndexOutOfBoundsException] {
				new CannonicalTokenClassFromBinary(src)
			}
		}
	}
	
}

object TokenClassFromBinary {
	// String Interpolation
	implicit class HexArrayStringConverter(val sc: StringContext) extends AnyVal {
		def hexArray(args: Any*):Array[Byte] = {
			((sc.parts.head):String).filter{x => ('A' <= x && x <= 'F') || ('a' <= x && x <= 'f') || ('0' <= x && x <= '9')}.grouped(2).map{x => Integer.parseInt(x, 16)}.map{_.byteValue}.toArray
		}
	}
}
