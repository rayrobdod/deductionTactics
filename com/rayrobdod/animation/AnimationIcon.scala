package com.rayrobdod.animation

import java.awt.{Component, Graphics}

/**
 * An icon that will forward it's paintIcon calls to an animation.
 * This does not listen to the animation. Something else will have
 * to tell the component to repaint or whatever it needs to do.
 * @author Raymond Dodge
 * @version 18 May 2012
 * @version 22 May 2012 - now requires RenderableAnimation rather than Animation
 * @version 11 Jun 2012 - adding both addRepaintOnNextFrameListener methods
 */
class AnimationIcon(val animation:RenderableAnimation) extends javax.swing.Icon
{
	def paintIcon(c:Component, g:Graphics, x:Int, y:Int) =
			animation.paintCurrentFrame(c,g,x,y)
	def getIconWidth = animation.width
	def getIconHeight = animation.height
	
	/**
	 * Adds a NextFrameListener to the backing renderable animation
	 * whose only job is to call c.repaint()
	 */
	def addRepaintOnNextFrameListener(c:Component) {
		animation.addNextFrameListener(new NextFrameListener{
			def frameChanged(e:NextFrameEvent) {
				c.repaint()
			}
		})
	}
	
	def addRepaintOnNextFrameListener(c:Component, x:Int, y:Int) {
		animation.addNextFrameListener(new NextFrameListener{
			def frameChanged(e:NextFrameEvent) {
				c.repaint(x, y, getIconWidth, getIconHeight)
			}
		})
	}
}
