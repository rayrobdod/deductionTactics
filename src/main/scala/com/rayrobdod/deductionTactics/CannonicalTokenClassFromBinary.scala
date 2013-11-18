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

/**
 * A Token Class that gets its values from a DataInput.
 * @author Raymond Dodge
 * @version 2013 Aug 06
 */
class CannonicalTokenClassFromBinary(reader:java.io.DataInput) extends CannonicalTokenClass
{
	import CannonicalTokenClassFromBinary.{nameLength, imageLocLength}
	
	override val name = {
		val bytes = new Array[Byte](nameLength)
		reader.readFully(bytes)
		new String(bytes.takeWhile{_ != 0})
	}
	
	override val atkElement = Some(Elements(reader.readByte()))
	override val atkWeapon = Some(Weaponkinds(reader.readByte()))
	override val atkStatus = Some(Statuses(reader.readByte()))
	override val body = Some(BodyTypes(reader.readByte()))
	
	override val range = Some(reader.readByte().intValue)
	override val speed = Some(reader.readByte().intValue)
	override val weakStatus = Some(Statuses(reader.readByte()))
	override val weakDirection = Some(Directions(reader.readByte()))
	override val weakWeapon = Weaponkinds.values.map{(x:Weaponkind) =>
		((x, Some(reader.readFloat()) ))
	}.toMap
	
	reader.skipBytes(imageLocLength)
}

/**
 * Constants relating to CannonicalTokenClassFromBinary
 * @author Raymond Dodge
 * @version 2013 Aug 06
 */
object CannonicalTokenClassFromBinary {
	val nameLength = 20;
	val enumsLength = 8 + (5 * 4); // 8 bytes + 5 floats
	val imageLocLength = 80;
	
	val totalLength = nameLength + enumsLength + imageLocLength;
}
