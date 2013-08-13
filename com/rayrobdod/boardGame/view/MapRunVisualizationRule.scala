package com.rayrobdod.boardGame.view

import com.rayrobdod.boardGame.{RectangularField, SpaceClassConstructor => SpaceConstructor}
import scala.util.Random
import scala.parallel.Future
import java.awt.Image
import javax.swing.{Icon, ImageIcon}
import com.rayrobdod.animation.{AnimationIcon, ImageFrameAnimation}
import com.rayrobdod.util.BlitzAnimImage

/**
 * A rule for defining what image represents a tile generated
 * from a Map[String, SpaceConstructor] (for example, a JSONObject run through 
 * {@link RectangularSpaceConstructedField#desterilizeObjectNames}, and a Map[String,Object] (also could be a JSONObject)
 * 
 * TRYTHIS (use vals or lazy vals instead?)
 * 
 * @author Raymond Dodge
 * @version 02 Aug 2011
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 18 Aug 2011 - matching changes in RectangularVisualizationRule; changing #onMod to #equation
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 11 Jun 2012 - changing image from an java.awt.Image to a javax.swing.Icon
 * @version 11 Jun 2012 - making image able to be animated
 */
class MapRunVisualizationRule(strConsMap:Map[String, SpaceConstructor], rules:Map[String, Any],
		tiledImage:BlitzAnimImage) extends RectangularVisualizationRule
{
	def image:Icon = {
		val startTile = getInt("tile")
		val frameCount = getInt("animationFrames")
		
		if (frameCount == 1)
		{
			new ImageIcon(tiledImage.getFrame(startTile))
		}
		else
		{
			val frames = (startTile until (startTile + frameCount)).map{(i:Int) =>
				tiledImage.getFrame(i)
			}
			
			new AnimationIcon(new ImageFrameAnimation(frames, 1000/5, true))
		}
	}
	
	// TODO magic strings
	override def center:SpaceConstructor = getSpaceConstructor("center")
	override def north:SpaceConstructor = getSpaceConstructor("north")
	override def south:SpaceConstructor = getSpaceConstructor("south")
	override def east:SpaceConstructor = getSpaceConstructor("east")
	override def west:SpaceConstructor = getSpaceConstructor("west")
	// TODO: NE, NW, SE, SW
	
	/**
	 * An equation, to be parsed by the default JavaScript engine, relating to field size and current tile index
	 */
	 override def equation:String = if (rules.contains("indexies")) {rules("indexies").toString} else {"true"}
	
	/**
	 * Grabs the value in rules at the key "rand", and turns it into an Integer
	 * 
	 * @throws ClassCastException if rules(rand) is not a Number or String
	 * @throws NumberFormatException if rules(rand) is a String that cannot be parsed into an Int
	 */
	override def rand:Int = getInt("rand")
	
	protected def getInt(key:String):Int =
	{
		if (rules.contains(key))
		{
			rules(key) match
			{
				case x:Int => x
				case x:Long => x.toInt
				case x:Double => x.toInt
				case x:String => Integer.parseInt(x)
				case x:CharSequence => Integer.parseInt(x.toString)
				case x:AnyRef => throw new ClassCastException(
						"expected rules(key) to contain an Int, Double or String, " +
						"but it was " + x + " of type " + x.getClass.toString)
			}
		}
		else 1
	}
	
	protected def getSpaceConstructor(key:String):SpaceConstructor =
	{
		if (rules.contains(key))
		{
			rules(key) match
			{
				case x:String => strConsMap(x)
				case x:CharSequence => strConsMap(x.toString)
				case x:SpaceConstructor => x
				case x:AnyRef => throw new ClassCastException(
						"expected rules(key) to contain a SpaceConstructor or String, " +
						"but it was " + x + " of type " + x.getClass.toString)
			}
		}
		else AnySpace
	}
}
