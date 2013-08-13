package com.rayrobdod.swing.layouts

import java.awt.{Container, Component, Point, LayoutManager2, Dimension, EventQueue}
import scala.collection.mutable.{ListMap, Queue, Set => MSet}
import scala.collection.immutable.{Map, Set}
import java.util.concurrent.Executors.{newSingleThreadExecutor => SingleThreadExecutor}
import com.rayrobdod.animation.Animation

/**
 * A layout manager that allows manual specification
 * of where a component is. Probably better than no layout
 * manager for some unknown reason.
 * 
 * Only the moveTo method is new.
 * 
 * @author Raymond Dodge
 * @version 04 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.swing.layouts to com.rayrobdod.swing.layouts
 */
trait MoveToLayout extends LayoutManager2
{
	def layoutContainer(parent:Container):Unit
	def moveTo(comp:Component, point:Point):Unit
	
	def addLayoutComponent(name:String, comp:Component) = addLayoutComponent(comp,name)
	def addLayoutComponent(comp:Component, constraints:Object) =
	{
		comp.setLocation(0,0)
		comp.setSize(comp.getPreferredSize)
	}
	def removeLayoutComponent(comp:Component) {}
	def invalidateLayout(target:Container) {}
	
	private val getDimWidth = {(d:Dimension) => d.width} 
	private val getDimHeight = {(d:Dimension) => d.height} 
	
	def minimumLayoutSize(parent:Container) =
	{
		val layoutSizes = parent.getComponents.map{_.getMinimumSize}
		val minX = layoutSizes.map{getDimWidth}.min
		val minY = layoutSizes.map{getDimHeight}.min
		
		new Dimension(minX, minY)
	}
	def preferredLayoutSize(parent:Container) =
	{
		val layoutSizes = parent.getComponents.map{_.getPreferredSize}
		val minX = layoutSizes.map{getDimWidth}.max
		val minY = layoutSizes.map{getDimHeight}.max
		
		new Dimension(minX, minY)
	}
	def maximumLayoutSize(parent:Container) =
	{
		val layoutSizes = parent.getComponents.map{_.getMaximumSize}
		val minX = layoutSizes.map{getDimWidth}.max
		val minY = layoutSizes.map{getDimHeight}.max
		
		new Dimension(minX, minY)
	}
	
	def getLayoutAlignmentX(target:Container) = {.5f}
	def getLayoutAlignmentY(target:Container) = {.5f}
}

/**
 * A MoveToLayout that instantly moves a component to its
 * desired location
 * 
 * @author Raymond Dodge
 * @version 04 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.swing.layouts to com.rayrobdod.swing.layouts
 */
class MoveToInstantLayout extends MoveToLayout
{
	def layoutContainer(parent:Container) {}
	def moveTo(comp:Component, point:Point) =
	{
		comp.setLocation(point)
	}
}

/**
 * A MoveToLayout that will slide a component to
 * its desired location gradually
 * 
 * @author Raymond Dodge
 * @version 04 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.swing.layouts to com.rayrobdod.swing.layouts
 * @version 13 Feb 2012 - now uses an Executor instead of creating a new thread for each refresh
 * @version 13 Feb 2012 - allowed anonfuns to take advantage of Function2.tupled, while I'm at it.
 * @version 30 May 2012 - optimized to always remove used paths from the layout, such that this will
			stop repainting the parent component at some point.
 * @version 30 May 2012 - now, first move of a component will be instant
 */
class MoveToGradualLayout(val speed:Float) extends MoveToLayout
{
	import MoveToGradualLayout.Path
	import MoveToGradualLayout.refreshRate
	
	var usedComps:MSet[Component] = MSet.empty
	var compPathMap:Map[Component, Path] = Map.empty
	
	def moveTo(comp:Component, point:Point) =
	{
		if (!(usedComps contains comp))
		{
			usedComps += comp
			comp.setLocation(point)
		}
		else
		{
			val path = new Path(comp.getLocation,
					System.currentTimeMillis(), point,
					(System.currentTimeMillis() +
						(speed * comp.getLocation.distance(point)).toLong))
					  
			MoveToGradualLayout.logger.finer(path.toString)
			
			compPathMap = compPathMap + ((comp, path))
			comp.getParent.doLayout()
		}
	}
	
	def layoutContainer(parent:Container) =
	{
		compPathMap.foreach({(comp:Component, path:Path) =>
			// if (parent contains comp)
			// premature optimization - shut up
			if (parent.getComponentZOrder(comp) >= 0)
			{
				comp.setLocation(path.currentPoint)
			}
		}.tupled)
		
		compPathMap = compPathMap.filter({(comp:Component, path:Path) => !path.finished}.tupled)
		
		if (! compPathMap.isEmpty)
		{
			MoveToGradualLayout.logger.finer("\tcurrTime:" + System.currentTimeMillis()
						+ "\n\t" + compPathMap.head._2.toString
						+ "\n\tisFinished: " + compPathMap.head._2.finished)
			
			threadPool.execute(new Runnable(){
				def run() {
					Thread.sleep(refreshRate)
					// container.validate doesn't seem to do anything,
					// and this, surprisingly, will not hang.
					parent.doLayout()
					//parent.repaint()
				}
			})
		}
	}
	
	val threadPool = SingleThreadExecutor(MoveToGradualLayout.threadFactory)
}

/**
 * Move along, nothing to see here.
 * Just a static class and a constant for the class.
 * @author Raymond Dodge
 * @version 04 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.swing.layouts to com.rayrobdod.swing.layouts
 * @version 13 Feb 2012 - implemented threadFactory
 */
