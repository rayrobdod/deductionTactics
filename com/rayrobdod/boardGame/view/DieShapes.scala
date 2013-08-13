package com.rayrobdod.math.geom

import java.awt.geom.Path2D
import java.awt.Shape

/**
 * A factory for a Dodecahedron-style shape
 * @author Raymond Dodge
 * @version 27 May 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.math.geom} to {@code com.rayrobdod.math.geom}
 */
object Dodecahedron
{
	def apply():Shape = apply(1)
	
	def apply(size:Double):Shape =
	{
		val s = new Path2D.Double
		
		s.moveTo(0. * size, .472 * size)
		s.lineTo(.180 * size, .474 * size)
		
		s.moveTo(.342 * size, .946 * size)
		s.lineTo(.392 * size, .767 * size)
		
		s.moveTo(.899 * size, .767 * size)
		s.lineTo(.736 * size, .656 * size)
		
		s.moveTo(.736 * size, .294 * size)
		s.lineTo(.900 * size, .182 * size)
		
		s.moveTo(.345 * size, 0. * size)
		s.lineTo(.393 * size, .182 * size)
		
		s.moveTo(.393 * size, .182 * size)
		s.lineTo(.180 * size, .474 * size)
		s.lineTo(.392 * size, .767 * size)
		s.lineTo(.736 * size, .656 * size)
		s.lineTo(.736 * size, .294 * size)
		s.lineTo(.393 * size, .182 * size)
		
		s.moveTo(0. * size, .472 * size)
		s.lineTo(.100 * size, .764 * size)
		s.lineTo(.343 * size, .946 * size)
		s.lineTo(.656 * size, .946 * size)
		s.lineTo(.900 * size, .767 * size)
		s.lineTo(1. * size, .474 * size)
		s.lineTo(.900 * size, .182 * size)
		s.lineTo(.657 * size, 0. * size)
		s.lineTo(.345 * size, 0. * size)
		s.lineTo(.102 * size, .180 * size)
		s.lineTo(0. * size, .472 * size)
		
		s
	}
}

/**
 * A factory for a Isoshedron-style shape
 * @author Raymond Dodge
 * @version 20 May 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.math.geom} to {@code com.rayrobdod.math.geom}
 */
object Isoshedron
{
	def apply():Shape = apply(1)
	
	def apply(size:Double):Shape =
	{
		val s = new Path2D.Double
		
		s.moveTo(.25 * size, .069 * size)
		s.lineTo(.34 * size, .23 * size)
		
		s.moveTo(.25 * size, .935 * size)
		s.lineTo(.34 * size, .76 * size)
		
		s.moveTo(1.0 * size, .50 * size)
		s.lineTo(.75 * size, .069 * size)
		s.lineTo(.25 * size, .069 * size)
		s.lineTo(0 * size, .50 * size)
		s.lineTo(.25 * size, .935 * size)
		s.lineTo(.75 * size, .935 * size)
		s.lineTo(1.0 * size, .50 * size)
		s.lineTo(.80 * size, .50 * size)
		s.lineTo(.75 * size, .069 * size)
		s.lineTo(.34 * size, .23 * size)
		s.lineTo(0 * size, .50 * size)
		s.lineTo(.34 * size, .76 * size)
		s.lineTo(.75 * size, .935 * size)
		s.lineTo(.80 * size, .50 * size)
		s.lineTo(.34 * size, .23 * size)
		s.lineTo(.34 * size, .76 * size)
		s.lineTo(.80 * size, .50 * size)
		
		s
	}
}
