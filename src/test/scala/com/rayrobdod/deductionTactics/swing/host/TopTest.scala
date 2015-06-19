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
package com.rayrobdod.deductionTactics.swingView.host

import org.scalatest.{FunSuite, FunSpec}
import org.scalatest.prop.PropertyChecks
import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.ai.SwingInterface
import com.rayrobdod.deductionTactics.PlayerAI
import com.rayrobdod.boardGame.{RectangularField, Space}

class TopTest extends FunSpec {
	
	describe ("host.Top") {
		it ("has values selected by default") {
			val t = new Top()
			val target = new MockNextListener
			t.addNextListener(target)
			
			// doClick using the backdoorsy method
			val frame = java.awt.Window.getOwnerlessWindows.find{_ match {
				case x:javax.swing.JFrame => x.isDisplayable
				case _ => false
			}}.get.asInstanceOf[javax.swing.JFrame]
			System.out.println(frame)
			val nextButton:javax.swing.JButton = {
				frame.getContentPane
					.getComponents
					.find{_.getName == "buttonPanel"}
					.map{_.asInstanceOf[javax.swing.JPanel]}
					.map{x => x.getComponents
						.find{_.getName == "nextButton"}
						.map{_.asInstanceOf[javax.swing.JButton]}
					}.flatten
					.get
			}
			nextButton.doClick()
			// should already be disposed after the click, but since
			// it not being disposed would be bad... worth a try/finally?
			frame.dispose()
			
			assert(target.hasBeenCalled)
			assertResult(Seq(new SwingInterface, new SwingInterface)){target.ais}
			assertResult("com/rayrobdod/deductionTactics/maps/emptyField.json"){target.map}
			assertResult(Vector(
				Vector((1,5), (1,3), (1,7), (1,1), (1,9), (1,4), (1,6), (1,2), (1,8), (1,0)),
				Vector((8,5), (8,3), (8,7), (8,1), (8,9), (8,4), (8,6), (8,2), (8,8), (8,0))
			)){target.startSpaces}
		}
	}
	
	
	class MockNextListener extends Top.NextListener {
		override def apply(ais:Seq[PlayerAI], map:String, startSpaces:Seq[Seq[(Int,Int)]]):Unit = {
			if (this._hasBeenCalled) {throw new IllegalStateException("MockNextListener.apply called twice")}
			
			this._hasBeenCalled = true
			this._ais = ais
			this._map = map
			this._startSpaces = startSpaces
		}
		
		private var _hasBeenCalled:Boolean = false
		private var _ais:Seq[PlayerAI] = Nil
		private var _map:String = ""
		private var _startSpaces:Seq[Seq[(Int,Int)]] = Nil
		def hasBeenCalled:Boolean = _hasBeenCalled
		def ais:Seq[PlayerAI] = _ais
		def map:String = _map
		def startSpaces:Seq[Seq[(Int,Int)]] = _startSpaces
	}
}
