package com.rayrobdod.boardGame.view

import scala.swing.Component
import java.util.Random
import java.awt.Color.{white, black}
import java.awt.{Graphics2D, Polygon, Font, Shape, Dimension}
import scala.swing.event.Event
import scala.swing.Reactions.Reaction
import scala.swing.Publisher
import scala.actors.Future
import scala.collection.immutable.{Traversable, SortedMap}
import java.util.logging.{Logger, Level, ConsoleHandler}
import java.util.concurrent.{Executors, TimeUnit}
import com.rayrobdod.math.geom.{Dodecahedron, Isoshedron}
import com.rayrobdod.math.distribution.{ProbabilityDistribution, 
		BinomialDistribution, DiscreteUniformDistribution, FiniteDistribution}
import com.rayrobdod.boardGame.{Die, RollDie, Animation}

/**
 * A component that shows a die, with an animation.
 * @author Raymond Dodge
 * @version 17 May 2011
 * @version 14 June 2011 - since Die with LinkedComponent was eliminated, this absorebed its abilites.
 * @version 16 June 2011 - debugging!
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.boardGame to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
abstract class DieComponent(val die:Die) extends Component with Animation
{
	import VisualDiceSetLoggerInitializer.{dieLogger => logger}
	logger.entering("DieComponent", "this()")
	
	override val totalFrames = 30
	
	logger.fine("die.probabilityDistribution.possibleValues was " + die.probabilityDistribution.possibleValues)
	def possibleValues:Traversable[Int] = die.probabilityDistribution.possibleValues.map((x:Double) => x.intValue)
	logger.fine("possibleValues set to: " + possibleValues)
	def possibleDistribution:ProbabilityDistribution =
	{
		val mapValues = possibleValues.map{(x:Int) => ((x.doubleValue, 1d / possibleValues.size))}.toMap
		val sortedValues = SortedMap.empty[Double,Double] ++ mapValues
		
		new FiniteDistribution(sortedValues)
	}
	logger.fine("possibleDistribution set to: " + possibleDistribution)
	protected var endResult = possibleDistribution.rand()
	
	def frameDelay() = 33 // * (currentFrame / totalFrames) * (currentFrame / totalFrames))
	
	stop()
	
	die.action = RollDieAnimationReaction
	object RollDieAnimationReaction extends PartialFunction[Event, Int]
	{
		override def apply(e:Event) = {e match
		{
			case RollDie =>
			{
				val dieValue = die.probabilityDistribution.rand().floor.intValue
				DieComponent.this.endResult = dieValue
				DieComponent.this.restart()
				DieComponent.this.repaint()
				
				DieComponent.this.animatingFinishFuture.apply()
				dieValue
			}
			case _ =>  throw new IllegalArgumentException("Event not a RollDie event")
		}}
		
		override def isDefinedAt(e:Event) = {e match
		{
			case RollDie => true
			case _ => false
		}}
	}
	
	
}

/**
 * a factory. Also includes things used by the DieComponents
 * @author Raymond Dodge
 * @version 20 May 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.boardGame to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
object DieComponent
{
	val valueGenerator = new Random()
	val executor = Executors.newSingleThreadScheduledExecutor()
	
	private object to
	{
		def unapply(range:Range):Option[(Int, Int)] = Option((range.start, range.end))
	}
	
	/** Tries to find a known DieComponent that is appropriate for the distribution */
	def apply(die:Die):DieComponent =
	{
		die.probabilityDistribution match
		{
			case DiscreteUniformDistribution(1 to 6) => new DottedSixDieComponent(die)
			case BinomialDistribution(6, _) => new DottedSixDieComponent(die)
			case DiscreteUniformDistribution(1 to 20) => new TwentyDieComponent(die)
			case BinomialDistribution(20, _) => new TwentyDieComponent(die)
			case DiscreteUniformDistribution(1 to 12) => new TwelveDieComponent(die)
			case BinomialDistribution(12, _) => new TwelveDieComponent(die)
			case _ => new SixDieComponent(die)
//			case _ => throw new IllegalArgumentException("cannot make a valid die component from " + die)
		}
	}
}

class DoLayouter(val comp:Component) extends Runnable
{
	def run() = 
	{
		comp.repaint()
	}
}

/**
 * A component that shows a 6-sized die, with a rolling animation.
 * @author Raymond Dodge
 * @version 17 May 2011
 * @version 16 June 2011 - debugging!
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.boardGame to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
class DottedSixDieComponent(die:Die) extends DieComponent(die)
{
	import VisualDiceSetLoggerInitializer.{sixDieLogger => logger}
	import scala.swing.Swing.pair2Dimension
	
	logger.entering("SixDieComponent", "this()")
	
	// easy enough to make allow between 0 and 9?
	private val dpdpv = die.probabilityDistribution.possibleValues
	if (!(dpdpv.max.intValue <= 6 && dpdpv.min.intValue >= 1)) throw new IllegalArgumentException(
				"this die component can only handle die values between 1 and 6. " + die)
	
	/** no, not val! That doesn't truly make dieSize 6 until the super's constructor is finished! */
	override def possibleValues = 1 to 6
	
	val dotsLocation = Seq((.25, .25), (.25, .50), (.25, .75), (.5,.5), (.75, .25), (.75, .50), (.75, .75))
	val usedDots = Map((0, Set()), (1, Set(3)), (2, Set(0,6)), (3, Set(0,3,6)),
				(4, Set(0,2,4,6)), (5, Set(0,2,3,4,6)), (6, Set(0,1,2,4,5,6)))  
	def dotDiameter = math.max(math.min(size.width, size.height) / 6, 8)
	
	minimumSize = new Dimension(8 * 5 , 8 * 5)
		
	override def paint(g:Graphics2D) = 
	{
		logger.entering("SixDieComponent", "paint(Graphics2D)", "some g")
		
		val currentValue = if (currentFrame <= totalFrames) {possibleDistribution.rand()} else {endResult}
		val drawnDots = usedDots(currentValue.intValue)
		
		g.setColor(background)
		g.fillRect(0, 0, size.width, size.height)
		g.setColor(foreground)
		drawnDots.map{dotsLocation(_)}.foreach{(x:Pair[Double, Double]) =>
			{
				val upperLeftCornerX = (size.width * x._1).intValue - (dotDiameter / 2)
				val upperLeftCornerY = (size.height * x._2).intValue - (dotDiameter / 2)
				
				g.fillOval(upperLeftCornerX, upperLeftCornerY, dotDiameter, dotDiameter)
			}
		}
		
		this.incrementFrame()
		
		if (currentFrame <= totalFrames + 1)
		{
			DieComponent.executor.schedule(new DoLayouter(this), frameDelay(), TimeUnit.MILLISECONDS)
		}
	}
}

