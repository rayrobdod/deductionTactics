package com.rayrobdod.deductionTactics.consoleView

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction

import com.rayrobdod.deductionTactics.{TokenClass, Weaponkinds}
import scala.runtime.{AbstractFunction1 => Function1}

/**
 * @author Raymond Dodge
 * @version 2012 Dec 01
 * @version 2012 Dec 02 - manual optimization: private getName
 */
object TokenClassPrinter extends Function1[TokenClass,Unit]
{
	private def out = System.out
	
	import com.rayrobdod.swing.NameAndIcon
	private object getName extends Function1[NameAndIcon, String] {
		def apply(x:NameAndIcon) = x.name
	}
	
	
	
	def apply(tokenClass:TokenClass) = {
		out.println(tokenClass.name);
		
		out.print("Speed: ");
		out.print(tokenClass.speed.getOrElse(0));
		out.print(" Range: ");
		out.println(tokenClass.range.getOrElse(0));
		
		out.print("Attack:   ")
		out.print(tokenClass.atkElement.map{getName}.getOrElse{"Unknown"});
		out.print("; ");
		out.print(tokenClass.atkWeapon.map{getName}.getOrElse{"Unknown"});
		out.print("; ");
		out.println(tokenClass.atkStatus.map{getName}.getOrElse{"Unknown"});
		
		out.print("Weakness: ")
		out.print(tokenClass.weakDirection.map{getName}.getOrElse{"Unknown"});
		out.print("; ");
		out.print(getWeakWeapon(tokenClass));
		out.print("; ");
		out.println(tokenClass.weakStatus.map{getName}.getOrElse{"Unknown"});
	}
	
	private def getWeakWeapon(tokenClass:TokenClass) = {
		val maxWeakness = tokenClass.weakWeapon.map{_._2.getOrElse(0f)}.max
		
		if (maxWeakness == 0f) {
			"Unknown"
		} else {
			tokenClass.weakWeapon.maxBy{_._2.getOrElse(0f)}._1.name
		}
	}
}
