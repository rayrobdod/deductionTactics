/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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
package com.rayrobdod.deductionTactics.swingView.menuBar

import javax.swing.{JPanel, BoxLayout}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import com.rayrobdod.deductionTactics.TokenClass
import com.rayrobdod.deductionTactics.ai.TokenClassSuspicion
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.swingView.TokenClassPanel

/**
 * @version a.6.0
 */
class FilterKnownTokenClassesComponent extends JPanel {
	val tokenClassesAndComps = TokenClass.allKnown.map{(x:TokenClass) => (( x, new TokenClassPanel(x) ))}.toMap
	
	this.setLayout(new BoxLayout(this, boxYAxis))
	this.applyFilter(new TokenClassSuspicion)
	
	def applyFilter(tokenClass:TokenClassSuspicion):Unit = {
		val applicable = tokenClassesAndComps.filterKeys(new TokenClassMatcher(tokenClass))
		
		this.removeAll()
		applicable.values.foreach{this.add(_)}
	}
}

/**
 * @version a.6.0
 */
class TokenClassMatcher(template:TokenClassSuspicion) extends Function1[TokenClass,Boolean] {
	def apply(tc:TokenClass):Boolean = {
		template.atkElement.map{_ == tc.atkElement}.getOrElse(true) &&
		template.atkWeapon.map{_ == tc.atkWeapon}.getOrElse(true) &&
		template.atkStatus.map{_ == tc.atkStatus}.getOrElse(true) &&
		(template.speed.getOrElse(0) <= tc.speed) &&
		(template.range.getOrElse(0) <= tc.range) &&
		template.weakDirection.map{_ == tc.weakDirection}.getOrElse(true) &&
		template.weakStatus.map{_ == tc.weakStatus}.getOrElse(true) &&
		template.weakWeapon.keys.forall{(key:Weaponkind) =>
			template.weakWeapon(key).map{_ == tc.weakWeapon(key)}.getOrElse(true)
		}
	}
}
