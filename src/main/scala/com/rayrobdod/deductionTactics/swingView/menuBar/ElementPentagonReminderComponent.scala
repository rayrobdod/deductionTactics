/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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
package com.rayrobdod.deductionTactics.swingView.menuBar

import com.rayrobdod.deductionTactics.Elements
import com.rayrobdod.deductionTactics.Elements.Element
import javax.swing.{JPanel, JLabel, SwingConstants}
import java.awt.GridLayout
import com.rayrobdod.deductionTactics.swingView.makeIconFor

/**
 * A small component to remind the elemnt relationships
 */
class ElementPentagonReminderComponent extends JPanel
{
	setLayout(new GridLayout(2,11))
	
	Elements.values.foreach{(x:Element) => 
		add(new JLabel(makeIconFor(x), SwingConstants.CENTER))
		add(new JLabel("<<", SwingConstants.CENTER))
	}
	add(new JLabel(makeIconFor(Elements.values.head)))
	
	import Elements._
	private val elementList = Seq[Element](Light, Fire, Sound, Electric, Frost)
	
	elementList.foreach{(x:Element) => 
		add(new JLabel(makeIconFor(x), SwingConstants.CENTER))
		add(new JLabel("<", SwingConstants.CENTER))
	}
	add(new JLabel(makeIconFor(elementList.head)))
}
