package com.rayrobdod.boardGame.swingView

import scala.util.Random
import scala.swing.Swing
import java.awt.{Image, GridLayout, Point}
import javax.swing.{JLabel, JComponent, Icon, JPanel}
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
 * @version 2.0
 * @version 25 Aug 2012 - adding layers: one for below the tokens, one for the tokens, and one for above the tokens
 * @version 28 Oct 2012 - fixing afterimage effect with `tokenLayer.setOpaque(false)`
 *  
 * @param tilesheet the tilesheet that images to display are selected from
 * @param field the field that this tile will represent
 * @todo net.verizon.rayrobdod.swing.layouts.LayeredLayout
 */
class FieldComponent(tilesheet:RectangularTilesheet, field:RectangularField, rng:Random) extends JComponent
{
	def this(tilesheet:RectangularTilesheet, field:RectangularField) = this(tilesheet, field, Random);
	
	val points:Seq[Seq[Point]] = field.spaces.indices.map{(y:Int) => field.spaces(y).indices.map{(x:Int) => new Point(x,y)}}
	val flatPoints:Seq[Point] = points.flatten
	
	val spaces:Seq[RectangularSpace] = flatPoints.map{(p:Point) => field.space(x = p.x, y = p.y)}
	val (lowIcons:Seq[Icon], highIcons:Seq[Icon]) = flatPoints.map{(p:Point) => tilesheet.getIconFor(field, p.x, p.y, rng)}.unzip
	
	val lowLayer   = new FieldComponentLayer(lowIcons, field.spaces.size)
	val tokenLayer = new JPanel(null)
	val highLayer  = new FieldComponentLayer(highIcons, field.spaces.size)
	
	tokenLayer.setBackground(new java.awt.Color(0,0,0,0))
	tokenLayer.setOpaque(false)
	this.add(highLayer)
	this.add(tokenLayer)
	this.add(lowLayer)
	
	// adding annimations
	(lowIcons ++ highIcons).filter{_.isInstanceOf[AnimationIcon]}.foreach{(x:Icon) => 
		val animIcon = x.asInstanceOf[AnimationIcon]
		animIcon.addRepaintOnNextFrameListener(FieldComponent.this)
	}
	// TODO: stop threads at some point
	
	val threads:Seq[Thread] = (lowIcons ++ highIcons).distinct.filter{_.isInstanceOf[AnimationIcon]}.map{
				_.asInstanceOf[AnimationIcon]}.map{(x:AnimationIcon) =>
		val returnValue = new Thread(x.animation, "AnimationIcon animator")
		returnValue.setDaemon(true)
		returnValue.start()
		returnValue
	}
	
	/**
	 * A map of RectangularSpaces to the JLabel that represents that RectangularSpace
	 */
	val spaceLabelMap:Map[RectangularSpace, JLabel] = spaces.zip(lowLayer.labels).toMap
	
	// overlapping layout
	this.setLayout(OverlappingLayout)
	this.doLayout()
	
	
	class FieldComponentLayer(icons:Seq[Icon], height:Int)
				extends JPanel(new GridLayout(height, -1))
	{
		val transparent = new java.awt.Color(0,0,0,0);
		
		val labels = icons.map{new JLabel(_)}
		labels.foreach{(x:JLabel) =>
			x.setBackground(transparent)
			x.setOpaque(false)
			this.add(x)
		}
		this.setBackground(transparent)
		this.setOpaque(false)
	}
	
	object OverlappingLayout extends java.awt.LayoutManager
	{
		import java.awt.{Component, Container, Dimension}
		
		override def addLayoutComponent(name:String, comp:Component) = {}
		override def removeLayoutComponent(comp:Component) = {}

		override def layoutContainer(parent:Container) = {
			parent.getComponents().foreach{(child:Component) =>
				child.setLocation(0,0)
				child.setSize(parent.getWidth, parent.getHeight)
			}
		}

		override def minimumLayoutSize(parent:Container) = {
			val width = parent.getComponents.map{_.getMinimumSize.getWidth}.max
			val height = parent.getComponents.map{_.getMinimumSize.getHeight}.max
			
			new Dimension(width.intValue, height.intValue)
		}
		
		override def preferredLayoutSize(parent:Container) = {
			val width = parent.getComponents.map{_.getPreferredSize.getWidth}.max
			val height = parent.getComponents.map{_.getPreferredSize.getHeight}.max
			
			new Dimension(width.intValue, height.intValue)
		}
	}
}
