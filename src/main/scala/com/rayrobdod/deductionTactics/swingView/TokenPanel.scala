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
	
	val tokenClass = new TokenClassPanel(token.tokenClass.get)
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
	
}