object MoveToGradualLayout
{
	class Path(val startP:Point, val startT:Long, val endP:Point, val endT:Long)
	{
		def finished:Boolean = System.currentTimeMillis() > endT
		def started:Boolean = startT < System.currentTimeMillis()
		
		def currentPoint:Point =
		{
			if (finished) {endP}
			else if (!started) {startP}
			else {
				val time = System.currentTimeMillis()
				val percentDone = (time - startT).toDouble / (endT - startT)
				val currentX = startP.x + ((endP.x - startP.x) * percentDone)
				val currentY = startP.y + ((endP.y - startP.y) * percentDone)
				
				new Point(currentX.toInt, currentY.toInt)
			}
		}
		
		override def toString():String =
		{
			"MoveToGradualLayout.Path" + " [startP:" + startP +
					", startT:" + startT +
					", endP:" + endP +
					", endT:" + endT + "]"
		}
	}
	
	object threadFactory extends java.util.concurrent.ThreadFactory
	{
		var count = 0;
		
		override def newThread(r:Runnable) =
		{
			val t = new Thread(r);
			this.synchronized{
				count = count + 1
				t.setName("MoveToGradualLayout-"+count)
			}
			t
		}
	}
	
	val logger = 
	{
		import java.util.logging.{Logger, Level, ConsoleHandler} 
		
		val finerConsoleHander = new ConsoleHandler()
		finerConsoleHander.setLevel(Level.FINER)
		
		val logger = Logger.getLogger(
			"com.rayrobdod.swing.layouts.MoveToGradualLayout")
		logger.addHandler(finerConsoleHander)
		logger.setLevel(Level.WARNING)
//		logger.setLevel(Level.FINER)
		
		logger
	}
	
	val refreshRate:Int = 100
}

/**
 * This was an attempt to make a better MoveToGradualLayout. It's different,
 * but I'm not actually sure that it is an improvement.
 * 
 * @author Raymond Dodge
 * @version 08 Apr 2012 - copied from MoveToGradualLayout and modified
 * @version 22 May 2012 - making more resistant to NullPointerExceptions
 */
class MoveToGradualLayout2(val speed:Float) extends MoveToLayout
{
	import MoveToGradualLayout.Path
	import MoveToGradualLayout.refreshRate
	
	val compPathMap:ListMap[Component, Path] = ListMap.empty
	val startLayoutCycleLock = new Object
	
	def moveTo(comp:Component, point:Point) =
	{
		val path = new Path(comp.getLocation,
				System.currentTimeMillis(), point,
				(System.currentTimeMillis() +
					(speed * comp.getLocation.distance(point)).toLong))
		          
		MoveToGradualLayout.logger.finer(path.toString)
		
		compPathMap += ((comp, path))
		Option(comp.getParent).foreach{_.doLayout()}
		
		startLayoutCycleLock.synchronized{startLayoutCycleLock.notifyAll}
	}
	
	def layoutContainer(parent:Container) =
	{
		compPathMap.foreach({(comp:Component, path:Path) =>
			// if (parent contains comp)
			// premature optimization - shut up
			if (parent.getComponentZOrder(comp) >= 0)
			{
				comp.setLocation(path.currentPoint)
				
				if (path.finished) {compPathMap -= comp} else {}
			}
		}.tupled)
	}
	
	object RefreshRunnable extends Runnable(){
		def run() {
			while (true)
			{
				compPathMap.foreach({(comp:Component, x:Path) =>
					Option(comp.getParent).foreach{_.doLayout()}
				}.tupled)
				
				if (compPathMap.isEmpty)
				{
					startLayoutCycleLock.synchronized{startLayoutCycleLock.wait()}
				}
				else
				{
					startLayoutCycleLock.synchronized{startLayoutCycleLock.wait(refreshRate)}
				}
			}
		}
	}
	
	MoveToGradualLayout.threadFactory.newThread(RefreshRunnable).start();
}

/**
 * A MoveToLayout that permits one object to move at a time
 * 
 * @author Raymond Dodge
 * @version 22 May 2012
 */
class SequentialMoveToLayout(val speed:Float) extends Animation with MoveToLayout
{
	def currFrameLength = MoveToGradualLayout.refreshRate
	def layoutContainer(parent: java.awt.Container) = {}
	
	import MoveToGradualLayout.Path
	
	private var currentMover:Option[(Component, Path)] = None
	private val waitQueue:Queue[(Component, Point)] = Queue()
	
	def moveTo(comp:Component, point:Point) =
	{
		waitQueue += ((comp, point))
		
//		this.paused = false
	}
	
	def incrementFrame():Any =
	{
		if (currentMover == None)
		{
//			this.paused = true
		}
		else
		{
			currentMover.get._1.setLocation(currentMover.get._2.currentPoint)
		}
		
		if (currentMover == None || currentMover.get._2.finished)
		{
			if (waitQueue.isEmpty)
			{
				currentMover = None
			}
			else
			{
				val (nextComp, nextPoint) = waitQueue.dequeue
				
				currentMover = Option(nextComp, new Path(nextComp.getLocation,
						System.currentTimeMillis(),	nextPoint,
						(System.currentTimeMillis() + (speed *
								nextComp.getLocation.distance(nextPoint)).toLong)))
			}
		}
	}
	
	MoveToGradualLayout.threadFactory.newThread(this).start();
}
