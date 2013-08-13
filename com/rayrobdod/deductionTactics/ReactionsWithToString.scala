package com.rayrobdod.tmp

import scala.swing.Reactions
import Reactions.Reaction
import scala.swing.event.Event
import scala.collection.mutable.{Buffer, ListBuffer}

class ReactionsWithToString extends Reactions {
	private val parts: Buffer[Reaction] = new ListBuffer[Reaction]
	def isDefinedAt(e: Event) = parts.exists(_ isDefinedAt e)
	def += (r: Reaction): this.type = { parts += r; this }
	def -= (r: Reaction): this.type = { parts -= r; this }
	def apply(e: Event) {
		for (p <- parts) if (p isDefinedAt e) p(e)
	}

	override def toString() = "Reactions[ " + parts.toString
}
