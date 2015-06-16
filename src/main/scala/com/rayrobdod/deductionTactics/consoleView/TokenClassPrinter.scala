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
package com.rayrobdod.deductionTactics.consoleView

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.BodyType
import com.rayrobdod.deductionTactics.Directions.Direction

import com.rayrobdod.deductionTactics.ai.TokenClassSuspision
import com.rayrobdod.deductionTactics.{TokenClass, Weaponkinds}
import scala.runtime.{AbstractFunction1 => Function1}
import scala.language.reflectiveCalls

/**
 * @version a.6.0
 */
object TokenClassPrinter extends Function1[TokenClass,Unit]
{
	private def out = System.out
	private val elseString = "Unknown"
	
	val getName = {(x:{def name:String}) => x.name}
	
	
	def apply(tokenClass:TokenClass) = {
		out.println(tokenClass.name);
		
		out.print("Speed: ");
		out.print(tokenClass.speed);
		out.print("  Range: ");
		out.println(tokenClass.range);
		
		out.print("Attack:   ")
		out.print(tokenClass.atkElement.name);
		out.print("; ");
		out.print(tokenClass.atkWeapon.name);
		out.print("; ");
		out.println(tokenClass.atkStatus.name);
		
		out.print("Weakness: ")
		out.print(tokenClass.weakDirection.name);
		out.print("; ");
		out.print(getWeakWeapon(tokenClass.weakWeapon));
		out.print("; ");
		out.println(tokenClass.weakStatus.name);
	}
	
	/**
	 * @since a.6.0
	 */
	def apply(tokenClass:TokenClassSuspision) = {
		out.println("???");
		
		out.print("Speed: ");
		out.print(tokenClass.speed.getOrElse("?"));
		out.print("  Range: ");
		out.println(tokenClass.range.getOrElse("?"));
		
		out.print("Attack:   ")
		out.print(tokenClass.atkElement.map{_.name}.getOrElse("?"));
		out.print("; ");
		out.print(tokenClass.atkWeapon.map{_.name}.getOrElse("?"));
		out.print("; ");
		out.println(tokenClass.atkStatus.map{_.name}.getOrElse("?"));
		
		out.print("Weakness: ")
		out.print(tokenClass.weakDirection.map{_.name}.getOrElse("?"));
		out.print("; ");
		out.print(getWeakWeapon(tokenClass.weakWeapon.map{x => ((x._1, x._2.getOrElse(0f) ))}));
		out.print("; ");
		out.println(tokenClass.weakStatus.map{_.name}.getOrElse("?"));
	}
	
	/**
	 * @version a.6.0
	 */
	private def getWeakWeapon(weakWeapon:Map[Weaponkind,Float]) = {
		val maxWeakness = weakWeapon.map{
				(x) => (( x._1, x._2 ))
		}.maxBy{_._2}
		
		if (maxWeakness._2 == 0f) {
			elseString
		} else {
			maxWeakness._1.name
		}
	}
}
