package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction

import javax.swing.{JPanel, JLabel, Icon}
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout}
import com.rayrobdod.deductionTactics.Token

import javax.swing.{JList, ListCellRenderer}
import com.rayrobdod.boardGame.{BeSelected, StartOfTurn, EndOfTurn}
import com.rayrobdod.deductionTactics.{AttackForDamage, AttackForStatus}

import scala.swing.Reactions.Reaction
import scala.swing.event.Event

import java.awt.event.{MouseAdapter, MouseEvent}

/**
 * @author Raymond Dodge
 * @version 20 Jan 2012
 * @version 14 Feb 2012 - now interacts with {@link BeSelected}
 * @version 21 Mar 2012 - modified reactions for new event model
 * @version 05 Apr 2012 - unbroke UpdateAct such that it will respond to stuff now
 * @version 06 Apr 2012 - apparently UpdateAct only works for my tokens and at the EndOfTurn.
 * @version 06 Apr 2012 - Somehow managed to break UpdateAct such that it only works for enemy tokens, not your own.
 * @version 03 Aug 2012 - If the corresponding token is selected, the panel will request itself to be visible.
 */
class TokenPanel(val token:Token) extends JPanel
{
	setLayout(new GridBagLayout)
	
	val tokenClass = new TokenClassPanel(token.tokenClass)
	val hitpoints = new JLabel(token.currentHitpoints + " / " + token.maximumHitpoints) 
	val status = new JLabel(token.currentStatus.map{_.icon}.getOrElse(TokenClassPanel.unknownIcon))
	val statusTurnsLeft = new JLabel("" + token.currentStatusTurnsLeft)
	
	val statusRow = new JPanel(){
		add(hitpoints)
		add(status)
		add(statusTurnsLeft)
		setBackground(null)
	}
	tokenClass.setBackground(null)
	
	add(statusRow, new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER})
	add(tokenClass, new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER})
	
	token.reactions += UpdateAct
	/** add to all tokens and players */
	object UpdateAct extends Reaction
	{
		case object DelayedUpdate extends Event
		
		override def apply(e:Event) =
		{
			hitpoints.setText(token.currentHitpoints + " / " + token.maximumHitpoints)
			status.setIcon(token.currentStatus.map{_.icon}.getOrElse(TokenClassPanel.unknownIcon))
			statusTurnsLeft.setText("" + token.currentStatusTurnsLeft)
			
			
			{e match {
				case DelayedUpdate => {}
				case _ => token ! DelayedUpdate
			}}
		}
		
		override def isDefinedAt(e:Event) = {e match {
			case EndOfTurn => true
			case StartOfTurn => true
			case AttackForDamage(target,_,_,_) => true //target == token
			case AttackForStatus(target,_,_) => true //target == token
			case BeSelected(_) => true
			case DelayedUpdate => true
			case _ => false
		}}
	}
	
	token.reactions += SelectedAct
	/** Add to token */
	object SelectedAct extends Reaction
	{
		override def apply(e:Event):Unit = {e match {
			case BeSelected(x) => {
				TokenPanel.this.setBackground(if (x) {
					new java.awt.Color(184, 207, 229)
				}else{null})
				
				if (x) {
					TokenPanel.this.scrollRectToVisible(
						new java.awt.Rectangle(
							new java.awt.Point(0,0),
							TokenPanel.this.getSize()
						)
					)
				}
			}
		}}
		
		override def isDefinedAt(e:Event) = {e match {
			case BeSelected(_) => true
			case _ => false
		}}
	}
	
	this.addMouseListener(SelectMouseListener)
	object SelectMouseListener extends MouseAdapter
	{
		override def mouseClicked(e:MouseEvent) = {
			token ! BeSelected(true)
		}
	}
}


/**
 * @author Raymond Dodge
 * @version 20 Jan 2012 - c/p and modified from TokenClassListRender
 * @version 06 Feb 2012 - made the thing's color the same as the list's prefered seleciton color
 * @version 11 Feb 2012 - renamed from TokenListRender to TokenListRenderer
 */
object TokenListRenderer extends ListCellRenderer[Token]
{
	/**
	 *
	 */
	def getListCellRendererComponent(list:JList[_ <: Token], value:Token, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = new TokenPanel(value)
		if (isSelected)
		{
			returnValue.setBackground(list.getSelectionBackground)
			returnValue.statusRow.setBackground(list.getSelectionBackground)
			returnValue.tokenClass.setBackground(list.getSelectionBackground)
			returnValue.tokenClass.atkRow.setBackground(list.getSelectionBackground)
			returnValue.tokenClass.weakRow.setBackground(list.getSelectionBackground)
		}
		value ! BeSelected(isSelected)
		
		returnValue
	}
}
