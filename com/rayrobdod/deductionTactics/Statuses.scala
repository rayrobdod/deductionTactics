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
 * @version 10 Jul 2012 - replacing apply(x) = values.find{_.id == x}.get with  apply(x) = values(x)
 * @version 29 Jul 2012 - making withName throw a NoSuchElementException with a better message
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
	
	/** no move */
	val Sleep = new Status(0, "Sleep")
	/** damage */
	val Burn = new Status(1, "Burn")
	/** can't attack */
	val Blind = new Status(2, "Blind")
	/** move three space or attack before player given control */
	val Confuse = new Status(3, "Confuse")
	/** damage + move a space or attack before player given control */
	val Neuro = new Status(4, "Neuro")
	/** damage + move one space per turn max */
	val Snake = new Status(5, "Snake")
	/** undamage (given that you can't attack partners...) */
	val Heal = new Status(6, "Heal")
	
	def values = Seq[Status](Sleep, Burn, Blind, Confuse, Neuro, Snake, Heal)
	def apply(x:Int) = values(x) //.find{_.id == x}.get
	
	def withName(s:String) = {
		try {
			values.find{_.name equalsIgnoreCase s}.get
		} catch {
			case x:NoSuchElementException => 
				val y = new NoSuchElementException("No element with name: "+ s)
				y.initCause(x)
				throw y
		}
	}
}
