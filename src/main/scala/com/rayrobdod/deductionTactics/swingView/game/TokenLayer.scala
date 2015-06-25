/*
	Deduction Tactics
	Copyright (C) 2012-2014  Raymond Dodge

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
package com.rayrobdod.deductionTactics
package swingView
package game

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.BodyType
import com.rayrobdod.deductionTactics.Directions.Direction

import java.awt.Graphics
import javax.swing.{JComponent, Icon}
import javax.imageio.ImageIO
import scala.collection.immutable.Seq
import com.rayrobdod.util.BlitzAnimImage
import com.rayrobdod.animation.{AnimationIcon, ImageFrameAnimation,
		NextFrameListener, AnimationEndedListener,
		NextFrameEvent, AnimationEndedEvent
}
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.swingView._
import com.rayrobdod.deductionTactics.ai.TokenClassSuspision


/**
 * @version a.6.0
 */
final class TokenLayer(spaces:RectangularField[SpaceClass], tiles:RectangularTilemapComponent) extends JComponent {
	var tokens:ListOfTokens = new ListOfTokens(Nil)
	var suspisions:Map[(Int, Int), TokenClassSuspision] = Map.empty
	
	override def paintComponent(g:Graphics):Unit = {
		// TODO: don't paint dead tokens
		val t2:Map[(Int, Int), Option[TokenClass]] = {
			tokens.tokens.zipWithIndex.flatMap{x =>
				x._1.zipWithIndex.map{y => (( ((x._2, y._2)), y._1.tokenClass ))}
			}.toMap
		}
		
		t2.keySet.foreach{x =>
			val icon:Icon = t2.get(x).flatMap{y => y.map{tokenClassToIcon _}}.getOrElse{
				val y = suspisions.getOrElse(x, new TokenClassSuspision)
				generateGenericIcon(y.atkElement, y.atkWeapon)
			}
			val space = tokens.tokens(x).currentSpace
			val spaceIndex = spaces.find(_._2 == space).get._1
			val bounds = tiles.spaceBounds(spaceIndex).getBounds
			
			icon.paintIcon(this, g, bounds.x, bounds.y)
		}
	}
}

object TokenIcon {
	
	def BeAttackedAnimation(elem:Element, kind:Weaponkind):ImageFrameAnimation = {
		val effect:BlitzAnimImage = {
			val effectFile = this.getClass().getResource(attackEffectFile(kind))
			val effectImage = ImageIO.read(effectFile)
			
			(0 until effectImage.getWidth).foreach{(x:Int) => 
				(0 until effectImage.getHeight).foreach{(y:Int) => 
					if (effectImage.getRGB(x,y) == 0xFFFFFFFF) {effectImage.setRGB(x,y,elementToColor(elem).getRGB)}
				}
			}
			
			new BlitzAnimImage(effectImage, EFFECT_IMAGE_DIM, EFFECT_IMAGE_DIM, 0, 8)
		}
		
		new ImageFrameAnimation(effect, EFFECT_FRAME_LENGTH, false)
	}
	
	def BeAttackedAnimation(status:Status):ImageFrameAnimation = {
		val effect:BlitzAnimImage = {
			val effectFile = this.getClass().getResource("/com/rayrobdod/glyphs/status/" + status.name.toLowerCase + "-i.png")
			val effectImage = ImageIO.read(effectFile)
			
			new BlitzAnimImage(effectImage, EFFECT_IMAGE_DIM, EFFECT_IMAGE_DIM, 0, EFFECT_FRAME_COUNT)
		}
		
		new ImageFrameAnimation(effect, EFFECT_FRAME_LENGTH, false)
	}
	
	
	
	
	/* constants */
	val REFRESH_DELAY = 12
	val MOVE_TIME_MULTIPLER = 120
	val EFFECT_FRAME_LENGTH = 36 
	val EFFECT_IMAGE_DIM = 32
	val EFFECT_FRAME_COUNT = 8
}
