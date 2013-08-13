package com.rayrobdod.deductionTactics

import scala.collection.immutable.{Seq, Set}
import javax.swing.{ImageIcon, Icon}
import com.rayrobdod.swing.NameAndIcon

/**
 * An enumeration for the statuses
 * 
 * May in the future contain what the status does
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 27 Jan 2012 - using Seq.find().get instead of Seq.filter().head
 * @version 02 Feb 2012 - subtrait Element now extends NameAndIcon
 * @version 15 Apr 2012 - moving icons
 * @version 03 Jun 2012 - adding Heal
 * @version 04 Jun 2012 - adding toString to Status
 */
object Statuses
{
	class Status(val id:Int, val name:String) extends NameAndIcon
	{
		lazy val icon:Icon = {
			loadIcon(this.getClass().getResource("/com/rayrobdod/glyphs/status/" + name.toLowerCase + ".svg"))
		}
		
		override def toString = "com.rayrobdod.deductionTactics.Elements." + name
	}
	
	
	val Sleep = new Status(0, "Sleep") // no move
	val Burn = new Status(1, "Burn") // damage
	val Blind = new Status(2, "Blind") // can't attack
	val Confuse = new Status(3, "Confuse") // move three space or attack before player given control
	val Neuro = new Status(4, "Neuro") // damage + move a space or attack before player given control
	val Snake = new Status(5, "Snake") // damage + move one space per turn max
	val Heal = new Status(6, "Heal") // undamage (given that you can't attack partners...)
	
	def values = Seq[Status](Sleep, Burn, Blind, Confuse, Neuro, Snake, Heal)
	def withName(s:String) = values.find{_.name equalsIgnoreCase s}.get
	def apply(x:Int) = values.find{_.id == x}.get
}
