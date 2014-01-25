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
import com.rayrobdod.deductionTactics.BodyTypes.BodyType
import com.rayrobdod.deductionTactics.Directions.Direction

import javax.swing.{JPanel, JLabel, JList, ListCellRenderer}
import java.awt.{GridBagLayout, GridBagConstraints}
import com.rayrobdod.deductionTactics.Token
import com.rayrobdod.swing.{GridBagConstraintsFactory}

import java.awt.event.{MouseAdapter, MouseEvent}

/**
 * A panel that displays information about a [[com.rayrobdod.deductionTactics.Token]]
 * 
 * @author Raymond Dodge
 * @version a.5.0
 */
class TokenPanel(val token:Token) extends JPanel
{
	private val ICON_SIZE = 32
	
	setLayout(new GridBagLayout)
	
	val tokenClass = new TokenClassPanel(token.tokenClass)
	val hitpoints = new JLabel(token.currentHitpoints + " / " + token.maximumHitpoints) 
	val status = new JLabel( makeIconFor(token.currentStatus, ICON_SIZE) )
	val statusTurnsLeft = new JLabel("" + token.currentStatusTurnsLeft)
	
	val statusRow = new JPanel()
	statusRow.add(hitpoints)
	statusRow.add(status)
	statusRow.add(statusTurnsLeft)
	statusRow.setBackground(null)
	
	tokenClass.setBackground(null)
	
	add(statusRow, GridBagConstraintsFactory( gridwidth = GridBagConstraints.REMAINDER ))
	add(tokenClass, GridBagConstraintsFactory( gridwidth = GridBagConstraints.REMAINDER ))
	
	token.updateReactions_+=(UpdateAct)
	/** add to all tokens and players */
	object UpdateAct extends Function0[Unit] {
		override def apply():Unit = {
			hitpoints.setText(token.currentHitpoints + " / " + token.maximumHitpoints)
			status.setIcon( makeIconFor(token.currentStatus, ICON_SIZE) )
			statusTurnsLeft.setText("" + token.currentStatusTurnsLeft)
		}
	}
	
	token.selectedReactions_+=(SelectedAct)
	/** Add to token */
	object SelectedAct extends Function1[Boolean, Unit]
	{
		override def apply(x:Boolean):Unit = {
			
			TokenPanel.this.setBackground(if (x) {
				new java.awt.Color(184, 207, 229)
			} else {null})
				
			if (x) {
				TokenPanel.this.scrollRectToVisible(
					new java.awt.Rectangle(
						new java.awt.Point(0,0),
						TokenPanel.this.getSize()
					)
				)
			}
			
		}
	}
	
	this.addMouseListener(SelectMouseListener)
	object SelectMouseListener extends MouseAdapter
	{
		override def mouseClicked(e:MouseEvent) = {
			token.beSelected(true)
		}
	}
}


/**
 * @author Raymond Dodge
 * @version a.5.0
 */
object TokenListRenderer extends ListCellRenderer[Token]
{
	/**
	 *
	 */
	def getListCellRendererComponent(list:JList[_ <: Token], value:Token, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = new TokenPanel(value)
		returnValue.tokenClass.doLayout()
		if (isSelected)
		{
			returnValue.setBackground(list.getSelectionBackground)
			returnValue.statusRow.setBackground(list.getSelectionBackground)
			returnValue.tokenClass.setBackground(list.getSelectionBackground)
			returnValue.tokenClass.atkRow.setBackground(list.getSelectionBackground)
			returnValue.tokenClass.weakRow.setBackground(list.getSelectionBackground)
		}
		value.beSelected(isSelected)
		
		returnValue
	}
}
