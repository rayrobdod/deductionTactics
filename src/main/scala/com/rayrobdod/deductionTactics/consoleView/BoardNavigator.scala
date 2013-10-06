package com.rayrobdod.deductionTactics
package consoleView

import scala.collection.mutable.Buffer
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}

class BoardNavigator(tokens:ListOfTokens, val field:RectangularField) extends Runnable {
	val out:java.io.PrintStream = System.out
	val in:java.io.InputStream = System.in
	
	private var current:RectangularSpace = field.space(0,0);
	private var selected:Option[Token] = None;
	private var continue:Boolean = true;
	
	val PressUp    = 'w';
	val PressLeft  = 'a';
	val PressRight = 'd';
	val PressDown  = 's';
	val PressEsc   = 'e';
	val PressQuit  = 'q';
	val PressEnter = 'x';
	
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
			
			val tokenOnSpace = tokens.tokens.flatten.filter{_.currentSpace == current}.headOption
			tokenOnSpace.foreach{a => TokenPrinter(a); out.println()}
			// print info about current space
			
			
			
			val char = in.read(); 
			
			if (char == PressUp)    current = current.up.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressLeft)  current = current.left.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressDown)  current = current.down.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressRight) current = current.right.getOrElse(current).asInstanceOf[RectangularSpace]
			if (char == PressEsc)   endOfTurnReactions.foreach{x => x()}
			if (char == PressEnter) tokenOnSpace.foreach{_.beSelected(true)}
			if (char == PressQuit)  System.exit(0)
			
		}
		
		System.out.println("End of turn")
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
			if (b == true) {
				selected = Some(t)
			} else if (t == selected) {
				selected = None
			}
		}
	}
}
