package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.swingView.ChooseAIsComponent
import javax.swing.JFrame

/**
 * @author Raymond Dodge
 * @version ?? ??? ????
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.test
			to com.rayrobdod.deductionTactics.test
 */
object ChooseAIsComponentTest extends App
{
	val frame:JFrame = new JFrame()
	frame.getContentPane.add(new ChooseAIsComponent())
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(400,200)
	frame.setVisible(true)
}
