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
 * @version 15 Apr 2012 - moving icons
 */
object Weaponkinds
{
	class Weaponkind(val id:Int, val name:String, val classType:String) extends NameAndIcon
	{
		lazy val icon:Icon = {
			loadIcon(this.getClass().getResource("/com/rayrobdod/glyphs/weapon/" +
					name.toLowerCase.dropRight(4) + ".svg"))
		}
		
		val genericTokenClassFile = "/sprites/generic/" + classType + ".png"
		val attackEffectFile = "/sprites/effects/" + name + " strike.png"
	}
	
	val Bladekind = new Weaponkind(0, "Bladekind", "swordsman")
	val Bluntkind = new Weaponkind(1, "Bluntkind", "clubman")
	val Spearkind = new Weaponkind(2, "Spearkind", "pikeman")
	val Whipkind  = new Weaponkind(3, "Whipkind", "whipman")
	
	def values = Seq[Weaponkind](Bladekind, Bluntkind, Spearkind, Whipkind)
	def withName(s:String) = values.find{_.name.equalsIgnoreCase(s + "kind")}.get
	def apply(x:Int) = values.find{_.id == x}.get
}