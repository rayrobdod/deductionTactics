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
import com.rayrobdod.swing.NameAndIcon

/** 
 * @author Raymond Dodge
 * @version 11 Feb 2012 - cut vast majority of content from {@link HumanSuspicionTest} and pasted here
 * @version 11 Jun 2012 - adding listeners to WeaponWeakPanel
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Jun 17 - Inducing component revalidate upon actions in this class
 */
class HumanSuspicionsPanel(tokenClass:SuspicionsTokenClass) extends TokenClassPanel(tokenClass)
{
	this.atkElement.addMouseListener(new NameAndIconSetterChooserFrameMaker[Element](
			null +: Elements.values, new FunctionThenRevalidate(tokenClass.atkElement_=_), this))
	this.atkWeapon.addMouseListener(new NameAndIconSetterChooserFrameMaker[Weaponkind](
			null +: Weaponkinds.values, new FunctionThenRevalidate(tokenClass.atkWeapon_=_), this))
	this.atkStatus.addMouseListener(new NameAndIconSetterChooserFrameMaker[Status](
			null +: Statuses.values, new FunctionThenRevalidate(tokenClass.atkStatus_=_), this))
	this.weakStatus.addMouseListener(new NameAndIconSetterChooserFrameMaker[Status](
			null +: Statuses.values, new FunctionThenRevalidate(tokenClass.weakStatus_=_), this))
	this.weakDirection.addMouseListener(new NameAndIconSetterChooserFrameMaker[Direction](
			null +: Directions.values, new FunctionThenRevalidate(tokenClass.weakDirection_=_), this))
	this.speed.addMouseListener(new IntSetterChooserFrameMaker(
			new FunctionThenRevalidate(tokenClass.speed_=_), this))
	this.range.addMouseListener(new IntSetterChooserFrameMaker(
			new FunctionThenRevalidate(tokenClass.range_=_), this))
	
	this.weaponWeakPanel.addends.zip(Weaponkinds.values).foreach(
		{(bar:JProgressBar, weapon:Weaponkind) =>
			bar.addMouseListener(new WeaponMultiplerSetterChooserFrameMaker(
			{(i:Option[Float]) => tokenClass.weakWeapon += ((weapon, i))}, this))
	}.tupled)
	
	this.name.addMouseListener(new ClassSynchonizerFrameMaker(
			tokenClass, this))
	
	class FunctionThenRevalidate[A](fun:Function1[A,Any]) extends Function1[A,Any] {
		def apply(a:A) {
			fun(a)
			HumanSuspicionsPanel.this.revalidate()
			HumanSuspicionsPanel.this.repaint()
		}
	}
}

