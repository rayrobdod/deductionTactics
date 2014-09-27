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
package com.rayrobdod.deductionTactics.swingView

import javax.swing.{JList, JButton, JPanel, JFrame, JScrollPane, BoxLayout}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.{TokenClass, CannonicalTokenClassBuilder => TokenClassBuilder }
import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind

/**
 * @author Raymond Dodge
 */
class FilterKnownTokenClassesComponent extends JPanel
{
	val tokenClassesAndComps = TokenClass.allKnown.map{(x:TokenClass) => (( x, new TokenClassPanel(x) ))}.toMap
	
	this.setLayout(new BoxLayout(this, boxYAxis))
	this.filter(new TokenClassBuilder)
	
	def filter(tokenClass:TokenClassBuilder) {
		val applicable = tokenClassesAndComps.filterKeys(new TokenClassMatcher(tokenClass))
		
		this.removeAll()
		applicable.values.foreach{this.add(_)}
	}
}

/**
 * @author Raymond Dodge
 */
class TokenClassMatcher(template:TokenClassBuilder) extends Function1[TokenClass,Boolean]
	{
		def apply(tc:TokenClass) = {
			(
				eitherIsNoneOrBothAreEqual(template.atkElement, tc.atkElement) &&
				eitherIsNoneOrBothAreEqual(template.atkWeapon, tc.atkWeapon) &&
				eitherIsNoneOrBothAreEqual(template.atkStatus, tc.atkStatus) &&
				eitherIsNoneOrAIsLessThanOrEqualToB(template.range, tc.range) &&
				eitherIsNoneOrAIsLessThanOrEqualToB(template.speed, tc.speed) &&
				
				eitherIsNoneOrBothAreEqual(template.weakDirection, tc.weakDirection) &&
				eitherIsNoneOrBothAreEqual(template.weakStatus, tc.weakStatus) &&
				template.weakWeapon.keys.forall{(key:Weaponkind) =>
						eitherIsNoneOrBothAreEqual(template.weakWeapon(key), tc.weakWeapon(key))} 
			)
		}
		
		private def eitherIsNoneOrBothAreEqual[A](a:Option[A], b:Option[A]) =
		{
			!a.isDefined || !b.isDefined || (a.get == b.get) 
		}
		
		private def eitherIsNoneOrBothAreEqual[A](a:Option[A], b:A) =
		{
			!a.isDefined || (a.get == b) 
		}
		
		private def eitherIsNoneOrAIsLessThanOrEqualToB(a:Option[Int], b:Option[Int]) =
		{
			!a.isDefined || !b.isDefined || (a.get <= b.get) 
		}
		
		private def eitherIsNoneOrAIsLessThanOrEqualToB(a:Option[Int], b:Int) =
		{
			!a.isDefined || (a.get <= b) 
		}
	}
