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
import scala.collection.immutable.{Seq}
import java.awt.image.BufferedImage
import java.awt.{Component, Container, Dimension, Point, LayoutManager}
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


/**
 * 
 * @since a.5.3
 */
final class TokenComponentController(fieldComp:FieldViewer,
		tokens:ListOfTokens) extends LayoutManager
{
	
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
				fieldComp.tokenLayer.revalidate()
			}
			def apply(s:Space, b:Boolean) {
				actionQueue += new MovementAction(t, s)
				fieldComp.tokenLayer.revalidate()
			}
			def apply(elem:Element, kind:Weaponkind, space:Space) {
				actionQueue += new DamageAction(t, tokenOnSpace(space), elem, kind)
				fieldComp.tokenLayer.revalidate()
			}
			def apply(status:Status, space:Space) {
				actionQueue += new StatusAction(t, tokenOnSpace(space), status)
				fieldComp.tokenLayer.revalidate()
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
		t.selectedReactions_+=(BeSelectedAct)
		
		val myTeamNumber = tokens.tokens.zipWithIndex.find{_._1.contains(t)}.get._2
		label.setBackground( teamColors(myTeamNumber) );
		
		label.setIcon( tokenClassToIcon(t.tokenClass) )
		label.setSize( label.getPreferredSize )
		
		fieldComp.tokenLayer.add(label)
	}.tupled)
	fieldComp.tokenLayer.setLayout(this)
	actionQueue += new RevalidateAction
	
	
	
	override def layoutContainer(p:Container) {
		if (p != fieldComp.tokenLayer) {
			throw new IllegalArgumentException("Unexpected container")
		} else {
			
			tokenToComponentMap.filterNot({(t:Token, label:JLabel) =>
				(actionQueue.toSeq).flatMap{_.dontLayout}.contains(t)
			}.tupled).foreach({(t:Token, label:JLabel) =>
				label.setLocation( new java.awt.Point(
					(fieldComp.spaceLocation(t.currentSpace).getBounds2D().getCenterX() - label.getWidth()  / 2).toInt,
					(fieldComp.spaceLocation(t.currentSpace).getBounds2D().getCenterY() - label.getHeight() / 2).toInt
				));
				
				label.setIcon( tokenClassToIcon(t.tokenClass) )
			}.tupled)
			
			
			if (actionQueue.nonEmpty) {
				if (actionQueue.front.isDone) {
					actionQueue.dequeue()
				} else {
					actionQueue.front.apply(fieldComp, tokenToComponentMap)
				}
				
				new Thread(new Runnable() {
					def run() {
						Thread.sleep(REFRESH_DELAY)
						fieldComp.tokenLayer.revalidate()
					}
				}, "TokenComponentControllerDelay").start()
			}
			
		}
	}
	
	
	
	override def addLayoutComponent(name:String, comp:Component) {}
	/* Not even trying */
	override def minimumLayoutSize(par:Container) = new Dimension(0,0)
	override def preferredLayoutSize(par:Container) = new Dimension(0,0)
	override def removeLayoutComponent(comp:Component) {}
}

object TokenComponentController {
	
