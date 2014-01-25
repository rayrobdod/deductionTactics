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
import com.rayrobdod.deductionTactics.BodyTypes.BodyType
import com.rayrobdod.deductionTactics.Directions.Direction

import javax.swing.JProgressBar

/** 
 * @author Raymond Dodge
 * @version a.5.2
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

