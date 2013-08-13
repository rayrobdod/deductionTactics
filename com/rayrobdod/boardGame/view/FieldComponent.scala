package com.rayrobdod.boardGame.view

import scala.util.Random
import scala.swing.Swing
import java.awt.{Image, GridLayout, Point}
import javax.swing.{JLabel, JComponent, Icon}
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace, SpaceClassConstructor => SpaceConstructor}
import com.rayrobdod.animation.{AnimationIcon, ImageFrameAnimation}

/**
 * A component that displays a RectangularFiled using a certain Tilesheet
 * 
 * @author Raymond Dodge
 * @version 03 Aug 2011
 * @version 04 Aug 2011 - flattening <code>Seq[Seq[_]]</code>s and combining maps so in theory less of those
 			"$$anonfunction1$$anonfunction$$" things are needed. Also less <code>x.map{_.map{doSomething}}</code>
 			This reduced 18 class files to 8. Also 37.5 KB down to 17.4 KB, uncompressed.
 * @version 06 Aug 2011 - moved from net.verizon.rayrobdod.rpgTest.view to net.verizon.rayrobdod.boardGame.view
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 11 Jun 2012 - due to RectangularVisualizationRule.image being an icon now, no longer turning it into an icon before putting it in a JLabel
 * @version 11 Jun 2012 - adding attempt to recognise an animation as one, and adding a frame change listener if it is an animation
 * @version 24 Jun 2012 - adding rng as an optional paramter to the constructor, which defults to scala.util.Random$
 * @param tilesheet the tilesheet that images to display are selected from
 * @param field the field that this tile will represent
 */
class FieldComponent(tilesheet:Tilesheet, field:RectangularField, rng:Random) extends JComponent
{
	def this(tilesheet:Tilesheet, field:RectangularField) = this(tilesheet, field, Random);
	
	// TODO: switch x and y
	val points:Seq[Seq[Point]] = field.spaces.indices.map{(x:Int) => field.spaces(x).indices.map{(y:Int) => new Point(x,y)}}
	val flatPoints:Seq[Point] = points.flatten
	
	val rules:Seq[RectangularVisualizationRule] = flatPoints.map{(p:Point) =>
		tilesheet.rules.filter{_.matches(field, p.x, p.y, rng)}.max(VisualizationRulePriorityOrdering)
	}
	
	val spaces:Seq[RectangularSpace] = flatPoints.map{(p:Point) => field.space(p.x, p.y)}
	
	val labels:Seq[JLabel] = rules.map{(x:RectangularVisualizationRule) => new JLabel(x.image)}
	labels.filter{_.getIcon.isInstanceOf[AnimationIcon]}.foreach{(x:JLabel) => 
		val animIcon = x.getIcon.asInstanceOf[AnimationIcon]
		animIcon.addRepaintOnNextFrameListener(x)
	}
	// TODO: stop threads at some point
	val threads:Seq[Thread] = labels.map{_.getIcon}.distinct.filter{_.isInstanceOf[AnimationIcon]}.map{
				_.asInstanceOf[AnimationIcon]}.map{(x:AnimationIcon) =>
		val returnValue = new Thread(x.animation, "AnimationIcon animator")
		returnValue.setDaemon(true)
		returnValue.start()
		returnValue
	}
	
	/**
	 * A map of RectangularSpaces to the JLabel that represents that RectangularSpace
	 */
	val spaceLabelMap:Map[RectangularSpace, JLabel] = spaces.zip(labels).toMap
	
	labels.foreach{this.add(_)}
	this.setLayout(new GridLayout(points.size, points(0).size))
	this.doLayout()
}
