package com.rayrobdod.boardGame.view

import scala.util.Random
import scala.swing.Swing
import java.awt.{Image, GridLayout, Point}
import javax.swing.{JLabel, JComponent, Icon}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import com.rayrobdod.swing.layouts.{MoveToLayout}
import com.rayrobdod.boardGame.{Token, Moved, Space, RectangularSpace, BeSelected}

/**
 * A component that shows a label and moves around a FieldComponent as a token moves around a field.
 * 
 * @author Raymond Dodge
 * @version 04 Aug 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
class TokenComponent(token:Token, fieldComp:FieldComponent, layout:MoveToLayout, icon:Icon) extends JLabel(icon)
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
					
					layout.moveTo(TokenComponent.this, location)
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
	
	token.reactions += BeSelectedAct
	object BeSelectedAct extends Reaction
	{
		def apply(e:Event) = {e match {
			case BeSelected(b:Boolean) => { 
				TokenComponent.this.setOpaque(b)
				TokenComponent.this.repaint()
			}
			case _ => {}
		}}
		
		def isDefinedAt(e:Event) = {e match {
			case BeSelected(_) => true
			case _ => false
		}}
	}
	
	this.setBackground(java.awt.Color.green)
}
