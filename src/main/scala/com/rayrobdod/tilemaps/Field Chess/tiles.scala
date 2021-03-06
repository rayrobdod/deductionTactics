/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.rayrobdod.deductionTactics.swingView

import scala.util.Random
import java.awt.{Component, Graphics, Graphics2D, Color}
import java.awt.geom.GeneralPath
import javax.swing.Icon
import com.rayrobdod.swing.SolidColorIcon
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.RectangularSpace
import com.rayrobdod.boardGame.RectangularIndex
import com.rayrobdod.boardGame.Tiling
import com.rayrobdod.boardGame.view.Tilesheet
import com.rayrobdod.boardGame.view.RectangularDimension
import com.rayrobdod.boardGame.view.TileLocationIcons
import com.rayrobdod.deductionTactics.SpaceClass

/**
 * A basic tilesheet that is both visually simplistic and versatile.
 * 
 * Is a hard-coded thing instead of a resource thing because the resources
 * were both slow and large, an the stylesheet is simple enough that it can
 * be hard-coded relatively easily.
 * 
 * @since a.4.1
 * @version next
 */
object FieldChessTilesheet extends RectangularTilesheet {
	private val waterLight = new Color(61, 215, 237);
	private val waterDark  = new Color(69, 208, 228);
	private val grassLight = new Color(61, 237, 61);
	private val grassDark  = new Color(69, 228, 69);
	private val lavaLight  = new Color(237, 61, 61);
	private val lavaDark   = new Color(228, 69, 69);
	private val rockLight  = new Color(149, 149, 149);
	private val rockDark   = new Color(127, 127, 127);
	private val tallGrassLight = new Color(56, 232, 91);
	private val tallGrassDark  = new Color(64, 223, 99);
	private val transColor = new Color(0,0,0,0);
	private val otherColor = new Color(0,0,0);
	
	def name:String = "Field Chess";
	
	private def spaceClassToColor(s:SpaceClass, useDarker:Boolean):Color = {
			import com.rayrobdod.deductionTactics._
			
			if (useDarker) {
				s match {
					case FreePassageSpaceClass()   => grassDark
					case AllyPassageSpaceClass()   => grassDark
					case UniPassageSpaceClass()    => grassDark
					case ImpassibleSpaceClass()    => rockDark
					case AttackOnlySpaceClass()    => rockDark
					case FlyingPassageSpaceClass() => waterDark
					case FirePassageSpaceClass()   => lavaDark
					case SlowPassageSpaceClass()   => tallGrassDark
					case _ => otherColor
				}
			} else {
				s match {
					case FreePassageSpaceClass()   => grassLight
					case AllyPassageSpaceClass()   => grassLight
					case UniPassageSpaceClass()    => grassLight
					case ImpassibleSpaceClass()    => rockLight
					case AttackOnlySpaceClass()    => rockLight
					case FlyingPassageSpaceClass() => waterLight
					case FirePassageSpaceClass()   => lavaLight
					case SlowPassageSpaceClass()   => tallGrassLight
					case _ => otherColor
				}
			}
	}
	
	override def getIconFor(field:Tiling[_ <: SpaceClass, RectangularIndex, _], xy:RectangularIndex, rng:Random):TileLocationIcons[Icon] = {
		val (x,y) = xy
		val useDarker = ((x + y) % 2) == 0
		val center = spaceClassToColor(field.spaceClass(x, y).get, useDarker)
		
		def spaceSeqToColor(x:Seq[SpaceClass]):Color = {
			val x1 = x.map{(y:SpaceClass) => spaceClassToColor(y, useDarker)}
			Option(x1.head)
					.filter{_ != center && x1.forall{_ == x1.head}}
					.getOrElse(transColor)
		}
		
		val nw = if (field.contains(x - 1, y - 1)) {
			spaceSeqToColor(Seq(
				field.spaceClass(x - 1, y    ).get,
				field.spaceClass(x - 1, y - 1).get,
				field.spaceClass(x,    y - 1).get
			))
		} else {transColor}
		val ne = if (field.contains(x + 1, y - 1)) {
			spaceSeqToColor(Seq(
				field.spaceClass(x + 1, y    ).get,
				field.spaceClass(x + 1, y - 1).get,
				field.spaceClass(x,     y - 1).get
			))
		} else {transColor}
		val sw = if (field.contains(x - 1, y + 1)) {
			spaceSeqToColor(Seq(
				field.spaceClass(x - 1, y    ).get,
				field.spaceClass(x - 1, y + 1).get,
				field.spaceClass(x,     y + 1).get
			))
		} else {transColor}
		val se = if (field.contains(x + 1, y + 1)) {
			spaceSeqToColor(Seq(
				field.spaceClass(x + 1, y    ).get,
				field.spaceClass(x + 1, y + 1).get,
				field.spaceClass(x,     y + 1).get
			))
		} else {transColor}
		
		TileLocationIcons(
			new MyIcon(center, nw, ne, sw, se),
			new SolidColorIcon(transColor, 32, 32)
		)
	}
	
	override def iconDimensions:RectangularDimension = RectangularDimension(32, 32)
	
	private class MyIcon(center:Color, nw:Color, ne:Color, sw:Color, se:Color) extends Icon
	{
		def getIconWidth:Int = 32;
		def getIconHeight:Int = 32;
		
		def paintIcon(c:Component, g:Graphics, x:Int, y:Int):Unit =
		{
			val nwCorner = new GeneralPath
			nwCorner.moveTo(x     , y     )
			nwCorner.lineTo(x + 16, y     )
			nwCorner.quadTo(x, y, x, y + 16)
			nwCorner.closePath()
			
			val neCorner = new GeneralPath
			neCorner.moveTo(x + 32, y)
			neCorner.lineTo(x + 32, y + 16)
			neCorner.quadTo(x + 32, y, x + 16, y)
			neCorner.closePath()
			
			val swCorner = new GeneralPath
			swCorner.moveTo(x     , y + 32)
			swCorner.lineTo(x + 16, y + 32)
			swCorner.quadTo(x     , y + 32, x     , y + 16)
			swCorner.closePath()
			
			val seCorner = new GeneralPath
			seCorner.moveTo(x + 32, y + 32)
			seCorner.lineTo(x + 32, y + 16)
			seCorner.quadTo(x + 32, y + 32,x + 16, y + 32)
			seCorner.closePath()
		
			val g2 = g.asInstanceOf[Graphics2D]
			
			g2.setColor(center);
			g2.fillRect(x, y, 32, 32);
			
			g2.setColor(ne)
			g2.fill(neCorner);
			
			g2.setColor(nw)
			g2.fill(nwCorner);
			
			g2.setColor(se)
			g2.fill(seCorner);
			
			g2.setColor(sw)
			g2.fill(swCorner);
			
		}
	}
}
