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
package com.rayrobdod.deductionTactics.swingView.host

import org.scalatest.{FunSpec}
import javax.swing.{JFrame, JButton, JPanel, JList, JScrollPane}
import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.ai.{BlindAttackAI, SwingInterface, WithRandomTeam}
import com.rayrobdod.deductionTactics.{Arena, PlayerAI, UniPassageSpaceClass, SpaceClass}
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.deductionTactics.swingView.ChooseAIsComponent

class TopTest extends FunSpec {
	
	describe ("host.Top") {
		it ("Has values selected by default", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(target.hasBeenCalled)
			assertResult(Seq(new SwingInterface, new SwingInterface)){target.ais}
			assertResult(Arena("Empty Field",
				RectangularField(
					(for(
						x <- 0 until 10;
						y <- 0 until 10
					) yield {
						((x, y), UniPassageSpaceClass.apply)
					}).toMap
				),
				startSpacesSeq = Seq(
					Seq(
						Seq((1 -> 5), (1 -> 3), (1 -> 7), (1 -> 1), (1 -> 9), (1 -> 4), (1 -> 6), (1 -> 2), (1 -> 8), (1 -> 0)),
						Seq((8 -> 5), (8 -> 3), (8 -> 7), (8 -> 1), (8 -> 9), (8 -> 4), (8 -> 6), (8 -> 2), (8 -> 8), (8 -> 0))
					),
					Seq(
						Seq((1 -> 5), (1 -> 3), (1 -> 7), (1 -> 4), (1 -> 6)),
						Seq((8 -> 5), (8 -> 3), (8 -> 7), (8 -> 4), (8 -> 6)),
						Seq((5 -> 1), (3 -> 1), (7 -> 1), (4 -> 1), (6 -> 1)),
						Seq((5 -> 8), (3 -> 8), (7 -> 8), (4 -> 8), (6 -> 8))
					)
				)
			)){target.map}
		}
		it ("Does not call the NextListener if cancel is called", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val cancelButton:JButton = getButton(frame, "cancelButton")
			cancelButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(! target.hasBeenCalled)
		}
		it ("Changes ai in return value when a new base ai is selected (1)", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val aisComp = getAIsComponent(frame)
			aisComp.aiLists(0).setSelectedIndex(2)
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(target.hasBeenCalled)
			assertResult(Seq(new BlindAttackAI, new SwingInterface)){target.ais}
		}
		it ("Changes ai in return value when a new base ai is selected (2)", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val aisComp = getAIsComponent(frame)
			aisComp.aiLists(1).setSelectedIndex(2)
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(target.hasBeenCalled)
			assertResult(Seq(new SwingInterface, new BlindAttackAI)){target.ais}
		}
		it ("Changes ai in return value when a new decorator ai is selected (2)", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val aisComp = getAIsComponent(frame)
			aisComp.aiDLists(1).setSelectedIndex(1)
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(target.hasBeenCalled)
			assertResult(Seq(new SwingInterface, new WithRandomTeam(new SwingInterface))){target.ais}
		}
		it ("Changes arena in return value when a new arena is selected", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val mapList = getMapList(frame)
			mapList.setSelectedIndex(2)
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(target.hasBeenCalled)
			assertResult(Arena.getAll(2)){target.map}
		}
		it ("Does not allow multiple arenas to be selected", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val mapList = getMapList(frame)
			mapList.setSelectedIndices(Array(1, 3))
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assertResult(Array(3)){mapList.getSelectedIndices}
			assert(target.hasBeenCalled)
			assertResult(Arena.getAll(3)){target.map}
		}
		it ("Changes player count when player count is changes", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val playerCount = getPlayerCountList(frame)
			playerCount.setSelectedIndex(1)
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assert(target.hasBeenCalled)
			assertResult(Seq(new SwingInterface, new SwingInterface, new SwingInterface, new SwingInterface)){target.ais}
		}
		it ("Will not continue if no arena is selected", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val mapList = getMapList(frame)
			mapList.setSelectedIndices(new Array[Int](0))
			
			val nextButton:JButton = getButton(frame, "nextButton")
			
			assert(! nextButton.isEnabled)
			nextButton.doClick()
			// dispose was not called
			assert(frame.isDisplayable)
			
			// cleanup
			val cancelButton:JButton = getButton(frame, "cancelButton")
			cancelButton.doClick()
			assert(! frame.isDisplayable)
		}
		it ("Will not continue if no playerCount is selected", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val playerCount = getPlayerCountList(frame)
			playerCount.setSelectedIndices(new Array[Int](0))
			
			val nextButton:JButton = getButton(frame, "nextButton")
			
			assert(! nextButton.isEnabled)
			nextButton.doClick()
			// dispose was not called
			assert(frame.isDisplayable)
			
			// cleanup
			val cancelButton:JButton = getButton(frame, "cancelButton")
			cancelButton.doClick()
			assert(! frame.isDisplayable)
		}
		it ("Does not allow multiple playerCount to be selected", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val playerCount = getPlayerCountList(frame)
			playerCount.setSelectedIndices(Array(0, 1))
			
			val nextButton:JButton = getButton(frame, "nextButton")
			nextButton.doClick()
			
			// dispose was called
			assert(! frame.isDisplayable)
			
			assertResult(Array(1)){playerCount.getSelectedIndices}
			assert(target.hasBeenCalled)
			assertResult(4){target.ais.size}
		}
		ignore ("Will not continue if no base ai is selected", RequiresGui) {
			val (frame, target) = this.createNextFrameAndListener()
			
			val aisComp = getAIsComponent(frame)
			aisComp.aiDLists(1).setSelectedIndices(new Array[Int](0))
			
			val nextButton:JButton = getButton(frame, "nextButton")
			
			assert(! nextButton.isEnabled)
			nextButton.doClick()
			// dispose was not called
			assert(frame.isDisplayable)
			
			// cleanup
			val cancelButton:JButton = getButton(frame, "cancelButton")
			cancelButton.doClick()
			assert(! frame.isDisplayable)
		}
	}
	
	
	private[this] def createNextFrameAndListener():(JFrame, MockNextListener) = {
		val t = new Top()
		val target = new MockNextListener
		t.addNextListener(target)
		
		val frame = java.awt.Window.getOwnerlessWindows.find{_ match {
			case x:JFrame => x.isDisplayable
			case _ => false
		}}.get.asInstanceOf[JFrame]
		
		(frame, target)
	}
	
