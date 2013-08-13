package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.view.{
		AvailibleTilesheetListModel, TilesheetListRenderer}
import com.rayrobdod.boardGame.view.{Tilesheet}
import javax.swing.{JFrame, JList}

/**
 * @author Raymond Dodge
 * @version 15 Apr 2012
 */
object ChooseTilesheetComponentTest extends App
{
	val list = new JList[Tilesheet](AvailibleTilesheetListModel)
	list.setCellRenderer(TilesheetListRenderer)
	
	val frame:JFrame = new JFrame()
	frame.getContentPane.add(list)
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(400,200)
	frame.setVisible(true)
}
