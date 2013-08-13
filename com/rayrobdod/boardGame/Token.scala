package com.rayrobdod.boardGame

import scala.actors.Actor
import scala.swing.Reactions
import scala.swing.event.Event
import com.rayrobdod.boardGame.{Space => BoardGameSpace}

/**
 * An object that can move around a game board.
 * 
 * Made to extend Actor with messages for moving around the board.
 * 
 * @author Raymond Dodge
 * @version 06 May 2011
 * @version 15 May 2011 - gave a reactions value instead of forcing extending functions
 * @version 03 Oct 2011 - removed the constructor and type, and allowed currentSpace to be null.
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 */
abstract class Token extends Actor
{
	private var _currentSpace:Space = null
	/** returns the Token's current space. */
	def currentSpace:Space = _currentSpace
	private def currentSpace_=(newValue:Space) = {_currentSpace = newValue}
	
	override def act()
	{
		loop { react
		{
			case x:Event => reactions(x)
		}}
	}
	
	/** A list of things this can do in reponse to recieving an Event in a #! */
	val reactions = new Reactions.Impl
	reactions += BaseMovementAct
	
	/**
	 * A reaction that responds to Token.Moved events by performing the #passOverAction or #landOnAction
	 * of the space refered to by the Moved object.
	 */
	object BaseMovementAct extends Reactions.Reaction
	{
		def apply(event:Event)
		{
			event match
			{
				case Moved(movedTo:Space, landed:Boolean) =>
				{
					movedTo.typeOfSpace.passOverAction(Token.this)
					if (landed) movedTo.typeOfSpace.landOnAction(Token.this)
					Token.this.currentSpace = movedTo
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
}