	private[this] def getButton(frame:JFrame, buttonName:String):JButton = {
		frame.getContentPane
			.getComponents
			.find{_.getName == "buttonPanel"}
			.map{_.asInstanceOf[JPanel]}
			.map{x => x.getComponents
				.find{_.getName == buttonName}
				.map{_.asInstanceOf[JButton]}
			}.flatten
			.get
	}
	
	private[this] def getAIsComponent(frame:JFrame):ChooseAIsComponent = {
		frame.getContentPane
			.getComponents
			.find{_.getName == "topPanel"}
			.map{_.asInstanceOf[JPanel]}
			.map{x => x.getComponents
				.find{_.isInstanceOf[ChooseAIsComponent]}
				.map{_.asInstanceOf[ChooseAIsComponent]}
			}.flatten
			.get
	}
	
	private[this] def getMapList(frame:JFrame):JList[_] = {
		frame.getContentPane
			.getComponents
			.find{_.getName == "topPanel"}
			.map{_.asInstanceOf[JPanel]}
			.map{x => x.getComponents
				.find{_.getName == "mapChoosingPanel"}
				.map{_.asInstanceOf[JPanel]}
				.map{x => x.getComponents
					.find{_.getName == "mapListScrollPane"}
					.map{_.asInstanceOf[JScrollPane]}
					.map{x => Option(x.getViewport.getView)
						.find{_.getName == "mapList"}
						.map{_.asInstanceOf[JList[_]]}
					}.flatten
				}.flatten
			}.flatten
			.get
	}
	
	private[this] def getPlayerCountList(frame:JFrame):JList[_] = {
		frame.getContentPane
			.getComponents
			.find{_.getName == "topPanel"}
			.map{_.asInstanceOf[JPanel]}
			.map{x => x.getComponents
				.find{_.getName == "mapChoosingPanel"}
				.map{_.asInstanceOf[JPanel]}
				.map{x => x.getComponents
					.find{_.getName == "playerCount"}
					.map{_.asInstanceOf[JList[_]]}
				}.flatten
			}.flatten
			.get
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
		private var _map:Arena = Arena("", RectangularField(Map.empty[(Int, Int),SpaceClass]), Seq.empty)
		def hasBeenCalled:Boolean = _hasBeenCalled
		def ais:Seq[PlayerAI] = _ais
		def map:Arena = _map
	}
}
