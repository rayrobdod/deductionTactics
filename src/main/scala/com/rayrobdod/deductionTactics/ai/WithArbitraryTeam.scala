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
package ai

import scala.util.Random
import scala.collection.immutable.Seq

/**
 * A decorator for PlayerAIs. It intercepts the buildTeam command and creates
 * a random one using the package randomTeam method
 *
 * @author Raymond Dodge
 * @version a.6.0
 */
final class WithArbitraryTeam(val base:PlayerAI) extends DecoratorPlayerAI(base)
{
	private var input:Int = 0;
	
	/** chooses a team randomly */
	override def selectTokenClasses(size:Int):Seq[TokenClass] = {
		import javax.swing.JOptionPane.PLAIN_MESSAGE
		
		val pane = new javax.swing.JOptionPane;
		pane.setWantsInput(true);
		pane.setMessage("Choose a RNG seed");
		pane.setInitialSelectionValue(Random.nextInt);
		pane.selectInitialValue() 
		pane.setMessageType(PLAIN_MESSAGE);
		val dialog = pane.createDialog("WithArbitraryTeam")
		dialog.setVisible(true);
		input = pane.getInputValue.hashCode
		
		ai.randomTeam(size, new Random(input))
	}
	
	/** chooses a subset of selectedTokenClasses randomly */
	override def narrowTokenClasses(
				selectedTokenClasses:Seq[Seq[TokenClass]],
				maxResultSize:Int,
				index:Int
	):Seq[TokenClass] = Random.shuffle(selectedTokenClasses(index)).take(maxResultSize)
	
	
	
	protected def canEquals(other:Any):Boolean = {other.isInstanceOf[WithArbitraryTeam]}
	override def equals(other:Any):Boolean = {
		this.canEquals(other) && other.asInstanceOf[WithArbitraryTeam].canEquals(this) &&
				this.base == other.asInstanceOf[WithArbitraryTeam].base
	}
	override def hashCode:Int = base.hashCode * 7 + 23
	
	override def toString:String = base.toString + " with " + this.getClass.getName
}
