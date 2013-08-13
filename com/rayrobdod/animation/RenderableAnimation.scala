package com.rayrobdod.animation

import java.awt.{Component, Graphics}

/**
 * An animation that can be printed to a graphics
 * @author Raymond Dodge
 * @version 22 May 2012
 */
abstract class RenderableAnimation extends Animation
{
	/**
	 * Paints the current frame of animation to g at (x,y)
	 * basically ripped from javax.swing.Icon.
	 */
	def paintCurrentFrame(c:Component, g:Graphics, x:Int, y:Int)
	def width:Int
	def height:Int
}