/**
 * @author Raymond Dodge
 * @version 20 May 2011
 * @version 16 June 2011 - debugging!
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.boardGame to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
trait NumberOnShapeDieComponent extends DieComponent
{
	/** 
	 * Returns a function that returns the shape this will paint.
	 * The int parameter is te size the spahe needs to fill
	 */
	def shapeFunction:Function1[Int,Shape]
	
	override def paint(g:Graphics2D) = 
	{
		import VisualDiceSetLoggerInitializer.{shapeDieLogger => logger}
		logger.entering("NumberOnShapeDieComponent", "paint(Graphics2D)", "some g")
		
		val currentValue = if (currentFrame <= totalFrames) {possibleDistribution.rand()} else {endResult}.intValue
		val stringBounds = g.getFontMetrics(g.getFont()).getStringBounds(currentValue.toString, g)
		
		logger.finer("currentFrame = " + currentFrame)
		logger.finer("endResult = " + endResult)
		logger.finer("possibleDistribution.rand() = " + possibleDistribution.rand())
		logger.finer("currentValue = " + currentValue)
		
		val scale = math.min(size.width, size.height) - 1
		val s = shapeFunction(scale)
		g.setColor(background)
		g.fill(s)
		g.setColor(foreground)
		g.draw(s)
		
		g.drawString(currentValue.intValue.toString, (scale - stringBounds.getWidth.intValue) / 2, (scale + stringBounds.getHeight.intValue) / 2)
		
		incrementFrame()
		
		if (currentFrame <= totalFrames + 1)
		{
			DieComponent.executor.schedule(new DoLayouter(this), frameDelay(), TimeUnit.MILLISECONDS)
		}
	}
}

/**
 * @author Raymond Dodge
 * @version 14 June 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.boardGame to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
class SixDieComponent(die:Die) extends DieComponent(die) with NumberOnShapeDieComponent
{
//	private val stringBounds = g.getFontMetrics(g.getFont()).getStringBounds(currentValue.toString, g)
//	minimumSize = new Dimension(stringBounds.getWidth.intValue + 2, stringBounds.getHeight.intValue + 2)
	
	override def shapeFunction = {(x:Int) => new java.awt.Rectangle(0,0,x,x)}
}

/**
 * @author Raymond Dodge
 * @version 20 May 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.boardGame to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
class TwentyDieComponent(die:Die) extends DieComponent(die) with NumberOnShapeDieComponent
{
//	private val stringBounds = g.getFontMetrics(g.getFont()).getStringBounds(currentValue.toString, g)
//	minimumSize = new Dimension(stringBounds.getWidth.intValue * 3, stringBounds.getHeight.intValue * 3)
	
	override def shapeFunction = {(x:Int) => Isoshedron(x)}
}

/**
 * @author Raymond Dodge
 * @version 27 May 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.boardGame to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
class TwelveDieComponent(die:Die) extends DieComponent(die) with NumberOnShapeDieComponent
{
	//TODO remove delay for changing the font
	//	this.font = new Font("", Font.BOLD, 20)
	
//	private val stringBounds = g.getFontMetrics(g.getFont()).getStringBounds(currentValue.toString, g)
//	minimumSize = new Dimension(stringBounds.getWidth.intValue * 3, stringBounds.getHeight.intValue * 3)
	
	override def shapeFunction = {(x:Int) => Dodecahedron(x)}
}

object VisualDiceSetLoggerInitializer
{
	val warningConsoleHander = new ConsoleHandler()
	warningConsoleHander.setLevel(Level.WARNING)
	
	val finerConsoleHander = new ConsoleHandler()
	finerConsoleHander.setLevel(Level.FINER)
	
	val dieLogger = Logger.getLogger("net.verizon.rayrobdod.boardGame.DieComponent")
	dieLogger.addHandler(finerConsoleHander)
	dieLogger.setLevel(Level.WARNING)
	
	val sixDieLogger = Logger.getLogger("net.verizon.rayrobdod.boardGame.SixDieComponent")
	sixDieLogger.addHandler(finerConsoleHander)
	sixDieLogger.setLevel(Level.WARNING)
	
	val shapeDieLogger = Logger.getLogger("net.verizon.rayrobdod.boardGame.NumberOnShapeDieComponent")
	shapeDieLogger.addHandler(finerConsoleHander)
	shapeDieLogger.setLevel(Level.WARNING)
}
