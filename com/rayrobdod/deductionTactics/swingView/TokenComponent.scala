package com.rayrobdod.deductionTactics.swingView

import scala.util.Random
import scala.swing.Swing
import java.awt.{Image, GridLayout, Point}
import java.awt.image.BufferedImage
import javax.swing.{JLabel, JComponent, Icon, ImageIcon}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import com.rayrobdod.swing.layouts.MoveToLayout
import com.rayrobdod.boardGame.swingView.{FieldComponent,
		TokenComponent => BoardGameTokenComponent}
import com.rayrobdod.deductionTactics.{CannonicalToken, Player,
		AttackForDamage, AttackForStatus, Token, ListOfTokens}
import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.util.BlitzAnimImage
import javax.imageio.ImageIO
import com.rayrobdod.boardGame.BeSelected
import java.awt.Color

/**
 * @author Raymond Dodge
 * @version 29 Feb 2012 ?
 * @version 08 Apr 2012 - working on ShowDamageReaction; works to my satisfaction now.
 * @version 30 May 2012 - removing File from ImageIO call, using URL instead
 * @version 28 Oct 2012 - changing imports from com.rayrobdod.boardGame.view to com.rayrobdod.boardGame.swingView
 * @version 13 Nov 2012 - adding status-infliction animations to ShowDamageReaction
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 27 Nov 2012 - background color now corresponds to which team a token is on. 
 * @TODO try making a queue of animations - for cases where a lot of moves are made really quickly?
         Better done by artificial delays in the layout's reactions?
 */
class TokenComponent(token:Token, fieldComp:FieldComponent, layout:MoveToLayout, tokens:ListOfTokens)
		extends BoardGameTokenComponent(token, fieldComp, layout, token.tokenClass.icon)
{
	private val myTeamNumber = tokens.tokens.zipWithIndex.filter{_._1.contains(token)}.head._2
	private val teamColors:Function1[Int,Color] = Seq(new Color(64,64,255), new Color(255,64,64), new Color(64,255,64), new Color(192,192,64) /*,... */)
	
	this.setBackground( teamColors(myTeamNumber) );
	
	
	
	token.reactions += UpdateIconReaction
	object UpdateIconReaction extends Reaction
	{
		override def apply(e:Event) = {
			TokenComponent.this.setIcon(token.tokenClass.icon)
		}
		
		override def isDefinedAt(e:Event) = {e match {
			case BeSelected(_) => true 
			case _ => false
		}}
	}
	
	tokens.tokens.flatten.foreach{_.reactions += ShowDamageReaction}
	object ShowDamageReaction extends Reaction
	{
		val FRAME_LENGTH = 100 //ms
		val IMAGE_DIM = 32
		case object UpdateFrame extends Event
		
		var effect:BlitzAnimImage = null;
		var currentEffectFrameNumber = 0;
		var prevFrameStartTime:Long = System.currentTimeMillis();
		
		override def apply(e:Event):Unit = {			
			e match {
				case AttackForDamage(_, elem:Element, kind:Weaponkind, _) =>
				{
					val effectFile = this.getClass().getResource(kind.attackEffectFile)
					val effectImage = ImageIO.read(effectFile)
					
					(0 until effectImage.getWidth).foreach{(x:Int) => {
						(0 until effectImage.getHeight).foreach{(y:Int) => {
							if (effectImage.getRGB(x,y) == 0xFFFFFFFF) {effectImage.setRGB(x,y,elem.color.getRGB)}
						}}
					}}
					
					effect = new BlitzAnimImage(effectImage, IMAGE_DIM, IMAGE_DIM, 0, 8)
					currentEffectFrameNumber = 0;
				}
				case AttackForStatus(_, status:Status, _) =>
				{
					val effectFile = this.getClass().getResource("/com/rayrobdod/glyphs/status/" + status.name.toLowerCase + "-i.png")
					val effectImage = ImageIO.read(effectFile)
					
					effect = new BlitzAnimImage(effectImage, IMAGE_DIM, IMAGE_DIM, 0, 8)
					currentEffectFrameNumber = 0;
				}
				case UpdateFrame =>
				{
					val prevFrameEndTime = prevFrameStartTime + FRAME_LENGTH
					
					if (System.currentTimeMillis() <= prevFrameEndTime)
					{
						Thread.sleep(10)
						token ! UpdateFrame
						return;
					}
				}
			}
			
			val currentImage = new BufferedImage(IMAGE_DIM, IMAGE_DIM, BufferedImage.TYPE_INT_ARGB)
			val currentImageGraphics = currentImage.getGraphics()
			
			token.tokenClass.icon.paintIcon(
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
			if (currentEffectFrameNumber <= effect.size())
			{
				token ! UpdateFrame
			}
		}
		
		// x.currentFrame == y.currentFrame is kinda imprecise, but it's a superset of
		// correct implementations, not a subset.
		override def isDefinedAt(e:Event) = {e match {
			case AttackForDamage(target:Token,_,_,_) => target.currentSpace == token.currentSpace 
			case AttackForStatus(target:Token,_,_) => target.currentSpace == token.currentSpace 
			case UpdateFrame => true
			case _ => false
		}}
	}
}
