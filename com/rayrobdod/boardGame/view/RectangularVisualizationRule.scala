package com.rayrobdod.boardGame.view

import com.rayrobdod.boardGame.{RectangularField, RectangularSpace, SpaceClassConstructor => SpaceConstructor}
import scala.util.Random
import scala.parallel.Future
import java.awt.Image
import javax.swing.Icon
import java.util.regex.{Pattern, Matcher}
import javax.script.{Bindings, SimpleBindings, ScriptEngineManager}

/**
 * A generic definition of a rule for defining what image represents a tile.
 * 
 * This class is somewhat based on Squidi's [http://www.squidi.net/mapmaker/musings/m091015.php RLTypeViewRule description]
 * 
 * @author Raymond Dodge
 * @version 02 Aug 2011
 * @version 03 Aug 2011 - fixed #matches so it won't throw IndexOutOfBoundsException around the edges of the field.
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 18 Aug 2011 - removing #onMod, adding #equation
 * @version 18 Aug 2011 - switching the priority of the equation and the rand value
 * @version 03 Aug 2011 - Switchiing from RectangularSpaceConstructor to SpaceClassConstructor
 * @version 04 Aug 2011 - changing the matches() function such that it works with SpaceClassConstructors
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 11 Jun 2012 - changing image from an java.awt.Image to a javax.swing.Icon
 */
trait RectangularVisualizationRule
{
	/** the image used if the rule is matched */
	def image:Icon
	
	// TODO: replace with a map of directions to SpaceConstructors
	def center:SpaceConstructor = AnySpace
	def north:SpaceConstructor = AnySpace
	def south:SpaceConstructor = AnySpace
	def east:SpaceConstructor = AnySpace
	def west:SpaceConstructor = AnySpace
	// TODO: NE, NW, SE, SW
	
	def rand:Int = 1
	
	// TODO: turn into a Function4[Int,Int,Int,Int,Boolean]
	def equation:String = "true"
	
	/** @see matches */
//	final def apply(field:RectangularField, x:Int, y:Int, rng:Random) = matches(field, x, y, rng)
	/** Determines if the space at (x,y) in the field matches this space. */
	final def matches(field:RectangularField, x:Int, y:Int, rng:Random):Boolean =
	{
		import RectangularVisualizationRule.{scriptEngine, buildBindings}
		
		// TODO: switch whether the x or y is one the plus or minus.
		(!field.containsIndexies(x,y) || center.unapply(field.space(x,y).typeOfSpace)) &&
			(!field.containsIndexies(x,y-1) || west.unapply(field.space(x,y-1).typeOfSpace)) &&
			(!field.containsIndexies(x,y+1) || east.unapply(field.space(x,y+1).typeOfSpace)) &&
			(!field.containsIndexies(x-1,y) || north.unapply(field.space(x-1,y).typeOfSpace)) &&
			(!field.containsIndexies(x+1,y) || south.unapply(field.space(x+1,y).typeOfSpace)) &&
			(rng.nextInt(rand) == 0) &&
			(scriptEngine.eval(equation, buildBindings(x, y, field.spaces.size,
					field.spaces(0).size)) match
			{
				case b:java.lang.Boolean => b
				case i:java.lang.Integer => i != 0
			})
	}
	
	/**
	 * A priority is a number given to a rule; the more specific the rule,
	 * the higher it's priority (remember CSS?). 
	 * 
	 * In this case, a rules priority is 100 times the number of defined spaces,
	 * plus 10 times the rand value, plus the a calculation based on the equation.
	 */
	final def priority =
	{
		import RectangularVisualizationRule.{divisionCount, numberSum}
		val equationBase = 1000
		val spaceMultiplier = 10000
		val randMultiplier = 1
		
		(if (center != AnySpace) spaceMultiplier else 0) +
			(if (north != AnySpace) spaceMultiplier else 0) +
			(if (south != AnySpace) spaceMultiplier else 0) +
			(if (east != AnySpace) spaceMultiplier else 0) +
			(if (west != AnySpace) spaceMultiplier else 0) +
			rand * randMultiplier +
			(if (equation != "true") {equationBase / divisionCount(equation) + numberSum(equation)} else 0)
	}
}

/**
 * Holds static constants for the RectangularVisualizationRule
 * @author Raymond Dodge
 * @version 18 Aug 2011
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
object RectangularVisualizationRule
{
	val divisionPattern = Pattern.compile("[%//]")
	val numberPattern = Pattern.compile("\\d+")
	
	val scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	
	def buildBindings(x:Int, y:Int, width:Int, height:Int):Bindings =
	{
		val binding = new SimpleBindings()
		binding.put("x", x)
		binding.put("y", y)
		binding.put("w", width)
		binding.put("h", height)
		binding
	}
	
	def divisionCount(equation:String):Int =
	{
		countMatches( divisionPattern.matcher(equation) )
	}
	
	private def countMatches(m:Matcher):Int =
	{
		if (! m.hitEnd)
		{
			m.find
			1 + countMatches(m)
		}
		else {0}
	}
	
	def numberSum(equation:String):Int =
	{
		val m = numberPattern.matcher(equation)
		m.find()
		sumMatches( m )
	}
	
	private def sumMatches(m:Matcher):Int =
	{
		if (! m.hitEnd)
		{
			m.find()
			val numberAsString = m.group
			val number = Integer.parseInt(numberAsString)
			number + sumMatches(m)
		} else {0}
	}
}

/**
 * An ordering that orders the VisualizationRules by their priority.
 * 
 * @author Raymond Dodge
 * @version 02 Aug 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 */
object VisualizationRulePriorityOrdering extends Ordering[RectangularVisualizationRule]
{
	def compare(x:RectangularVisualizationRule, y:RectangularVisualizationRule):Int =
	{
		x.priority compareTo y.priority
	}
}

/**
 * A simple RectangularVisualizationRule where each item is specified in the constructor.
 * 
 * @author Raymond Dodge
 * @version 02 Aug 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 11 Jun 2012 - changing image from an java.awt.Image to a javax.swing.Icon
 */
class BluntRectangularVisualizationRule(
		override val image:Icon,
		override val center:SpaceConstructor = AnySpace,
		override val north:SpaceConstructor = AnySpace,
		override val south:SpaceConstructor = AnySpace,
		override val east:SpaceConstructor = AnySpace,
		override val west:SpaceConstructor = AnySpace,
		override val equation:String = "true",
		override val rand:Int = 1) extends RectangularVisualizationRule
