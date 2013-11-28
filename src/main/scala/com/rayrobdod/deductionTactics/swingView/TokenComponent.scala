/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

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
import java.awt.image.BufferedImage
import javax.swing.{JLabel, JComponent, Icon, ImageIcon}
import javax.swing.SwingUtilities.invokeLater
import com.rayrobdod.swing.layouts.MoveToLayout
import com.rayrobdod.boardGame.Space
import com.rayrobdod.boardGame.swingView.{FieldComponent,
		TokenComponent => BoardGameTokenComponent}
import com.rayrobdod.deductionTactics.{Token, ListOfTokens}
import com.rayrobdod.util.BlitzAnimImage
import javax.imageio.ImageIO
import java.awt.Color

/**
 * @author Raymond Dodge
 * @version a.5.0
 * @TODO try making a queue of animations - for cases where a lot of moves are made really quickly?
         Better done by artificial delays in the layout's reactions?
 */
class TokenComponent(token:Token, fieldComp:FieldComponent, layout:MoveToLayout, tokens:ListOfTokens)
		extends BoardGameTokenComponent(token, fieldComp, layout, 
				tokenClassToIcon(token.tokenClass)
		)
{
	private val myTeamNumber = tokens.tokens.zipWithIndex.filter{_._1.contains(token)}.head._2
	private val teamColors:Function1[Int,Color] = Seq(new Color(64,64,255), new Color(255,64,64), new Color(64,255,64), new Color(192,192,64) /*,... */)
	
	this.setBackground( teamColors(myTeamNumber) );
	
	
	
	token.updateReactions_+=(UpdateIconReaction)
	object UpdateIconReaction extends Function0[Unit] {
		override def apply():Unit = TokenComponent.this.setIcon(
				tokenClassToIcon(token.tokenClass)
		)
	}
	
	token.beDamageAttackedReactions_+=(ShowDamageReaction)
	token.beStatusAttackedReactions_+=(ShowDamageReaction)
	
	object ShowDamageReaction extends Token.DamageAttackedReactionType
			with Token.StatusAttackedReactionType with Runnable
	{
		val FRAME_LENGTH = 100 //ms
		val IMAGE_DIM = 32
		
		var effect:BlitzAnimImage = null;
		var currentEffectFrameNumber = 0;
		var prevFrameStartTime:Long = System.currentTimeMillis();
		
		/**
		 * @param elem the element of damage inflicted by the attack
		 * @param kind the weaponkind of damage inflicted by the attack
		 * @param space unused
		 */
		def apply(elem:Element, kind:Weaponkind, space:Space):Unit =
		{
			val effectFile = this.getClass().getResource(attackEffectFile(kind))
			val effectImage = ImageIO.read(effectFile)
			
			(0 until effectImage.getWidth).foreach{(x:Int) => 
				(0 until effectImage.getHeight).foreach{(y:Int) => 
					if (effectImage.getRGB(x,y) == 0xFFFFFFFF) {effectImage.setRGB(x,y,elementToColor(elem).getRGB)}
				}
			}
			
			effect = new BlitzAnimImage(effectImage, IMAGE_DIM, IMAGE_DIM, 0, 8)
			currentEffectFrameNumber = 0;
			
			this.shared()
		}
		
		/**
		 * @param status the status inflicted by the attack
		 * @param space unused
		 */
		def apply(status:Status, space:Space):Unit =
		{
			val effectFile = this.getClass().getResource("/com/rayrobdod/glyphs/status/" + status.name.toLowerCase + "-i.png")
			val effectImage = ImageIO.read(effectFile)
			
			effect = new BlitzAnimImage(effectImage, IMAGE_DIM, IMAGE_DIM, 0, 8)
			currentEffectFrameNumber = 0;
			
			this.shared()
		}
		
		def run() {
			val prevFrameEndTime = prevFrameStartTime + FRAME_LENGTH
			
			if (System.currentTimeMillis() <= prevFrameEndTime) {
				Thread.sleep(10)
				invokeLater(this)
			} else {
				this.shared()
			}
		}
		
		def shared() {
			val currentImage = new BufferedImage(IMAGE_DIM, IMAGE_DIM, BufferedImage.TYPE_INT_ARGB)
			val currentImageGraphics = currentImage.getGraphics()
			
			tokenClassToIcon(token.tokenClass).paintIcon(
				TokenComponent.this,
				currentImageGraphics,
				0,0
			)
			
			if (currentEffectFrameNumber < effect.size())
			{
				val currentEffectFrame = effect.getFrame(currentEffectFrameNumber)
				
				currentImageGraphics.drawImage(currentEffectFrame, 0, 0, IMAGE_DIM, IMAGE_DIM, null)
			}
			
			TokenComponent.this.setIcon(new ImageIcon(currentImage))
			
			prevFrameStartTime = System.currentTimeMillis();
			currentEffectFrameNumber = currentEffectFrameNumber + 1
			if (currentEffectFrameNumber <= effect.size()) {
				invokeLater(this)
			}
		}
	}
}
