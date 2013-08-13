package com.rayrobdod.deductionTactics.swingView

import java.awt.Color
import scala.collection.immutable.{Seq, Set}
import javax.swing.{JLabel, JList, ImageIcon}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import java.awt.image.BufferedImage
import com.rayrobdod.boardGame.{BeSelected, StartOfTurn,
		Space => BoardGameSpace, RectangularSpace, TokenMovementCost,
		PhysicalStrikeCost, Moved}
import java.awt.EventQueue.invokeLater
import com.rayrobdod.deductionTactics.{Token, ListOfTokens,
		AttackForDamage, AttackForStatus}

/**
 * A reaction that hilights the spaces a unit can move to or attack
 *
 * @author Raymond Dodge
 * @version 11 Feb 2012 - 12 Feb 2012
 * @version 13 Feb 2012 - using java.awt.EventQueue.invokeLater to eliminate OutOfBounds errors while repainting a component
 			It only seems to notice when removing components, not when adding them.
 * @version 13 Feb 2012 - no longer hilights spaces that cannot be moved to as attackable when guy cannot attack.
 * @version 15 Feb 2012 - now reacts to things that can change what a token can do.
 * @version 05 Apr 2012 - adding TokenCosts to Space and SpaceClass methods that now require them
 * @version 05 Apr 2012 - Made the RangeSpaces take into account that you can't attack from someplace you can't reach.
 * @version 06 Apr 2012 - Now reasponds to AttackFor* and Moved, rather than RequestAttackFor* and RequestMove,
			meaning it now updates when the token moves or attacks.
 * @version 28 Oct 2012 - changing a name due to change in BoardGamePanel
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 */
class HighlightMovableSpacesReaction(token:Token, panel:BoardGamePanel, allTokens:ListOfTokens) extends Reaction
{
	val highlightLayer = new javax.swing.JPanel(panel.centerpiece.lowLayer.getLayout)
	highlightLayer.setPreferredSize(panel.centerpiece.lowLayer.getPreferredSize)
//	val highlightLayer = panel.tokenLayer

	val transparent = new java.awt.Color(0,0,0,0);
	
	val hilights:Map[BoardGameSpace, JLabel] = panel.centerpiece.spaces.map{(space:RectangularSpace) =>
		val label = new JLabel 
		label.setIcon(null)
		label.setBackground(transparent)
		label.setOpaque(false)
		highlightLayer.add(label)
		(space, label)
	}.toMap
	highlightLayer.setBackground(transparent)
	highlightLayer.setOpaque(false)
	panel.centerpiece.add(highlightLayer,0)
	
	var wasSelected:Boolean = false
	
	override def apply(e:Event) = {
		e match {
			case BeSelected(isSelected) => wasSelected = isSelected
			case _ => {}
		}
		
		highlightLayer.setPreferredSize(panel.centerpiece.lowLayer.getPreferredSize)
		
		hilights.foreach({(space:BoardGameSpace, label:JLabel) =>
			space match{
				case rs:RectangularSpace =>
				{
					val hilightedLabel = panel.centerpiece.spaceLabelMap(rs)
					label.setIcon(null)
				}
			}
		}.tupled)
		
		if (wasSelected) {
			val tokenMaxSpeed = token.tokenClass.speed.getOrElse(0)
			val tokenMaxRange = token.tokenClass.range.getOrElse(0)
			val tokenCurSpeed = token.canMoveThisTurn
			val tokenCurRange = tokenMaxRange // the computation for curRangeSpaces checks for this
			
			//val noUnitIsInSpace:Function1[BoardGameSpace,Boolean] = {(x:BoardGameSpace) => ! allTokens.aliveTokens.flatten.map{_.currentSpace}.contains(x)}
			
			val maxSpeedSpaces = token.currentSpace.spacesWithin(tokenMaxSpeed, token, TokenMovementCost) - token.currentSpace
			val maxRangeSpaces = (maxSpeedSpaces + token.currentSpace).map{_.spacesWithin(tokenMaxRange, token, PhysicalStrikeCost)}.flatten -- maxSpeedSpaces - token.currentSpace
			val curSpeedSpaces = token.currentSpace.spacesWithin(tokenCurSpeed, token, TokenMovementCost) - token.currentSpace
			val curRangeSpaces = if (token.canAttackThisTurn) {(curSpeedSpaces + token.currentSpace).map{_.spacesWithin(tokenMaxRange, token, PhysicalStrikeCost)}.flatten -- curSpeedSpaces - token.currentSpace} else {Seq.empty}
			
			import HighlightMovableSpacesReaction._
			maxSpeedSpaces.foreach{hilights(_).setIcon(maximumSpeedIcon)}
			maxRangeSpaces.foreach{hilights(_).setIcon(maximumRangeIcon)}
			curSpeedSpaces.foreach{hilights(_).setIcon(currentSpeedIcon)}
			curRangeSpaces.foreach{hilights(_).setIcon(currentRangeIcon)}
		}
	}
	
	override def isDefinedAt(e:Event) = {e match {
		case BeSelected(_) => true
		case AttackForDamage(_,_,_,_) => true
		case AttackForStatus(_,_,_) => true
		case Moved(_,_) => true
		case _ => false
	}}
}

/**
 * Useful functions used by the associated class
 *
 * @author Raymond Dodge
 * @version 11 Feb 2012 - 12 Feb 2012
 */
object HighlightMovableSpacesReaction
{
	def createSolidColorIcon(c:Color, alpha:Int) =
	{
		val image = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB)
		val g = image.getGraphics
		g.setColor(c)
		g.fillRect(0,0,32,32)
		image.getAlphaRaster.setPixels(0,0,32,32,
				Seq.fill(32*32){alpha}.toArray)
		new ImageIcon(image)
	}
	
	val currentSpeedIcon = createSolidColorIcon(new Color(0x3333DD), 128)
	val currentRangeIcon = createSolidColorIcon(new Color(0xDD3333), 128)
	val maximumSpeedIcon = createSolidColorIcon(new Color(0x3333DD), 32)
	val maximumRangeIcon = createSolidColorIcon(new Color(0xDD3333), 32)
	
}
