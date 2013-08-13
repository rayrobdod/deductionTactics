package com.rayrobdod.animation

import scala.collection.immutable.{Seq, Nil}
import java.lang.System.{currentTimeMillis => currentTime}

/**
 * A controler for an animation. Subclasses determine the model.
 *
 * 
 * @author Raymond Dodge
 * @version 16 June 2011 - debugging!
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 * @version 17 May 2012 - gutting, removing state, and replacing with a listener-based class 
 * @version 18 May 2012 - moved from {@code com.rayrobdod.boardGame} to {@code com.rayrobdod.animation}
 * @version 22 May 2012 - removing paintCurrentFrame, width and height. see [[com.rayrobdod.animation.RenderableAnimation]]
 */
abstract class Animation extends Runnable
{
	/** the listeners to notify when the frame changes */
	private var frameChangeListeners:Seq[NextFrameListener] = Nil
	/** the listeners to notify when the animation ends */
	private var endAnimationListeners:Seq[AnimationEndedListener] = Nil
	/** the time the previous frame change happened */
	private var lastFrameChange:Long = _
	
	var paused:Boolean = false
	def running:Boolean = _running
	protected def running_=(x:Boolean) = {_running = x}
	private var _running = false
	
	/** The length of time that the current frame should exist for */
	def currFrameLength:Int
	/** Advances to the next frame. */
	def incrementFrame():Any
	
	def addNextFrameListener(l:NextFrameListener) {
		frameChangeListeners = l +: frameChangeListeners
	}
	
	protected def notifyListenersOfFrameChange(e:NextFrameEvent) =
	{
		frameChangeListeners.foreach{_.frameChanged(e)}
	}
	
	protected def notifyListenersOfEndAnimation(e:AnimationEndedEvent) =
	{
		endAnimationListeners.foreach{_.animationEnded(e)}
	}
	
	def run():Unit = {
		running = true;
		
		while (running)
		{
			lastFrameChange = currentTime()
			
			while (!paused) {
				while (lastFrameChange + currFrameLength - currentTime() > 0)
				{
					Thread.sleep(lastFrameChange + currFrameLength - currentTime())
				}
				
				lastFrameChange += currFrameLength
				incrementFrame()
				notifyListenersOfFrameChange(NextFrameEvent(Animation.this))
			}
		}
		
		notifyListenersOfEndAnimation(AnimationEndedEvent(Animation.this))
	}
}

trait NextFrameListener extends java.util.EventListener
{
	def frameChanged(e:NextFrameEvent)
}

case class NextFrameEvent(src:Animation)
		extends java.util.EventObject(src)
//		with scala.swing.Event
{
	override def getSource:Animation = src
}

trait AnimationEndedListener extends java.util.EventListener
{
	def animationEnded(e:AnimationEndedEvent)
}

case class AnimationEndedEvent(src:Animation)
		extends java.util.EventObject(src)
//		with scala.swing.Event
{
	override def getSource:Animation = src
}