	/* actions */
	sealed abstract class Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		)
		
		def dontLayout:Seq[Token] = Nil
		def isDone:Boolean
	}
	
	class DiedAction(val t:Token) extends Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {
			val comp = tokenToComponentMap.find{_._1 == t}.get._2
			fieldComp.tokenLayer remove comp
			fieldComp.tokenLayer.revalidate()
			
			_isDone = true;
		}
		var _isDone:Boolean = false
		def isDone:Boolean = _isDone
	}
	
	class MovementAction(t:Token, s:Space) extends Action {
		private var startTime:Option[Long] = None
		private var startPoint:Option[Point] = None
		private var endPoint:Option[Point] = None
		private val speed = BoardGamePanel.movementSpeed
		
		private def deltaX:Option[Int] = (endPoint zip startPoint).map{(a) => a._1.x - a._2.x}.headOption
		private def deltaY:Option[Int] = (endPoint zip startPoint).map{(a) => a._1.y - a._2.y}.headOption
		
		private def soFarTime = System.currentTimeMillis - startTime.getOrElse(System.currentTimeMillis)
		private def totalTime = (deltaX zip deltaY).map{(a) =>
			math.sqrt((a._1 * a._1) + (a._2 * a._2)) * MOVE_TIME_MULTIPLER / speed
		}.headOption
		
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {
			val label = tokenToComponentMap.find{_._1 == t}.get._2
			if (startTime == None) {
				startTime = Some(System.currentTimeMillis())
				startPoint = Some(new Point(label.getX(), label.getY()))
			}
			
			val endX = (fieldComp.spaceLocation(s).getBounds2D().getCenterX() - label.getWidth()  / 2).toInt
			val endY = (fieldComp.spaceLocation(s).getBounds2D().getCenterY() - label.getHeight() / 2).toInt
			
			endPoint = Some(new Point(endX, endY))
			
			label.setLocation( new java.awt.Point(
				(startPoint.get.x + (deltaX.get * soFarTime / totalTime.get)).intValue,
				(startPoint.get.y + (deltaY.get * soFarTime / totalTime.get)).intValue
			));
			
			fieldComp.tokenLayer.revalidate()
		}
		
		override val dontLayout = Seq(t)
		
		override def isDone:Boolean = {
			totalTime.map{soFarTime > _}.getOrElse{false}
		}
	}
	
	class DamageAction(defender:Token, attacker:Token, elem:Element, kind:Weaponkind) extends Action {
		private var frameEnd:Option[Long] = None
		private val effect:BlitzAnimImage = {
			val effectFile = this.getClass().getResource(attackEffectFile(kind))
			val effectImage = ImageIO.read(effectFile)
			
			(0 until effectImage.getWidth).foreach{(x:Int) => 
				(0 until effectImage.getHeight).foreach{(y:Int) => 
					if (effectImage.getRGB(x,y) == 0xFFFFFFFF) {effectImage.setRGB(x,y,elementToColor(elem).getRGB)}
				}
			}
			
			new BlitzAnimImage(effectImage, EFFECT_IMAGE_DIM, EFFECT_IMAGE_DIM, 0, 8)
		}
		private var currentEffectFrameNumber = -1
		
		
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {
			val label = tokenToComponentMap.find{_._1 == defender}.get._2
			if (frameEnd.map{_ < System.currentTimeMillis()}.getOrElse(true)) {
				frameEnd = Some(System.currentTimeMillis() + EFFECT_FRAME_LENGTH)
				currentEffectFrameNumber = currentEffectFrameNumber + 1
			}
			
			val tokenIcon = tokenClassToIcon(defender.tokenClass)
			val currentImage = new BufferedImage(tokenIcon.getIconWidth(), tokenIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB)
			val currentImageGraphics = currentImage.getGraphics()
			
			tokenIcon.paintIcon(
				label, currentImageGraphics, 0, 0
			)
			
			if (currentEffectFrameNumber < effect.size()) {
				val currentEffectFrame = effect.getFrame(currentEffectFrameNumber)
				currentImageGraphics.drawImage(currentEffectFrame,
						(tokenIcon.getIconWidth()  - EFFECT_IMAGE_DIM) / 2,
						(tokenIcon.getIconHeight() - EFFECT_IMAGE_DIM) / 2,
						EFFECT_IMAGE_DIM, EFFECT_IMAGE_DIM, null)
			}
			
			label.setIcon(new ImageIcon(currentImage))
		}
		def isDone:Boolean = currentEffectFrameNumber >= EFFECT_FRAME_COUNT
	}
	
	class StatusAction(defender:Token, attacker:Token, status:Status) extends Action {
		private var frameEnd:Option[Long] = None
		private val effect:BlitzAnimImage = {
			val effectFile = this.getClass().getResource("/com/rayrobdod/glyphs/status/" + status.name.toLowerCase + "-i.png")
			val effectImage = ImageIO.read(effectFile)
			
			new BlitzAnimImage(effectImage, EFFECT_IMAGE_DIM, EFFECT_IMAGE_DIM, 0, EFFECT_FRAME_COUNT)
		}
		private var currentEffectFrameNumber = -1
		
		
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {
			val label = tokenToComponentMap.find{_._1 == defender}.get._2
			if (frameEnd.map{_ < System.currentTimeMillis()}.getOrElse(true)) {
				frameEnd = Some(System.currentTimeMillis() + EFFECT_FRAME_LENGTH)
				currentEffectFrameNumber = currentEffectFrameNumber + 1
			}
			
			val tokenIcon = tokenClassToIcon(defender.tokenClass)
			val currentImage = new BufferedImage(tokenIcon.getIconWidth(), tokenIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB)
			val currentImageGraphics = currentImage.getGraphics()
			
			tokenIcon.paintIcon(
				label, currentImageGraphics, 0, 0
			)
			
			if (currentEffectFrameNumber < effect.size()) {
				val currentEffectFrame = effect.getFrame(currentEffectFrameNumber)
				currentImageGraphics.drawImage(currentEffectFrame,
						(tokenIcon.getIconWidth()  - EFFECT_IMAGE_DIM) / 2,
						(tokenIcon.getIconHeight() - EFFECT_IMAGE_DIM) / 2,
						EFFECT_IMAGE_DIM, EFFECT_IMAGE_DIM, null)
			}
			
			label.setIcon(new ImageIcon(currentImage))
		}
		def isDone:Boolean = currentEffectFrameNumber >= EFFECT_FRAME_COUNT
	}
	
	class RevalidateAction extends Action {
		def apply(
			fieldComp:FieldViewer,
			tokenToComponentMap:Seq[(Token, JLabel)]
		) = {
			fieldComp.tokenLayer.revalidate()
			_isDone = true;
		}
		var _isDone:Boolean = false
		def isDone:Boolean = _isDone
	}
	
	
	
	/* constants */
	val REFRESH_DELAY = 12
	val MOVE_TIME_MULTIPLER = 120
	val EFFECT_FRAME_LENGTH = 36 
	val EFFECT_IMAGE_DIM = 32
	val EFFECT_FRAME_COUNT = 8
}
