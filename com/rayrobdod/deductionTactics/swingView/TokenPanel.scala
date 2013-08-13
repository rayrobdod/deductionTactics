package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction

import javax.swing.{JPanel, JLabel, Icon}
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout}
import com.rayrobdod.deductionTactics.Token

import javax.swing.{JList, ListCellRenderer}
import com.rayrobdod.swing.{GridBagConstraintsFactory}

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
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 29 Jan 2013 - Using the new class: com.rayrobdod.swing.GridBagConstraintsFactory
 * @version 2013 Jun 14 - using makeIconFor instead of per-class icon properties
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
 */
class TokenPanel(val token:Token) extends JPanel
{
	private val ICON_SIZE = 32
	
	setLayout(new GridBagLayout)
	
	val tokenClass = new TokenClassPanel(token.tokenClass)
	val hitpoints = new JLabel(token.currentHitpoints + " / " + token.maximumHitpoints) 
	val status = new JLabel( makeIconFor(token.currentStatus, ICON_SIZE) )
	val statusTurnsLeft = new JLabel("" + token.currentStatusTurnsLeft)
	
	val statusRow = new JPanel()
	statusRow.add(hitpoints)
	statusRow.add(status)
	statusRow.add(statusTurnsLeft)
	statusRow.setBackground(null)
	
	tokenClass.setBackground(null)
	
	add(statusRow, GridBagConstraintsFactory( gridwidth = GridBagConstraints.REMAINDER ))
	add(tokenClass, GridBagConstraintsFactory( gridwidth = GridBagConstraints.REMAINDER ))
	
	token.addUpdateReaction(UpdateAct)
	/** add to all tokens and players */
	object UpdateAct extends Function0[Unit] {
		override def apply():Unit = {
			hitpoints.setText(token.currentHitpoints + " / " + token.maximumHitpoints)
			status.setIcon( makeIconFor(token.currentStatus, ICON_SIZE) )
			statusTurnsLeft.setText("" + token.currentStatusTurnsLeft)
		}
	}
	
	token.addSelectedReaction(SelectedAct)
	/** Add to token */
	object SelectedAct extends Function1[Boolean, Unit]
	{
		override def apply(x:Boolean):Unit = {
			
				TokenPanel.this.setBackground(if (x) {
					new java.awt.Color(184, 207, 229)
				} else {null})
				
				if (x) {
					TokenPanel.this.scrollRectToVisible(
						new java.awt.Rectangle(
							new java.awt.Point(0,0),
							TokenPanel.this.getSize()
						)
					)
				}
			
		}
	}
	
	this.addMouseListener(SelectMouseListener)
	object SelectMouseListener extends MouseAdapter
	{
		override def mouseClicked(e:MouseEvent) = {
			token.beSelected(true)
		}
	}
}


/**
 * @author Raymond Dodge
 * @version 20 Jan 2012 - c/p and modified from TokenClassListRender
 * @version 06 Feb 2012 - made the thing's color the same as the list's prefered seleciton color
 * @version 11 Feb 2012 - renamed from TokenListRender to TokenListRenderer
 * @version 2013 Aug 07 - ripples from rewriting BoardGameToken
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
		value.beSelected(isSelected)
		
		returnValue
	}
}
