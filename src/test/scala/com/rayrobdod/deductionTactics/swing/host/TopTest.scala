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
import com.rayrobdod.deductionTactics.{Arena, PlayerAI}
import com.rayrobdod.boardGame.{RectangularField, Space}

class TopTest extends FunSpec {
	
	describe ("host.Top") {
		it ("Has values selected by default") {
			val t = new Top()
			val target = new MockNextListener
			t.addNextListener(target)
			
			// doClick using the backdoorsy method
			val frame = java.awt.Window.getOwnerlessWindows.find{_ match {
				case x:javax.swing.JFrame => x.isDisplayable
				case _ => false
			}}.get.asInstanceOf[javax.swing.JFrame]
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
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(target.hasBeenCalled)
			assertResult(Seq(new SwingInterface, new SwingInterface)){target.ais}
			assertResult(Arena("Empty Field", Seq.fill(10,10){" "}, Map(
				2 -> List(
					List((1,5), (1,3), (1,7), (1,1), (1,9), (1,4), (1,6), (1,2), (1,8), (1,0)),
					List((8,5), (8,3), (8,7), (8,1), (8,9), (8,4), (8,6), (8,2), (8,8), (8,0))
				),
				4 -> List(
					List((1,5), (1,3), (1,7), (1,4), (1,6)),
					List((8,5), (8,3), (8,7), (8,4), (8,6)),
					List((5,1), (3,1), (7,1), (4,1), (6,1)),
					List((5,8), (3,8), (7,8), (4,8), (6,8))
				)
			))){target.map}
		}
		it ("Does not call the NextListener if cancel is called") {
			val t = new Top()
			val target = new MockNextListener
			t.addNextListener(target)
			
			// doClick using the backdoorsy method
			val frame = java.awt.Window.getOwnerlessWindows.find{_ match {
				case x:javax.swing.JFrame => x.isDisplayable
				case _ => false
			}}.get.asInstanceOf[javax.swing.JFrame]
			val nextButton:javax.swing.JButton = {
				frame.getContentPane
					.getComponents
					.find{_.getName == "buttonPanel"}
					.map{_.asInstanceOf[javax.swing.JPanel]}
					.map{x => x.getComponents
						.find{_.getName == "cancelButton"}
						.map{_.asInstanceOf[javax.swing.JButton]}
					}.flatten
					.get
			}
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(! target.hasBeenCalled)
		}
	}
	
	
	class MockNextListener extends Top.NextListener {
		override def apply(ais:Seq[PlayerAI], map:Arena):Unit = {
			if (this._hasBeenCalled) {throw new IllegalStateException("MockNextListener.apply called twice")}
			
			this._hasBeenCalled = true
			this._ais = ais
			this._map = map
		}
		
		private var _hasBeenCalled:Boolean = false
		private var _ais:Seq[PlayerAI] = Nil
		private var _map:Arena = Arena("", Nil, Map.empty)
		def hasBeenCalled:Boolean = _hasBeenCalled
		def ais:Seq[PlayerAI] = _ais
		def map:Arena = _map
	}
}
