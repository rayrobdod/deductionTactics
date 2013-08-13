package com.rayrobdod.deductionTactics.view

import javax.swing.{JPanel, JRadioButton, ButtonGroup}
import com.rayrobdod.deductionTactics.{Token, CannonicalToken,
			RequestAttackForStatus, RequestAttackForDamage}
		
/**
 * @author Raymond Dodge
 * @version 06 Apr 2012
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
		if (forTypeGroup.getSelection == forStatus.getModel)
		{
			RequestAttackForStatus(attacker, defender)
		}
		else
		{
			// See if having neither button selected is possible
			// before adding this
			//forDamage.setSelected(true)
			
			RequestAttackForDamage(attacker, defender)
		}
	}
}
