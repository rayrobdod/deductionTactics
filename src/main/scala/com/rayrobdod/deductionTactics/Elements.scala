package com.rayrobdod.deductionTactics

import scala.collection.immutable.{Seq, Set}
import com.rayrobdod.deductionTactics.LoggerInitializer.{elementsLogger => logger}

/**
 * An enumeration for the elements
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 01 Feb 2012 - changed "filter{}.head" to "find{}.get"
 * @version 02 Feb 2012 - subtrait Element now extends NameAndIcon
 * @version 15 Feb 2012 - implementing Element.toString
 * @version 27 Feb 2012 - adding Element.color
 * @version 15 Apr 2012 - moving icons
 * @version 11 Jul 2012 - replacing apply(x) = values.find{_.id == x}.get with  apply(x) = values(x)
 * @version 29 Jul 2012 - making withName throw a NoSuchElementException with a better message
 * @version 30 Jul 2012 - Element never obeyed the transetivity requirement of Ordered, so it no longer implements that class
 * @version 2013 Jun 13 - Element is now a class rather than a trait with a single implementor
 * @version 2013 Jun 14 - Element no longer extends NameAndIcon; removing icon method
 * @version 2013 Aug 19 - Element no longer contains a color
 
 */
object Elements {
	
	final class Element(val id:Int, val name:String) {
		def damageModifier(other:Element):Float = {
			((((other.id - this.id) % 5) + 5) % 5) match {
				case 0 => 1f
				case 1 => 2f
				case 2 => 1.5f
				case 3 => .75f
				case 4 => .5f
			}
		}
		
		def compare(other:Element):Int = {
			val damageModifier = this.damageModifier(other)
			
			if (damageModifier > 1) {-1}
			else if (damageModifier < 1) {1}
			else {0}
		}
		
		override def toString = "com.rayrobdod.deductionTactics.Elements." + name
	}
	
	val Light:Element    = new Element(0, "Light"   )
	val Electric:Element = new Element(1, "Electric")
	val Fire:Element     = new Element(2, "Fire"    )
	val Frost:Element    = new Element(3, "Frost"   )
	val Sound:Element    = new Element(4, "Sound"   )
	
	def values = Seq[Element](Light, Electric, Fire, Frost, Sound)
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