package com.rayrobdod.deductionTactics.test

import com.rayrobdod.deductionTactics.view.BoardGamePanel
import com.rayrobdod.deductionTactics.{_}
import javax.swing.JFrame
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{Moved, StartOfTurn,
		EndOfTurn, BeSelected}
import com.rayrobdod.deductionTactics.ai.{HumanAI,
		BlindAttackAI, SleepAbuserAI}
import scala.swing.Reactions
import scala.swing.event.Event

/**
 * @author Raymond Dodge
 * @version 20 Jan 2012 - 14 Feb 2012
 * @version 29 Feb 2012 - changing ais
 * @version 21 Mar 2012 - changing method call due to change of name in PlayerAI
 */
object BoardGamePanelPlayerTest extends App
{
	val field = generateField
//	val ais = Seq(new HumanAI, new BlindAttackAI)
//	val ais = Seq(new BlindAttackAI, new HumanAI)
//	val ais = Seq(new HumanAI, new SleepAbuserAI)
//	val ais = Seq(new HumanAI, new HumanAI)
//	val ais = Seq(new BlindAttackAI, new BlindAttackAI)
	val ais = Seq(new SleepAbuserAI, new BlindAttackAI)
//	val ais = Seq(new SleepAbuserAI, new SleepAbuserAI)
	
	val tokenClasses = ais.map{_.buildTeam} 
	val canonTokens = tokenClasses.map{_.map{new CannonicalToken(_)}}
	val mirrorTokens = canonTokens.map{_.map{new MirrorToken(_)}}
	
	val canonListTokens = new CannonicalListOfTokens(canonTokens)
	val playerListOfTokens = canonTokens.zip(mirrorTokens).map{(
		(canon:Seq[CannonicalToken], mirror:Seq[MirrorToken]) => {
			new PlayerListOfTokens(canon, mirrorTokens diff Seq(mirror))
		}
	).tupled}
	val allTokens = (canonTokens ++ mirrorTokens).flatten
	
	// move tokens to starting positions
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
	
	frames.zip(panels).map{((frame:JFrame, panel:BoardGamePanel) => {
		frame.getContentPane add panel
		frame.setSize(750,600)
		frame.setVisible(true)
		frame.validate()
	}).tupled}
	frames(0).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	
	allTokens.foreach{_.start()}
	
	// shim to make the token components always go to where they are supposed to go.
	Thread.sleep(200)
	canonTokens.flatten.foreach{(x:CannonicalToken) => x ! Moved(x.currentSpace, true)}
	
//	Thread.sleep(4800)
	
	val players = playerListOfTokens.map(new Player(_))
	ais(0).initialize(players(0), field)
	ais(1).initialize(players(1), field)
	
	canonTokens.flatten.foreach{_.reactions += new PrintReactions}
	players.foreach{_.reactions += new PrintReactions}
	
	
	canonTokens.flatten.zip(mirrorTokens.flatten).foreach({(hisCannon:CannonicalToken, hisMirror:MirrorToken) => {
		canonTokens.flatten.foreach{(mine:CannonicalToken) => {
			mine.reactions.+=(new hisCannon.BeAttackedReaction(hisMirror))
		}}
	}}.tupled)
	
	players.zip(canonTokens).foreach({(p:Player, ts:Seq[CannonicalToken]) => {
		ts.foreach{(t:CannonicalToken) => {
			p.reactions.+=(t.TurnStartReaction)
			p.reactions.+=(t.AttackReaction)
			p.reactions.+=(t.MoveReaction)
			p.reactions.+=(new t.StatusAct(p.tokens))
		}}
	}}.tupled)
	canonTokens.foreach{(seq:Seq[CannonicalToken]) => {
		UnitAwareSpaceClass.tokens.tokens = UnitAwareSpaceClass.tokens.tokens :+ seq;
	}}
	allTokens.foreach{(x:Token) => {
		x.reactions += new UnselectOtherTokens(x,allTokens) 
	}}
	
	players.foreach{_.start}
	
	
	new PlayerTurnCycler(players.zip(ais)).run()
	
	
	
	class PrintReactions extends Reactions.Reaction
	{
		override def apply(e:Event) = System.out.println(e)
		
		override def isDefinedAt(e:Event) = {e match {
			case EndOfTurn => false
			case StartOfTurn => false
			case Moved(_,_) => false
			case RequestMove(_,_) => false
			case BeSelected(_) => false
	//		case RequestAttackForDamage(_,_) => true
	//		case RequestAttackForStatus(_,_) => true
	//		case AttackForDamage(_,_,_,_) => true
	//		case AttackForStatus(_,_,_) => true
			case _ => false
		}}
	}
}

/**
 * @author Raymond Dodge
 * @version 14 Feb 2012
 */
class UnselectOtherTokens(token:Token, otherTokens:Seq[Token])
		extends Reactions.Reaction
{
	def apply(event:Event) = {
		(otherTokens diff Seq[Token](token)).foreach{_ ! BeSelected(false)}
	}
	
	def isDefinedAt(e:Event) = {e match {
		case BeSelected(true) => true
		case _ => false
	}}
}
