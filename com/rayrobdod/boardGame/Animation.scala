package com.rayrobdod.boardGame

import scala.actors.Future


/**
 * indicates that the class can show an animation that will stop.
 * Created so DieComponent would not be so cluttered.
 * 
 * @author Raymond Dodge
 * @version 16 June 2011 - debugging!
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 * @deprecated replaced by the observer/obserable based com.rayrobdod.animation.Animation
 */
trait Animation
{
	def totalFrames:Int
	private var _currentFrame:Int = totalFrames
	protected def currentFrame:Int = _currentFrame
	protected def incrementFrame() = 
	{
		_currentFrame = _currentFrame + 1
		if (_currentFrame >= totalFrames)
		{
			animatingFinishFuture.set()
		}
	}
	protected def restart() = {_currentFrame = 0; newAnimatingFinishFuture()}
	protected def stop() = {_currentFrame = totalFrames}
	
	private var _animatingFinishFuture = new AnimatingFinishFuture
	def animatingFinishFuture = _animatingFinishFuture
	private def newAnimatingFinishFuture() = {_animatingFinishFuture = new AnimatingFinishFuture}
	
	def isAnimating = totalFrames < currentFrame
	
	/** A future that blocks the apply method unit a private method is called by its parent */
	class AnimatingFinishFuture extends Future[Unit]
	{
		/** No idea what this is supposed to be */
		override def respond(k: (Unit) => Unit): Unit = {throw new UnsupportedOperationException}
		
		private var _isSet = false
		override def isSet = _isSet
		private[Animation] def set() =
		{
			synchronized
			{
				_isSet = true
				notifyAll()
			}
		}
		
		override def apply():Unit =
		{
			synchronized
			{
				if (!_isSet) wait()
			}
		}
		
		/** I know what this is supposed to do, but it seems too hard */
		override def inputChannel() = {throw new UnsupportedOperationException} //TODO
	}
}
