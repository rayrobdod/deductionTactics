package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.swingView.BoardGamePanel
import com.rayrobdod.deductionTactics.{CannonicalListOfTokens,
		CannonicalToken, CannonicalTokenClass, PlayerListOfTokens,
		MirrorToken, generateField}
import javax.swing.JFrame
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{Moved, StartOfTurn}

/**
 * @author Raymond Dodge
 * @version 20 Jan 2012
 */
object BoardGamePanelMultiTest extends App
{
	val field = generateField("/com/rayrobdod/deductionTactics/maps/emptyField")
	
	val canonTokens = Seq(
			((0 to 4).map{(i:Int) => new CannonicalToken( CannonicalTokenClass.allKnown(i) )}),
			((4 to 0 by -1).map{(i:Int) => new CannonicalToken( CannonicalTokenClass.allKnown(i) )})
		)
	val mirrorTokens = canonTokens.map{_.map{new MirrorToken(_)}}
	
	val canonListTokens = new CannonicalListOfTokens(canonTokens)
	val playerListOfTokens = canonTokens.zip(mirrorTokens).map{
		(canonMirror:(Seq[CannonicalToken], Seq[MirrorToken])) => {
			val (canon, mirror) = canonMirror
			new PlayerListOfTokens(canon, mirrorTokens diff Seq(mirror))
		}
	}
	
	canonTokens(0)(0) ! Moved(field.space(1,1),true)
	canonTokens(0)(1) ! Moved(field.space(1,3),true)
	canonTokens(0)(2) ! Moved(field.space(1,5),true)
	canonTokens(0)(3) ! Moved(field.space(1,7),true)
	canonTokens(0)(4) ! Moved(field.space(1,9),true)
	canonTokens(1)(0) ! Moved(field.space(8,1),true)
	canonTokens(1)(1) ! Moved(field.space(8,3),true)
	canonTokens(1)(2) ! Moved(field.space(8,5),true)
	canonTokens(1)(3) ! Moved(field.space(8,7),true)
	canonTokens(1)(4) ! Moved(field.space(8,9),true)
	
	val panels = Seq(
			new BoardGamePanel(canonListTokens, field),
			new BoardGamePanel(playerListOfTokens(0), field),
			new BoardGamePanel(playerListOfTokens(1), field)
	)
	
	val frames = Seq(
			new JFrame("CanonFrame"),
			new JFrame("Player1Frame"),
			new JFrame("Player2Frame")
	)
	
	frames.zip(panels).map{(framePanel:(JFrame, BoardGamePanel)) => {
		val (frame, panel) = framePanel
		frame.getContentPane add panel
		frame.setSize(750,600)
		frame.setVisible(true)
		frame.doLayout()
	}}
	frames(0).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	
	canonTokens.foreach{_.foreach{_.start}}
	mirrorTokens.foreach{_.foreach{_.start}}
}
