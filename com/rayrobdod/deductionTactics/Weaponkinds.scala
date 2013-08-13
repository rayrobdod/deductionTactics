package com.rayrobdod.deductionTactics

import scala.collection.immutable.{Seq, Set}
import javax.swing.{ImageIcon, Icon}
import com.rayrobdod.swing.NameAndIcon

/**
 * An enumeration for the weaponkinds
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 02 Feb 2012 - changed "filter{}.head" to "find{}.get"
 * @version 02 Feb 2012 - subtrait Weaponkind now extends NameAndIcon
 * @version 27 Feb 2011 - added fileImage
 * @version 15 Apr 2012 - moving glyph icons
 * @version 03 Jun 2012 - adding Powderkind
 * @version 09 Jul 2012 - moving tokenClass icons and attackEffect icons
 * @version 10 Jul 2012 - replacing apply(x) = values.find{_.id == x}.get with  apply(x) = values(x)
 * @version 29 Jul 2012 - making withName throw a NoSuchElementException with a better message
 */
object Weaponkinds
{
	class Weaponkind(val id:Int, val name:String, val classType:String) extends NameAndIcon
	{
		lazy val icon:Icon = {
			loadIcon(this.getClass().getResource("/com/rayrobdod/glyphs/weapon/" +
					name.toLowerCase.dropRight(4) + ".svg"))
		}
		
		// TODO: figure out relative URLs.
		val genericTokenClassFile = "/com/rayrobdod/deductionTactics/tokenClasses/sprites/generic/" + classType + ".png"
		val attackEffectFile = "/com/rayrobdod/deductionTactics/tokenClasses/sprites/effects/" + name + " strike.png"
	}
	
	val Bladekind = new Weaponkind(0, "Bladekind", "swordsman")
	val Bluntkind = new Weaponkind(1, "Bluntkind", "clubman")
	val Spearkind = new Weaponkind(2, "Spearkind", "pikeman")
	val Whipkind  = new Weaponkind(3, "Whipkind", "whipman")
	val Powderkind= new Weaponkind(4, "Powderkind", "powderman")
	
	def values = Seq[Weaponkind](Bladekind, Bluntkind, Spearkind, Whipkind, Powderkind)
	def apply(x:Int) = values(x) //.find{_.id == x}.get
	
	def withName(s:String) = {
		try {
			values.find{_.name.equalsIgnoreCase(s + "kind")}.get
		} catch {
			case x:NoSuchElementException => 
				val y = new NoSuchElementException("No element with name: "+ s)
				y.initCause(x)
				throw y
		}
	}
}