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

/**
 * Constants relating to TokenClassFromBinary
 * @author Raymond Dodge
 * @since a.4.1
 * @version a.6.1
 */
object TokenClassFromBinary {
	/**
	 * A Token Class that gets its values from a DataInput, using the really
	 * compact encoding generated by [[com.rayrobdod.deductionTactics.serialization.CompileTokenClassesToBinary]]
	 * @since a.6.1
	 */
	def apply(reader:java.io.DataInput):TokenClass = {
		val retVal = new TokenClass(
			name = {
				val bytes = new Array[Byte](nameLength)
				reader.readFully(bytes)
				new String(bytes.takeWhile{_ != 0})
			},
			
			atkElement = Elements(reader.readByte()),
			atkWeapon = Weaponkinds(reader.readByte()),
			atkStatus = Statuses(reader.readByte()),
			body = BodyTypes(reader.readByte()),
			
			range = reader.readByte().intValue,
			speed = reader.readByte().intValue,
			weakStatus = Statuses(reader.readByte()),
			weakDirection = Directions(reader.readByte()),
			weakWeapon = Weaponkinds.values.map{(x:Weaponkind) =>
				((x, reader.readFloat() ))
			}.toMap
		)
		reader.skipBytes(imageLocLength)
		retVal
	}
	
	val nameLength = 20;
	val enumsLength = 8 + (5 * 4); // 8 bytes + 5 floats
	val imageLocLength = 80;
	
	val totalLength = nameLength + enumsLength + imageLocLength;
}