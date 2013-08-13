package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.view.BoardGamePanel
import com.rayrobdod.deductionTactics.{CannonicalListOfTokens,
		CannonicalToken, CannonicalTokenClass, generateField}
import javax.swing.JFrame
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{Moved, StartOfTurn}

/**
 * @author Raymond Dodge
 * @version 19-20 Jan 2012
 */
object BoardGamePanelTest extends App
{
	val field = generateField
	
	val tokenList = new CannonicalListOfTokens(
		Seq(
			((0 to 4).map{(i:Int) => new CannonicalToken( CannonicalTokenClass.allKnown(i) )}),
			((4 to 0 by -1).map{(i:Int) => new CannonicalToken( CannonicalTokenClass.allKnown(i) )})
		)
	)
	
	val panel = new BoardGamePanel(tokenList, field)
	
	tokenList.tokens(0)(0) ! Moved(field.space(1,1),true)
	tokenList.tokens(0)(1) ! Moved(field.space(1,3),true)
	tokenList.tokens(0)(2) ! Moved(field.space(1,5),true)
	tokenList.tokens(0)(3) ! Moved(field.space(1,7),true)
	tokenList.tokens(0)(4) ! Moved(field.space(1,9),true)
	tokenList.tokens(1)(0) ! Moved(field.space(8,1),true)
	tokenList.tokens(1)(1) ! Moved(field.space(8,3),true)
	tokenList.tokens(1)(2) ! Moved(field.space(8,5),true)
	tokenList.tokens(1)(3) ! Moved(field.space(8,7),true)
	tokenList.tokens(1)(4) ! Moved(field.space(8,9),true)
	
	val frame:JFrame = new JFrame()
	frame.getContentPane.add(panel)
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	frame.setSize(750,600)
	frame.setVisible(true)
	frame.doLayout()
	
	tokenList.tokens.foreach{_.foreach{_.start}}
}
