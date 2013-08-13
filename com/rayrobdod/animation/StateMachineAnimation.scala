package com.rayrobdod.animation

import scala.collection.immutable.Map
import java.awt.{Component, Graphics}

/**
 * A decorator for animtion that allows switching between several other animations at will.
 * 
 * @author Raymond Dodge
 * @version 18 May 2011 - moved from {@code com.rayrobdod.boardGame} to {@code com.rayrobdod.animation}
 * @version 22 May 2012 - not extends and uses RenderableAnimation rather than Animation
 */
class StateMachineAnimation(val stateFrames:Map[Object, RenderableAnimation]) extends RenderableAnimation
{
	var currentState:RenderableAnimation = stateFrames.head._2
	var currentImage:Int = 0;
	
	def changeState(state:Object) {
		currentState = stateFrames(state);
	}
	
	def currFrameLength = currentState.currFrameLength
	def incrementFrame = currentState.incrementFrame
	def width = currentState.width
	def height = currentState.height
	
	def paintCurrentFrame(c:Component, g:Graphics, x:Int, y:Int) =
			currentState.paintCurrentFrame(c,g,x,y)
}
