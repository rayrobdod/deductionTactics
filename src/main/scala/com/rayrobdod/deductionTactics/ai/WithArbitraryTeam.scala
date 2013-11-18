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
package ai

import scala.util.Random
import com.rayrobdod.boardGame.{RectangularField => Field}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList, JFormattedTextField}
import java.awt.event.{ActionListener, ActionEvent}
import java.awt.BorderLayout
import com.rayrobdod.deductionTactics.swingView.InputFrame

/**
 * A decorator for PlayerAIs. It intercepts the buildTeam command and creates
 * a random one using the package randomTeam method
 *
 * @author Raymond Dodge
 * @version 09 Jul 2012
 * @version 12 Jul 2012 - giving seedBox a default value; changed initialize to base.initialize
 * @version 24 Jul 2012 - changing seedBox from being a JTextField to a JFormattedTextField
 * @version 03 Aug 2012 - replacing an annonymous inner class with an instance of InputFrame
 * @version 2012 Nov 30 - modifying toString to include the base
 */
final class WithArbitraryTeam(val base:PlayerAI) extends PlayerAI
{
	/** Forwards command to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	/** Forwards command to base */
	def initialize(player:Player, field:Field) = base.initialize(player, field)
	
	/** chooses a team randomly */
	def buildTeam = {
		
		val buildingLock = new Object()
		
		val seedBox = new JFormattedTextField(java.text.NumberFormat.getIntegerInstance())
			seedBox.setValue(Random.nextInt)
		
		val frame = new InputFrame("Choose Seed", seedBox, new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				seedBox.commitEdit()
				if (seedBox.isEditValid()) {
					buildingLock.synchronized { buildingLock.notifyAll }
				}
			}
		})
		
		buildingLock.synchronized {
			frame.setVisible(true)
			buildingLock.wait
		}
		
		frame.setVisible(false)
		ai.randomTeam(new Random(somethingToLong(seedBox.getValue)))
	}
	
	def somethingToLong(a:Any):Long = a match {
		case x:Long => x
		case x:Int => x
		case x:Double => x.longValue
		case x:String => java.lang.Long.parseLong(x)
		case _ => java.lang.Long.parseLong(a.toString)
	}
	
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithRandomTeam]}
	override def equals(other:Any) = {
		this.canEquals(other) && other.asInstanceOf[WithRandomTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithRandomTeam].base
	}
	override def hashCode = base.hashCode * 7 + 23
	
	override def toString = base.toString + " with " + this.getClass.getName
}
