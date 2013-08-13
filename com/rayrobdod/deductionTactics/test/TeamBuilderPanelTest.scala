package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.swingView.TeamBuilderPanel
import javax.swing.JFrame

/**
 * @author Raymond Dodge
 * @version ?? ??? ????
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.test
			to com.rayrobdod.deductionTactics.test
 */
object TeamBuilderPanelTest extends App
{
	val frame:JFrame = new JFrame()
	frame.getContentPane.add(new TeamBuilderPanel())
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(450,600)
	frame.setVisible(true)
}
