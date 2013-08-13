package com.rayrobdod.deductionTactics

/**
 * An enumeration for the Bodytypes
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics
			to com.rayrobdod.deductionTactics
 * @version 29 Jul 2012 - adding Avian  as a value
 * @version 2013 Jun 14 - adding Gerbil as a value
 */
object BodyTypes extends Enumeration {
	val Humanoid = Value("Human")
	val Avian    = Value("Avian")
	val Gerbil   = Value("Gerbillinae")
}
