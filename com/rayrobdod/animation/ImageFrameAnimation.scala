package com.rayrobdod.animation

import scala.collection.immutable.Seq
import java.awt.Image
import java.awt.{Component, Graphics}
import com.rayrobdod.util.BlitzAnimImage

/**
 * @author Raymond Dodge
 * @version 17 May 2012
 * @version 22 May 2012 - not extends RenderableAnimation rather than Animation
 */
class ImageFrameAnimation(
			frames:Seq[_ <: Image],
			frameLengths:Seq[Int],
			repeat:Boolean = false
) extends RenderableAnimation
{
	def this(frames:Seq[_ <: Image], frameLength:Int, repeat:Boolean)
	{
		this(frames, Seq.fill(frames.length){frameLength}, repeat);
	}
	def this(frames:BlitzAnimImage, frameLengths:Seq[Int], repeat:Boolean)
	{
		this(Seq.empty ++ frames.getImages, frameLengths, repeat);
	}
	def this(frames:BlitzAnimImage, frameLength:Int, repeat:Boolean)
	{
		this(Seq.empty ++ frames.getImages, frameLength, repeat);
	}
	
	private var currentFrame = 0;
	
	frames.forall{_.getWidth(null) == frames(0).getWidth(null)}
	frames.forall{_.getHeight(null) == frames(0).getHeight(null)}
	
	val width = frames(0).getWidth(null);
	val height = frames(0).getHeight(null);
	def paintCurrentFrame(c:Component, g:Graphics, x:Int, y:Int) = {
		g.drawImage(frames(currentFrame), x, y, null)
	}
	def currFrameLength = frameLengths(currentFrame)
	
	def incrementFrame() {
		currentFrame = currentFrame + 1
		if (repeat) {
			currentFrame = currentFrame % frames.length
		} else {
			running = currentFrame <= frames.length
		}
	}
	
}
