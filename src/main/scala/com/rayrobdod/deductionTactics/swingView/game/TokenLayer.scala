/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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

import java.awt.Graphics
import javax.swing.{JComponent, Icon}
import javax.imageio.ImageIO
import scala.collection.immutable.Map
import com.rayrobdod.util.BlitzAnimImage
import com.rayrobdod.animation.ImageFrameAnimation
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.RectangularIndex
import com.rayrobdod.boardGame.view.{IconLocation, RectangularDimension}
import com.rayrobdod.boardGame.view._
import com.rayrobdod.boardGame.view.Swing._
import com.rayrobdod.deductionTactics.ai.TokenClassSuspicion
import com.rayrobdod.deductionTactics.swingView.RectangularTilesheet


/**
 * @since a.6.0
 */
final class TokenLayer(spaces:RectangularField[SpaceClass])(implicit locations:IconLocation[RectangularIndex, RectangularDimension]) extends JComponent {
	private[this] var _tokens:ListOfTokens = new ListOfTokens(Nil)
	var suspicions:Map[(Int, Int), TokenClassSuspicion] = Map.empty
	
	def tokens:ListOfTokens = _tokens
	def tokens_=(newTokens:ListOfTokens):Unit = {
		_tokens = newTokens
		this.repaint()
	}
	
	override def paintComponent(g:Graphics):Unit = {
		// TODO: don't paint dead tokens
		val t2:Map[(Int, Int), Option[TokenClass]] = {
			tokens.tokens.zipWithIndex.flatMap{x =>
				x._1.zipWithIndex.map{y => (( ((x._2, y._2)), y._1.tokenClass ))}
			}.toMap.filterKeys{(index:(Int, Int)) =>
				ListOfTokens.aliveFilter(tokens.tokens(index))
			}
		}
		
		t2.keySet.foreach{x =>
			val icon:Icon = t2.get(x).flatMap{y => y.map{tokenClassToIcon _}}.getOrElse{
				val y = suspicions.getOrElse(x, new TokenClassSuspicion)
				generateGenericIcon(y.atkElement, y.atkWeapon)
			}
			val space = tokens.tokens(x).currentSpace
			val spaceIndex = spaces.indexOfSpace(space).get
			val bounds = locations.bounds(spaceIndex, new RectangularDimension(32, 32)).getBounds
			
			icon.paintIcon(this, g, bounds.x, bounds.y)
		}
	}
}

object TokenIcon {
	
	/** @since a.6.0 */
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
	
	/** @since a.6.0 */
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
