package com.rayrobdod.boardGame

import scala.swing.event.Event

/**
 * A message that indicates that the token is moving around the board
 * @param movedTo the space this is moving to
 * @param landed true iff the space is the final space the token will move to
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 */
case class Moved(val movedTo:Space, val landed:Boolean) extends Event
/** An event indicating that a turn this piece cares about has started 
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}*/
case object StartOfTurn extends Event
/** An event indicating that a turn this piece is involed in has ended
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame} */
case object EndOfTurn extends Event
/** Indicates whether a token should act as if it were selected
 * @version 20 Jan 2011 */
case class BeSelected(val b:Boolean) extends Event
