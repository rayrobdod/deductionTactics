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
 * @version 2013 Jun 14 - getWeakWeapon only traverses the traverses weakWeapon once now
 * @version 2013 Jun 14 - undoing getName as classes no longer share a trait
 */
object TokenClassPrinter extends Function1[TokenClass,Unit]
{
	private def out = System.out
	private val elseString = "Unknown"
	
	
	
	def apply(tokenClass:TokenClass) = {
		out.println(tokenClass.name);
		
		out.print("Speed: ");
		out.print(tokenClass.speed.getOrElse(0));
		out.print(" Range: ");
		out.println(tokenClass.range.getOrElse(0));
		
		out.print("Attack:   ")
		out.print(tokenClass.atkElement.map{_.name}.getOrElse{elseString});
		out.print("; ");
		out.print(tokenClass.atkWeapon.map{_.name}.getOrElse{elseString});
		out.print("; ");
		out.println(tokenClass.atkStatus.map{_.name}.getOrElse{elseString});
		
		out.print("Weakness: ")
		out.print(tokenClass.weakDirection.map{_.name}.getOrElse{elseString});
		out.print("; ");
		out.print(getWeakWeapon(tokenClass));
		out.print("; ");
		out.println(tokenClass.weakStatus.map{_.name}.getOrElse{elseString});
	}
	
	private def getWeakWeapon(tokenClass:TokenClass) = {
		val maxWeakness = tokenClass.weakWeapon.map{
				(x) => (( x._1, x._2.getOrElse(0f) ))
		}.maxBy{_._2}
		
		if (maxWeakness._2 == 0f) {
			elseString
		} else {
			maxWeakness._1.name
		}
	}
}
