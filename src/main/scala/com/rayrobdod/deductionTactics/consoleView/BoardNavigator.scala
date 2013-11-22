package com.rayrobdod.deductionTactics
package consoleView

import scala.collection.mutable.Buffer
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}

/**
 * A console application that can let a user navigate a RectangularField
 * @author Raymond Dodge
 * @since a.5.1
 * @version a.5.1
 */
class BoardNavigator(tokens:ListOfTokens, val field:RectangularField) extends Runnable {
	val out:java.io.PrintStream = System.out
	val in:java.io.InputStream = System.in
	
	private var current:RectangularSpace = field.space(0,0);
	private var selected:Option[Token] = None;
	private var continue:Boolean = true;
	
	private val PressUp     = 'w';
	private val PressLeft   = 'a';
	private val PressRight  = 'd';
	private val PressDown   = 's';
	private val PressNextTurn = 'e';
	private val PressQuit   = 'q';
	private val PressSelect = 'x';
	private val PressTab    = 9;
	
	def run() {
		out print controlCursorToTop
		out print controlClearRest
		
		while (continue) {
			out print controlCursorToTop
			BoardPrinter.apply(out, tokens, field, Some(current), selected)
			out println ""
			out println controlClearRest
			
			(new SpaceInfoPrinter(tokens)).apply(current)
			out println ""
			
			val tokenOnSpace = tokens.aliveTokens.flatten.filter{_.currentSpace == current}.headOption
			tokenOnSpace.foreach{TokenPrinter}
			out.println()
			// print info about current space
			
			
			
			val char = in.read(); 
			
			if (char == PressUp)     current = current.up.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressLeft)   current = current.left.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressDown)   current = current.down.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressRight)  current = current.right.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressNextTurn) endOfTurnReactions.foreach{x => x()}
			if (char == PressQuit)   System.exit(0)
			if (char == PressSelect) { tokenOnSpace match {
				case None => selected match {
					case Some(x:CannonicalToken) => x.requestMoveTo(current)
					case _ => {}
				}
				case Some(x:CannonicalToken) => x.beSelected(true)
				case Some(other:MirrorToken) => selected match {
					case Some(mine:CannonicalToken) => mine.tryAttackDamage(other)
					case _ => {}
				}
			}}
		}
		
		System.out.println("End of game")
	}
	
	val endOfTurnReactions:Buffer[Function0[Any]] = Buffer.empty
	def addEndOfTurnReaction(f:Function0[Any]) = endOfTurnReactions += f
	
	object EndOfGameListener extends Function0[Unit] {
		def apply() {
			continue = false;
		}
	}
	
	class SelectedListener(t:Token) extends Function1[Boolean, Unit] {
		def apply(b:Boolean) {
			if (b) {
				selected = Some(t)
			} else if (selected.filter{t == _}.isDefined) {
				selected = None
			}
		}
	}
}
