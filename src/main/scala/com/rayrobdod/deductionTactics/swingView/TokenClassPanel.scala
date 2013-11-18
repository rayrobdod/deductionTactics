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

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction

import scala.collection.immutable.Seq
import javax.swing.{JPanel, JLabel, Icon, JProgressBar,
		DefaultBoundedRangeModel}
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout}
import com.rayrobdod.deductionTactics.{TokenClass, Weaponkinds}
import com.rayrobdod.swing.GridBagConstraintsFactory

/**
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 * @version 20 Jan 2012 - modifying due to change in TokenClass
 * @version 01 Feb 2012 - implemented paint(Graphics), which updates the children and calls the super
 * @version 14 Feb 2012 - child panels now change color with the TokenClassPanel
 * @version 20 Apr 2012 - reducing the number of anonimous classes by replacing a few identiacl ones with one object
 * @version 05 Jun 2012 - adding JProgressbars indicating weapon weaknesses
 * @version 05 Jun 2012 - changes due to weakWeapon change in [[com.rayrobdod.deductionTactics.TokenClass]]
 * @version 11 Jun 2012 - adding command to update weapon weakness bars
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 02 Dec 2012 - manual optimizations: RemainderGridBagConstraints
 * @version 29 Jan 2013 - Using the new class: com.rayrobdod.swing.GridBagConstraintsFactory
 * @version 2013 Jun 14 - using makeIconFor instead of per-class icon properties
 * @version 2013 Jun 14 - getWeakWeaponIcon only traverses the traverses weakWeapon once now
 * @version 2013 Jun 17 - putting subcomponent update stuff in "doLayout" instead of "repaint"
 * @version 2013 Jun 25 - weakWeaponPanel changed from val to object because it seems more Proguard-friendly
 * @version 2013 Aug 08 - uses package.tokenClassNameToIcon and package.generateGenericIcon instead of token.icon
 */
class TokenClassPanel(val tokenClass:TokenClass) extends JPanel(new GridBagLayout)
{
	private val ICON_SIZE = 32
	
	val icon = new JLabel
	val name = new JLabel
	val range = new JLabel
	val speed = new JLabel
	val atkWeapon = new JLabel
	val atkElement = new JLabel
	val atkStatus = new JLabel
	val weakWeapon = new JLabel
	val weakStatus = new JLabel
	val weakDirection = new JLabel
	
	val atkRow = new JPanel()
	atkRow.add(atkElement)
	atkRow.add(atkWeapon)
	atkRow.add(atkStatus)
	atkRow.setBackground(null)
	
	val weakRow = new JPanel()
	weakRow.add(weakDirection)
	weakRow.add(weakWeapon)
	weakRow.add(weakStatus)
	weakRow.setBackground(null)
	
	object weaponWeakPanel extends JPanel(new java.awt.GridLayout(5,1)){
		val addends:Seq[JProgressBar] = Weaponkinds.values.map{(e:Weaponkind) => 
			new JProgressBar(new TokenClassPanel.TokenWeakRangeModel(tokenClass, e)){
				setString(e.name)
			//	setStringPainted(true)
				
				override def paint(g:java.awt.Graphics)
				{
					setIndeterminate(this.getModel.getValueIsAdjusting)
					super.paint(g)
				}
			}
		}
		addends.foreach{this.add(_)}
	}
	
	// manual optimization of anon classes
	val RemainderGridBagConstraints = GridBagConstraintsFactory(gridwidth = GridBagConstraints.REMAINDER)
	
	add(icon, GridBagConstraintsFactory(gridheight = 2))
	add(name, RemainderGridBagConstraints)
	add(range, new GridBagConstraints())
	add(speed, RemainderGridBagConstraints)
	add(new JLabel("Atk:"), new GridBagConstraints())
	add(atkRow, RemainderGridBagConstraints)
	add(new JLabel("Weak:"), new GridBagConstraints())
	add(weakRow, RemainderGridBagConstraints)
	add(weaponWeakPanel, RemainderGridBagConstraints)
	
	def canEquals(other:Any) = other.isInstanceOf[TokenClassPanel]
	override def equals(other:Any) = {
		if (this.canEquals(other))
		{
			val other2 = other.asInstanceOf[TokenClassPanel]
			if (other2.canEquals(this))
			{
				this.tokenClass == other2.tokenClass
			}
			else false
		}
		else false
	}
	
	override def doLayout()
	{
		this.icon.setIcon({
			tokenClassNameToIcon.getOrElse(tokenClass.name, generateGenericIcon(tokenClass))
		})
		this.name.setText(tokenClass.name)
		this.range.setText("Range: " + tokenClass.range.getOrElse("?"))
		this.speed.setText("Speed: " + tokenClass.speed.getOrElse("?"))
		this.atkWeapon.setIcon( makeIconFor(tokenClass.atkWeapon, ICON_SIZE) )
		// this.atkWeapon.setToolTipText( tokenClass.atkWeapon.map{_.name}.getOrElse("???") )
		this.atkElement.setIcon( makeIconFor(tokenClass.atkElement, ICON_SIZE) )
		this.atkStatus.setIcon(  makeIconFor(tokenClass.atkStatus, ICON_SIZE) )
		this.weakWeapon.setIcon( getWeakWeaponIcon() )
		this.weakStatus.setIcon( makeIconFor(tokenClass.weakStatus, ICON_SIZE) )
		this.weakDirection.setIcon( makeIconFor(tokenClass.weakDirection, ICON_SIZE) )
		this.weaponWeakPanel.addends.zip(Weaponkinds.values).foreach(
			{(bar:JProgressBar, e:Weaponkind) =>
				// NOTE: This seems like it would put a lot of work on th GC
				bar.setModel(new TokenClassPanel.TokenWeakRangeModel(tokenClass, e))
		}.tupled)
		
		super.doLayout()
	}
	
	private def getWeakWeaponIcon() = {
		val maxWeakness = tokenClass.weakWeapon.map{
				(x) => (( x._1, x._2.getOrElse(0f) ))
		}.maxBy{_._2}
		
		if (maxWeakness._2 == 0f) {
			unknownIcon(ICON_SIZE)
		} else {
			makeIconFor(maxWeakness._1, ICON_SIZE)
		}
	}
}

object TokenClassPanel
{
	class TokenWeakRangeModel(tokenClass:TokenClass, kind:Weaponkind)
			extends DefaultBoundedRangeModel(10, 0, 5, 20)
	{
		(tokenClass.weakWeapon(kind)) match
		{
			case None => this.setValueIsAdjusting(true)
			case Some(x:Float) => this.setValue((x * 10).intValue)
		}
	}
}
