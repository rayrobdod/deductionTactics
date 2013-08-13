package com.rayrobdod.deductionTactics.swingView

import scala.util.Random
import java.awt.{Component, Graphics, Graphics2D, Color}
import java.awt.geom.GeneralPath
import javax.swing.Icon
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.swing.SolidColorIcon
import com.rayrobdod.boardGame.swingView.RectangularTilesheet
import com.rayrobdod.boardGame.{SpaceClass, Space}

/**
 * @author Raymond Dodge
 * @version 2013 Jan 19
 * @version 2013 Jan 26 - minor optimizations
 */
object FieldChessTilesheet extends RectangularTilesheet
{
	private val waterLight = new Color(61, 215, 237);
	private val waterDark  = new Color(69, 208, 228);
	private val grassLight = new Color(61, 237, 61);
	private val grassDark  = new Color(69, 228, 69);
	private val lavaLight  = new Color(237, 61, 61);
	private val lavaDark   = new Color(228, 69, 69);
	private val rockLight  = new Color(149, 149, 149);
	private val rockDark   = new Color(127, 127, 127);
	private val transColor = new Color(0,0,0,0);
	
	def name = "Field Chess";
	
	private def spaceClassToColor(s:SpaceClass, useDarker:Boolean) = {
			import com.rayrobdod.deductionTactics._
			
			if (useDarker) {
				s match {
					case PassibleSpaceClass() => grassDark
					case UnitAwareSpaceClass() => grassDark
					case ImpassibleSpaceClass() => rockDark
					case AttackableOnlySpaceClass() => rockDark
					case NoStandOnSpaceClass() => waterDark
					case FireRestrictedSpaceClass() => lavaDark
				}
			} else {
				s match {
					case PassibleSpaceClass() => grassLight
					case UnitAwareSpaceClass() => grassLight
					case ImpassibleSpaceClass() => rockLight
					case AttackableOnlySpaceClass() => rockLight
					case NoStandOnSpaceClass() => waterLight
					case FireRestrictedSpaceClass() => lavaLight
				}
			}
	}
	
	def getIconFor(field:RectangularField, x:Int, y:Int, rng:Random):(Icon, Icon) = {
		val useDarker = ((x + y) % 2) == 0
		val center = spaceClassToColor(field.space(x,y).typeOfSpace, useDarker)
		
		def SpaceSeqToColor(x:Seq[Space]) = {
			val x1 = x.map{(y:Space) => spaceClassToColor(y.typeOfSpace, useDarker)}
			Option(x1.head)
					.filter{_ != center && x1.forall{_ == x1.head}}
					.getOrElse(transColor)
		}
		
		val nw = if (field.containsIndexies(x-1, y-1)) {
			SpaceSeqToColor(Seq(
				field.space(x-1,y  ),
				field.space(x-1,y-1),
				field.space(x,  y-1)
			))
		} else {transColor}
		val ne = if (field.containsIndexies(x+1, y-1)) {
			SpaceSeqToColor(Seq(
				field.space(x+1,y  ),
				field.space(x+1,y-1),
				field.space(x,  y-1)
			))
		} else {transColor}
		val sw = if (field.containsIndexies(x-1, y+1)) {
			SpaceSeqToColor(Seq(
				field.space(x-1,y  ),
				field.space(x-1,y+1),
				field.space(x,  y+1)
			))
		} else {transColor}
		val se = if (field.containsIndexies(x+1, y+1)) {
			SpaceSeqToColor(Seq(
				field.space(x+1,y  ),
				field.space(x+1,y+1),
				field.space(x,  y+1)
			))
		} else {transColor}
		
		((
			new MyIcon(center, nw, ne, sw, se),
			new SolidColorIcon(transColor,32,32)
		))
	}
	
	private class MyIcon(center:Color, nw:Color, ne:Color, sw:Color, se:Color) extends Icon
	{
		def getIconWidth = 32;
		def getIconHeight = 32;
		
		def paintIcon(c:Component, g:Graphics, x:Int, y:Int)
		{
			val nwCorner = new GeneralPath
			nwCorner.moveTo(x   ,y   )
			nwCorner.lineTo(x+16,y   )
			nwCorner.quadTo(x   ,y   ,x   ,y+16)
			nwCorner.closePath()
			
			val neCorner = new GeneralPath
			neCorner.moveTo(x+32,y   )
			neCorner.lineTo(x+32,y+16)
			neCorner.quadTo(x+32,y   ,x+16,y   )
			neCorner.closePath()
			
			val swCorner = new GeneralPath
			swCorner.moveTo(x   ,y+32)
			swCorner.lineTo(x+16,y+32)
			swCorner.quadTo(x   ,y+32,x   ,y+16)
			swCorner.closePath()
			
			val seCorner = new GeneralPath
			seCorner.moveTo(x+32,y+32)
			seCorner.lineTo(x+32,y+16)
			seCorner.quadTo(x+32,y+32,x+16,y+32)
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
