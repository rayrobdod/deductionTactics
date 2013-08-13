package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.Elements
import com.rayrobdod.deductionTactics.Elements.Element
import javax.swing.{BoxLayout, JPanel, JLabel, SwingConstants}
import javax.swing.BoxLayout.{PAGE_AXIS => pageAxis}
import java.awt.GridLayout

/**
 * @version ?? ??? ????
 * @version 27 Apr 2012 - Changing from nexted Box Layouts to a Grid Layout
 * @version 27 Apr 2012 - Made show full circle, meaning the head is shown twice
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 */
class ElementPentagonReminderComponent extends JPanel
{
	setLayout(new GridLayout(2,11))
	
	Elements.values.foreach{(x:Element) => 
		add(new JLabel(x.icon, SwingConstants.CENTER))
		add(new JLabel("<<", SwingConstants.CENTER))
	}
	add(new JLabel(Elements.values.head.icon))
	
	import Elements._
	private val elementList = Seq[Element](Light, Fire, Sound, Electric, Frost)
	
	elementList.foreach{(x:Element) => 
		add(new JLabel(x.icon, SwingConstants.CENTER))
		add(new JLabel("<", SwingConstants.CENTER))
	}
	add(new JLabel(elementList.head.icon))
}
