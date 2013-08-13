package com.rayrobdod.boardGame.view

import scala.util.Random
import scala.swing.Swing
import java.awt.{Image, GridLayout, Point}
import javax.swing.{JLabel, JComponent, Icon}
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace, SpaceClassConstructor => SpaceConstructor}

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
 */
class FieldComponent(tilesheet:Tilesheet, field:RectangularField) extends JComponent
{
	val points:Seq[Seq[Point]] = field.spaces.indices.map{(x:Int) => field.spaces(x).indices.map{(y:Int) => new Point(x,y)}}
	val flatPoints:Seq[Point] = points.flatten
	
	val rules:Seq[RectangularVisualizationRule] = flatPoints.map{(p:Point) =>
		tilesheet.rules.filter{_.matches(field, p.x, p.y, Random)}.max(VisualizationRulePriorityOrdering)
	}
	
	val spaces:Seq[RectangularSpace] = flatPoints.map{(p:Point) => field.space(p.x, p.y)}
	
	val labels:Seq[JLabel] = rules.map{(x:RectangularVisualizationRule) => new JLabel(Swing.Icon(x.image))}
	
	val spaceLabelMap:Map[RectangularSpace, JLabel] = spaces.zip(labels).toMap
	
	labels.foreach{this.add(_)}
	this.setLayout(new GridLayout(points.size, points(0).size))
	this.doLayout()
}
