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
