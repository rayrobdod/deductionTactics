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
package com.rayrobdod.deductionTactics.swingView.game

import org.scalatest.FunSpec
import com.rayrobdod.deductionTactics.TokenIndex

class CurrentlySelectedTokenPropertyTest extends FunSpec {
	
	describe ("game.CurrentlySelectedTokenProperty") {
		it ("initial value is None") {
			assertResult(None){new CurrentlySelectedTokenProperty().get}
		}
		it ("value can be changed") {
			val dut = new CurrentlySelectedTokenProperty()
			val SET_VALUE = Some((5,5))
			
			dut.set(SET_VALUE)
			
			assertResult(SET_VALUE){dut.get}
		}
		it ("change in value is observable") {
			val dut = new CurrentlySelectedTokenProperty()
			val l = new SetOnceChangeListner
			dut.addChangeListener(l)
			val SET_VALUE = Some((3,7))
			
			dut.set(SET_VALUE)
			
			assert(l.hasBeenCalled)
			assertResult(SET_VALUE)(l.setTo)
		}
	}
	
	class SetOnceChangeListner extends CurrentlySelectedTokenProperty.ChangeListener {
		private[this] var _hasBeenCalled:Boolean = false
		private[this] var _setTo:Option[TokenIndex] = Some((-1,-1))
		
		def apply(x:Option[TokenIndex]) = {
			if (_hasBeenCalled) {
				throw new IllegalStateException("ChangeListener called twice")
			} else {
				_hasBeenCalled = true
				_setTo = x
			}
		}
		
		def hasBeenCalled = _hasBeenCalled
		def setTo:Option[TokenIndex] = _setTo
	}
}
