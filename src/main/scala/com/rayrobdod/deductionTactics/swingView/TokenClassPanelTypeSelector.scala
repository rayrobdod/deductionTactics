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
	val nameAndIconFunction = {(x:TokenClass) => new JLabel(x.name,
			tokenClassToIcon(x), SwingConstants.LEFT)
	}
	
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
