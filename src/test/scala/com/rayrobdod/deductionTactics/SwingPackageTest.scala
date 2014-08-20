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
package swing

import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{RectangularField, Space}
import org.scalatest.{FunSuite, FunSpec}
import org.scalatest.prop.PropertyChecks

class makeIconFor extends FunSpec {
			
	
	describe ("provides an icon for everything") {
		it ("Statuses") {
			Statuses.values.map{swingView.makeIconFor(_)}
		}
		it ("Elements") {
			Elements.values.map{swingView.makeIconFor(_)}
		}
		it ("Weaponkinds") {
			Weaponkinds.values.map{swingView.makeIconFor(_)}
		}
		it ("Directions") {
			Directions.values.map{swingView.makeIconFor(_)}
		}
		it ("None") {
			swingView.makeIconFor(None)
		}
	}
}
