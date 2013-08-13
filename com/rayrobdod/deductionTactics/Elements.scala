package com.rayrobdod.deductionTactics

import scala.collection.immutable.{Seq, Set}
import javax.swing.Icon
import com.rayrobdod.swing.NameAndIcon
import java.awt.Color
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
 */
object Elements
{
	trait Element extends NameAndIcon
	{
		def id:Int
		def name:String
		def color:Color
		
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
		
		lazy val icon:Icon = {
			val PREFIX = "/com/rayrobdod/glyphs/elements/"
			val file = PREFIX + name.toLowerCase + ".svg"
			
			logger.finer(file);
			
			loadIcon(this.getClass().getResource(file))
		}
		
		override def toString = "com.rayrobdod.deductionTactics.Elements." + name
	}
	
	private class ElementImpl(override val id:Int, override val name:String, override val color:Color) extends Element
	
	val Light:Element = new ElementImpl(0, "Light", new Color(253,253,187))
	val Electric:Element = new ElementImpl(1, "Electric", Color.yellow)
	val Fire:Element = new ElementImpl(2, "Fire", Color.red)
	val Frost:Element = new ElementImpl(3, "Frost", new Color(170,170,255))
	val Sound:Element = new ElementImpl(4, "Sound", new Color(0,255,0))
	
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