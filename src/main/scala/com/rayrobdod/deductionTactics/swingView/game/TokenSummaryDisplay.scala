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
package com.rayrobdod.deductionTactics.swingView.game

import java.awt.{Component, GridBagLayout, GridBagConstraints}
import javax.swing.{JComponent, JPanel}
import com.rayrobdod.deductionTactics.Token
import com.rayrobdod.deductionTactics.swingView.TokenPanel

/**
 * 
 */
final class TokenSummaryDisplay {
	private[this] val panel = new JPanel(new GridBagLayout)
	def component:Component = panel
	panel.setBackground(new java.awt.Color(0, true))
	panel.setOpaque(false)
	
	private[this] val constrs = new GridBagConstraints(
			0, 0,
			1, 1,
			1.0, 1.0,
			GridBagConstraints.NORTH,
			GridBagConstraints.NONE,
			new java.awt.Insets(0, 0, 0, 0),
			0, 0
	)
	
	/**
	 * @param placeInCorner see [[java.awt.GridBagConstraints#anchor]]
	 */
	def showDetailsOf(t:Option[Token], placeInCorner:Int) {
		panel.removeAll()
		constrs.anchor = placeInCorner
		t.map{t2 =>
			panel.add(new TokenPanel(t2), constrs)
		}
		panel.validate()
	}
}
