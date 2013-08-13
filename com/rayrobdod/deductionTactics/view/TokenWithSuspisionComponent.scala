package com.rayrobdod.deductionTactics.view
/*
import scala.util.Random
import scala.swing.Swing
import java.awt.{Image, GridLayout, Point}
import javax.swing.{JLabel, JComponent, Icon, ImageIcon}
import javax.imageio.ImageIO
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import com.rayrobdod.swing.layouts.{MoveToLayout}
import com.rayrobdod.boardGame.{Token, Moved, Space, RectangularSpace}
import com.rayrobdod.boardGame.view.FieldComponent
import com.rayrobdod.deductionTactics.{TokenSuspicions, SuspicionsChangedEvent}
*/

/**
 * A component that shows an icon representing what a player suspects that token to be
 *
 * @author Raymond Dodge
 * @version 05 Oct 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 * @deprecated Token and TokenSuspiciouns were changed - this shouldn't even compile 
 			at this point.
 *
class TokenWithSuspisionComponent(
		token:Token,
		fieldComp:FieldComponent,
		layout:MoveToLayout,
		tokenSuspicions:TokenSuspicions) extends JLabel()
{
	token.reactions += ComponentMovementUpdateAct
	object ComponentMovementUpdateAct extends Reaction
	{
		def apply(event:Event)
		{
			event match
			{
				case Moved(movedTo:Space, landed:Boolean) =>
				{
					val location = fieldComp.spaceLabelMap(movedTo.asInstanceOf[RectangularSpace]).getLocation()
					
					layout.moveTo(TokenWithSuspisionComponent.this, location)
				}
				case _ => {}
			}
		}
		
		def isDefinedAt(event:Event):Boolean =
		{
			event match
			{
				case Moved(movedTo:Space, landed:Boolean) => true
				case _ => false
			}
		}
	}
	
//	tokenSuspicions.reactions += UpdateIconDueToSuspicionChangeAct
	object UpdateIconDueToSuspicionChangeAct extends Reaction
	{
		def apply(event:Event)
		{
			// TODO: Not dummy data
			val icon = new ImageIcon(ImageIO.read(this.getClass().getResource("/sprites/Red shirt.png")))
			
			TokenWithSuspisionComponent.this.setIcon(icon)
		}
		def isDefinedAt(event:Event):Boolean = event match
		{
			case SuspicionsChangedEvent(a) => a == tokenSuspicions
			case _ => false
		}
	}
	UpdateIconDueToSuspicionChangeAct(SuspicionsChangedEvent(tokenSuspicions))
} */
