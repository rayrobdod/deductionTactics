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
package com.rayrobdod.deductionTactics
package swingView

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.BodyType
import com.rayrobdod.deductionTactics.Directions.Direction

import javax.swing.Icon
import javax.imageio.ImageIO
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.swingView.{TokenComponent => BoardGameTokenComponent, _}
import com.rayrobdod.animation.{AnimationIcon, ImageFrameAnimation,
		NextFrameListener, AnimationEndedListener,
		NextFrameEvent, AnimationEndedEvent
}
import com.rayrobdod.swing.StackedIcon
import com.rayrobdod.util.BlitzAnimImage


final class TokenComponent(
	fieldComp:FieldViewer[SpaceClass],
	mainIcon:Icon
	

) extends BoardGameTokenComponent(fieldComp) {
	this.setIcon(mainIcon)
	this.setSize(mainIcon.getIconWidth, mainIcon.getIconHeight)
	
	final def beAttacked(element:Element, kind:Weaponkind) {
		val animation = TokenComponent.BeAttackedAnimation(element, kind)
		val animIcon = new AnimationIcon(animation)
		
		val stackedIcon = new StackedIcon(Seq(mainIcon, animIcon))
		this.setIcon(stackedIcon)
		
		animIcon.addRepaintOnNextFrameListener(this)
		animation.addAnimationEndedListener(new AnimationEndedListener() {
			def animationEnded(e:AnimationEndedEvent) =
					TokenComponent.this.setIcon(mainIcon)
		})
		new Thread(animation).run()
		// TODO: also do the text raising thing
	}
	
	final def beAttacked(status:Status) {
		val animation = TokenComponent.BeAttackedAnimation(status)
		val animIcon = new AnimationIcon(animation)
		
		val stackedIcon = new StackedIcon(Seq(mainIcon, animIcon))
		this.setIcon(stackedIcon)
		
		animIcon.addRepaintOnNextFrameListener(this)
		animation.addAnimationEndedListener(new AnimationEndedListener() {
			def animationEnded(e:AnimationEndedEvent) =
					TokenComponent.this.setIcon(mainIcon)
		})
		new Thread(animation).run()
	}
	
}

object TokenComponent {
	
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
