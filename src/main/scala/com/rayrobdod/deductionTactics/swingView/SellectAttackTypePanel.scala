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

import javax.swing.{JPanel, JRadioButton, ButtonGroup}
import com.rayrobdod.deductionTactics.{Token, CannonicalToken}
		
/**
 * @author Raymond Dodge
 * @version 06 Apr 2012
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
 */
class SellectAttackTypePanel extends JPanel
{
	val forDamage = new JRadioButton("Damage Attack", true) 
	val forStatus = new JRadioButton("Status Attack", false)
	
	private val forTypeGroup = new ButtonGroup()
	forTypeGroup.add(forDamage)
	forTypeGroup.add(forStatus)
	
	this.add(forDamage)
	this.add(forStatus)
	
	def requestAttackForType(attacker:CannonicalToken, defender:Token) =
	{
		if (forTypeGroup.getSelection == forStatus.getModel) {
			attacker.tryAttackStatus(defender)
		} else {
			// See if having neither button selected is possible
			// before adding this
			//forDamage.setSelected(true)
			
			attacker.tryAttackDamage(defender)
		}
	}
}
