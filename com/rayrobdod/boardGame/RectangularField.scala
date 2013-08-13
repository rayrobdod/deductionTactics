package com.rayrobdod.boardGame

import scala.collection.immutable.Seq
import java.util.concurrent.{Future => JavaFuture, TimeUnit, TimeoutException}
import scala.parallel.{Future => ScalaPFuture}

/**
 * A group of spaces such that they form a rectangular board made of
 * [[com.rayrobdod.boardGame.RectangularSpace]]s, such that they follow
 * ecludian geometery.
 * 
 * 
 * @author Raymond Dodge
 * @version 30 Jul 2011
 * @version 02 Aug 2011 - renamed from RectangularRegionMap to RectangularField
 * @version 03 Aug 2011 - <code>&&</code> is the shortcut version of <code>&</code>... Fixed {@link #containsIndexies}
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 * @version 2012 Aug 25 - switching x and y in #space and #containsIndexies
 * @version 2012 Oct 28 - changing the spaces field to be protected.
 * @see [[com.rayrobdod.boardGame.RectangularSpace]]
 */
abstract class RectangularField
{
	/**  y is outer layer - x is inner layer */
	def spaces:Seq[Seq[RectangularSpace]]
	
	/** retrives a space from the spaces array. */
	final def space(x:Int, y:Int) = spaces(y)(x)
	final def containsIndexies(x:Int, y:Int) = spaces.isDefinedAt(y) && spaces(y).isDefinedAt(x)
	
	/** 
	 * Creates a future (both {@link java.util.concurrent.Future} and {@link scala.parallel.Future}) that, when called, will
	 * return the same value as {@code space(x,y)}
	 */
	final protected def spaceFuture(x:Int, y:Int) =
	{
		final class RectangularFieldSpaceFuture(x:Int, y:Int) extends JavaFuture[Option[RectangularSpace]]
				with ScalaPFuture[Option[RectangularSpace]]
		{
			override def get = apply
			override def apply = {
				while (!isDone) {Thread.sleep(100L)}
				
				if (RectangularField.this.containsIndexies(x,y)) Some(RectangularField.this.space(x,y)) else None
			}
			
			override def get(timeout:Long, timeUnit:TimeUnit) = {
				Thread.sleep(timeUnit.toMillis(timeout))
				
				if (isDone) get() else {
					throw new TimeoutException("RectangularFieldSpaceFuture.get(" + timeout + ":long, " + timeUnit +
							":java.util.concurrent.TimeUnit)")
				}
			}
			
			// in theory, this should never be called in a state when isDone is false.
			override def isDone:Boolean = {spaces != null}
			
			override def cancel(ignored:Boolean):Boolean = false
			override def isCancelled:Boolean = false
			
			override def toString = "RectangularFieldSpaceFuture [RectangularField.this: " + RectangularField.this + "x: " + x + ", y: " + y + "]" 
		}
		
		new RectangularFieldSpaceFuture(x,y)
	}
}

/**
 * A Constructorish for Rectangular Fields. Ish because none of them are #apply().
 */
object RectangularField
{
	/**
	 * @version 04 Oct 2011
	 */
	def applySCC(classConstructors:Seq[Seq[SpaceClassConstructor]]):RectangularField =
	{
		this.applySC(classConstructors.map{_.map{_.apply()}})
	}
	
	/**
	 * @version 29 Sept 2011
	 * @version 25 Aug 2012 - correction: switching x and y in rectangular space
	 * @version 25 Aug 2012 - changing anonfuns to have two parameters and use the tupled method
	 * @version 28 Oct 2012 - switching i and j in for loops, due to discrepency between spaces and spaceFuture.
	 */
	def applySC(classes:Seq[Seq[SpaceClass]]):RectangularField = {
		new RectangularField {
			
			val spaces = classes.zipWithIndex.map({(classSeq:Seq[SpaceClass], j:Int) => 
				classSeq.zipWithIndex.map({(clazz:SpaceClass, i:Int) => 
					new RectangularSpace(
							typeOfSpace = clazz,
							leftFuture  = spaceFuture(i-1,j),
							upFuture    = spaceFuture(i,j-1),
							rightFuture = spaceFuture(i+1,j),
							downFuture  = spaceFuture(i,j+1)
					)
				}.tupled)
			}.tupled)
		}
	}
}
