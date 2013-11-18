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

import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{JRadioButton, ButtonGroup, JLabel, JComponent, SwingConstants, JPanel}
import com.rayrobdod.deductionTactics.TokenClass

/**
 * @author Raymond Dodge
 * @version 08 Aug 2012
 * @version 14 Aug 2012 - moving inner object TokenClassPanelTypeSelector to 
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 08 - uses package.tokenClassNameToIcon and package.generateGenericIcon instead of token.icon
 */
class TokenClassPanelTypeSelector(modend:AllKnownTokenClassesComponent) extends JPanel
{
	val full = new JRadioButton("Full", true)
	val noWeaponWeak = new JRadioButton("Without Weapon Weakness", false)
	val nameAndIcon = new JRadioButton("Name and Icon", false)
	
	private val forTypeGroup = new ButtonGroup()
	forTypeGroup.add(full)
	forTypeGroup.add(noWeaponWeak)
	forTypeGroup.add(nameAndIcon)
	
	this.add(full)
	this.add(noWeaponWeak)
	this.add(nameAndIcon)
	
	val fullFunction = {(x:TokenClass) => new TokenClassPanel(x)}
	val noWeaponWeakFunction = {(x:TokenClass) => {val y = new TokenClassPanel(x); y.remove(y.weaponWeakPanel); y}}
	val nameAndIconFunction = {(x:TokenClass) => new JLabel(x.name, {
			tokenClassNameToIcon.getOrElse(x.name, generateGenericIcon(x))
	}, SwingConstants.LEFT)}
	
	full.addActionListener{new ChangeTokenClassView(fullFunction)}
	noWeaponWeak.addActionListener{new ChangeTokenClassView(noWeaponWeakFunction)}
	nameAndIcon.addActionListener{new ChangeTokenClassView(nameAndIconFunction)}
	
	class ChangeTokenClassView(tcToComp:Function1[TokenClass,JComponent]) extends ActionListener
	{
		def actionPerformed(e:ActionEvent) = {
			modend.tokenClassToComponent = tcToComp;
		}
	}
}
