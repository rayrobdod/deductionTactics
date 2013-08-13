package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics._

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction

import javax.swing.JFrame
import javax.swing.{JLabel, JPanel, JList}
import java.awt.event.{MouseAdapter, MouseEvent}
import java.lang.Integer
import javax.swing.event.{ListSelectionListener, ListSelectionEvent}
import javax.swing.{ListModel, ListCellRenderer, DefaultListCellRenderer, AbstractListModel, JScrollBar, JProgressBar}
import java.awt.Adjustable.{HORIZONTAL => horizontal}
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout}

import com.rayrobdod.swing.NameAndIconCellRenderer

/** 
 * @author Raymond Dodge
 * @version 11 Feb 2012 - cut vast majority of content from {@link HumanSuspicionTest} and pasted here
 * @version 11 Jun 2012 - adding listeners to WeaponWeakPanel
 */
class HumanSuspicionsPanel(tokenClass:SuspicionsTokenClass) extends TokenClassPanel(tokenClass)
{
	this.atkElement.addMouseListener(new NameAndIconSetterChooserFrameMaker[Element](
			null +: Elements.values, tokenClass.atkElement_=_, this))
	this.atkWeapon.addMouseListener(new NameAndIconSetterChooserFrameMaker[Weaponkind](
			null +: Weaponkinds.values, tokenClass.atkWeapon_=_, this))
	this.atkStatus.addMouseListener(new NameAndIconSetterChooserFrameMaker[Status](
			null +: Statuses.values, tokenClass.atkStatus_=_, this))
	this.weakStatus.addMouseListener(new NameAndIconSetterChooserFrameMaker[Status](
			null +: Statuses.values, tokenClass.weakStatus_=_, this))
	this.weakDirection.addMouseListener(new NameAndIconSetterChooserFrameMaker[Direction](
			null +: Directions.values, tokenClass.weakDirection_=_, this))
	this.speed.addMouseListener(new IntSetterChooserFrameMaker(
			tokenClass.speed_=_, this))
	this.range.addMouseListener(new IntSetterChooserFrameMaker(
			tokenClass.range_=_, this))
	
	this.weaponWeakPanel.addends.zip(Weaponkinds.values).foreach(
		{(bar:JProgressBar, weapon:Weaponkind) =>
			bar.addMouseListener(new WeaponMultiplerSetterChooserFrameMaker(
			{(i:Option[Float]) => tokenClass.weakWeapon += ((weapon, i))}, this))
	}.tupled)
	
	this.name.addMouseListener(new ClassSynchonizerFrameMaker(
			tokenClass, this))
}

