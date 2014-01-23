/*
	Deduction Tactics
	Copyright (C) 2014  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction

import scala.util.Random
import scala.runtime.{AbstractFunction1, AbstractFunction2}
import scala.collection.mutable.{SynchronizedQueue => MQueue}
import java.awt.image.BufferedImage
import java.awt.{Component, Container}
import java.awt.event.{ComponentAdapter, ComponentEvent}
import javax.swing.{JLabel, JComponent, Icon, ImageIcon}
import javax.swing.SwingUtilities.invokeLater
import com.rayrobdod.boardGame.Space
import com.rayrobdod.boardGame.swingView.{FieldViewer}
import com.rayrobdod.deductionTactics.{Token, ListOfTokens}
import com.rayrobdod.util.BlitzAnimImage
import javax.imageio.ImageIO
import java.awt.Color
import TokenComponentController._




final class TokenComponentController(fieldComp:FieldViewer,
		tokens:ListOfTokens) extends Runnable
{
	
	private val idleLock:Object = new Object
	private val actionQueue:MQueue[Action] = new MQueue
	
	private val tokenToComponentMap:Seq[(Token, JLabel)] = 
			tokens.tokens.flatten.map{(t:Token) => (( t, new JLabel ))}
	
	tokenToComponentMap.foreach({(t:Token, label:JLabel) =>
		object AddActionToQueueReaction extends 
					AbstractFunction2[Space, Boolean, Unit] with
					Function0[Unit] with 
					Token.DamageAttackedReactionType with
					Token.StatusAttackedReactionType {
			def apply() {
				actionQueue += new DiedAction(t)
				idleLock.synchronized{ idleLock.notifyAll() }
			}
			def apply(s:Space, b:Boolean) {
				actionQueue += new MovementAction(t, s)
				idleLock.synchronized{ idleLock.notifyAll() }
			}
			def apply(elem:Element, kind:Weaponkind, space:Space) {
				actionQueue += new DamageAction(t, tokenOnSpace(space), elem, kind)
				idleLock.synchronized{ idleLock.notifyAll() }
			}
			def apply(status:Status, space:Space) {
				actionQueue += new StatusAction(t, tokenOnSpace(space), status)
				idleLock.synchronized{ idleLock.notifyAll() }
			}
			
			private def tokenOnSpace(s:Space):Token = {
				tokens.tokens.flatten.find{_.currentSpace == s}.get
			}
		}
		object BeSelectedAct extends AbstractFunction1[Boolean, Unit] {
			def apply(b:Boolean) = {
				label.setOpaque(b)
				label.repaint()
			}
		}
		
		t.beDamageAttackedReactions_+=(AddActionToQueueReaction)
		t.beStatusAttackedReactions_+=(AddActionToQueueReaction)
		t.moveReactions_+=(AddActionToQueueReaction)
		t.diedReactions_+=(AddActionToQueueReaction)
		// t.selectedReactions_+=(BeSelectedAct)
		
		label.setIcon( tokenClassToIcon(t.tokenClass) )
		
		label.addComponentListener(new ComponentAdapter() {
			override def componentShown(e:ComponentEvent) { 
				invokeLater( new Runnable() {
					override def run() {
						label.setLocation( new java.awt.Point(
							(fieldComp.spaceLocation(t.currentSpace).getBounds2D().getCenterX() - label.getWidth()  / 2).toInt,
							(fieldComp.spaceLocation(t.currentSpace).getBounds2D().getCenterY() - label.getHeight() / 2).toInt
						));
					}
				})
			}
		})
		
		fieldComp.tokenLayer.add(label)
	}.tupled)
	
	
	
	
	
	
	def run() { while (true) { idleLock.synchronized{
		idleLock.wait()
		while (actionQueue.nonEmpty) {
			actionQueue.dequeue().apply(fieldComp, tokenToComponentMap)
		}
	}}}
}

object TokenComponentController {
	
	/* token actions */
	sealed abstract class Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		)
	}
	
	class DiedAction(val t:Token) extends Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {
			val comp = tokenToComponentMap.find{_._1 == t}.get._2
			fieldComp.tokenLayer remove comp
		}
	}
	
	class MovementAction(t:Token, s:Space) extends Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {
			val label = tokenToComponentMap.find{_._1 == t}.get._2
			
			label.setLocation( new java.awt.Point(
				(fieldComp.spaceLocation(t.currentSpace).getBounds2D().getCenterX() - label.getWidth()  / 2).toInt,
				(fieldComp.spaceLocation(t.currentSpace).getBounds2D().getCenterY() - label.getHeight() / 2).toInt
			));
		}
	}
	
	class DamageAction(defender:Token, attacker:Token, elem:Element, kind:Weaponkind) extends Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {}
	}
	
	class StatusAction(defender:Token, attacker:Token, status:Status) extends Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {}
	}
	
	
	
	
	
	/* token reactions */
	
	
	
	
}
