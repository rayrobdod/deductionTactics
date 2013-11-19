/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

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
package com.rayrobdod.deductionTactics

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction
import com.rayrobdod.deductionTactics.swingView.{
		TokenClassNameToIconFromJson, TokenClassNameToIconFromBinary
}

import java.awt.{Dimension, Color}
import javax.swing.{ImageIcon, Icon}
import java.awt.Image.{SCALE_SMOOTH => imageScaleSmooth}

import java.net.URL
import java.nio.file.{Path, Files}
import javax.imageio.ImageIO
import com.kitfox.svg.app.beans.SVGIcon
import com.rayrobdod.swing.NameAndIcon

import scala.collection.mutable.{Map => MMap}
import scala.collection.immutable.{Map => IMap}


package object swingView
{
	/** @since a.5.0 */
	private[swingView] implicit def elementToNameAndIcon = {(e:Element) =>
		if (e != null) {
			new MyNameAndIcon(e.name, makeIconFor(e))
		} else {UnsetNameAndIcon}
	}
	/** @since a.5.0 */
	private[swingView] implicit def weaponToNameAndIcon = {(e:Weaponkind) =>
		if (e != null) {
			new MyNameAndIcon(e.name, makeIconFor(e))
		} else {UnsetNameAndIcon}
	}
	/** @since a.5.0 */
	private[swingView] implicit def statusToNameAndIcon = {(e:Status) =>
		if (e != null) {
			new MyNameAndIcon(e.name, makeIconFor(e))
		} else {UnsetNameAndIcon}
	}
	/** @since a.5.0 */
	private[swingView] implicit def directionToNameAndIcon = {(e:Direction) =>
		if (e != null) {
			new MyNameAndIcon(e.name, makeIconFor(e))
		} else {UnsetNameAndIcon}
	}
	
	
	/**
	 * @since 2013 Jun 14
	 */
	private val DEFAULT_SIZE = 32
	/**
	 * @version 2013 Jun 14
	 */
	def unknownIcon(size:Int = DEFAULT_SIZE):Icon = makeSVGIcon("/com/rayrobdod/glyphs/unknown.svg", size)
	
	
	private val iconCache = MMap.empty[AnyRef, Icon];
	
	
	/**
	 * Makes an icon to represent an object
	 * 
	 * @author Raymond Dodge
	 * @version 2013 Jun 14
	 * @note I tried having a bunch of overloads, rather than one mega-match
	 		but the compiler didn't seem to recognise some of the overloads
	 */
	def makeIconFor(e:AnyRef, size:Int = DEFAULT_SIZE):Icon = {
		if (iconCache.isDefinedAt(e)) {
			iconCache(e)
		} else {
			val retVal = e match {
				case None => unknownIcon(size)
				case Some(e:AnyRef) => makeIconFor(e, size)
				case e:Element   => makeSVGIcon("/com/rayrobdod/glyphs/elements/"
						+ e.name.toLowerCase + ".svg", size)
				case e:Direction => makeSVGIcon("/com/rayrobdod/glyphs/direction/"
						+ e.name.toLowerCase + ".svg", size)
				case e:Status    => makeSVGIcon("/com/rayrobdod/glyphs/status/"
						+ e.name.toLowerCase + ".svg", size)
				case e:Weaponkind => makeSVGIcon("/com/rayrobdod/glyphs/weapon/"
						+ e.name.toLowerCase.dropRight(4) + ".svg", size)
				
				case _ => unknownIcon(size)
			}
			iconCache(e) = retVal;
			retVal
		}
	}
	
	/**
	 * To load a resource and create an icon using SVGSalamander
	 * @version 2013 Jun 14
	 */
	private def makeSVGIcon(resource:String, size:Int):Icon = {
		val uri = this.getClass().getResource(resource).toURI
		
		val icon = new SVGIcon()
		icon.setSvgURI(uri)
		icon.setPreferredSize( new Dimension(size, size) )
		icon.setClipToViewbox(true)
		icon.setScaleToFit(true)
		icon.setAntiAlias(true)
		icon
	}
	
	/**
	 * To load a resource and create an icon using ImageIO
	 * @version 2013 Jun 14
	 */
	private def makePNGIcon(resource:String, size:Int):Icon = {
		val url = this.getClass().getResource(resource)
		
		val image = ImageIO.read(url)
		val image32 = image.getScaledInstance(size, size, imageScaleSmooth)
		
		new javax.swing.ImageIcon(image32)
	}
	
	/**
	 * To load a resource and create an icon using ImageIO
	 * @version 2013 Jun 14
	 */
	private def makePNGIcon(resource:String, size:Int, portion:(Int,Int,Int,Int)):Icon = {
		val url = this.getClass().getResource(resource)
		
		val image = ImageIO.read(url)
		val imagePort = image.getSubimage(portion._1, portion._2, portion._3, portion._4)
		val image32 = imagePort.getScaledInstance(size, size, imageScaleSmooth)
		
		new javax.swing.ImageIcon(image32)
	}
	
	/**
	 * Turns an file at the given URL into an icon, based on the url's last
	 * filetype extension
	 * @since 22 Aug 2011
	 * @version 2013 Jun 30 - copied over from deductionTactics.package
	 */
	def loadIcon(resource:String, size:Int):Icon = {
		val (path:String, frac:String) = resource.split('#').toSeq match {
			case Seq(a, b) => (a, b)
			case Seq(a)    => (a, "")
		}
		val fracMatcher = xywhPattern.matcher(frac)
		val fracRect = if (fracMatcher.matches) {
			Some(( fracMatcher.group(1).toInt, fracMatcher.group(2).toInt,
				fracMatcher.group(3).toInt, fracMatcher.group(4).toInt ))
		} else None
		
		path.split('.').last match {
			case "svg" => makeSVGIcon(path, size)
			case "png" => {
				if (fracRect == None) {
					makePNGIcon(path, size)
				} else {
					makePNGIcon(path, size, fracRect.get)
				}
			}
		}
	}
	private val xywhPattern = java.util.regex.Pattern.compile("""xywh=(\d+),(\d+),(\d+),(\d+)""")
	
	
	
	
	
	
	/**
	 * Creates an undetailed icon which matches some of the traits of the TokenClass
	 * @version 27 Feb 2012
	 * @version 28 Jun 2012 - adding a cache to generateGenericIcon
	 * @version 2013 Jun 30 - copied over from deductionTactics.package
	 */
	def generateGenericIcon(tokenClass:TokenClass) =
	{
		val fileName = tokenClass.atkWeapon.map{genericTokenClassFile(_)}.getOrElse(
				"/com/rayrobdod/deductionTactics/tokenClasses/sprites/generic/Gray shirt.png")
		val base = ImageIO.read(this.getClass().getResource(fileName))
		
		tokenClass.atkElement.foreach{(elem:Element) => 
			(0 until base.getWidth).foreach{(x:Int) => 
				(0 until base.getHeight).foreach{(y:Int) => 
					if (base.getRGB(x,y) == 0xFF949494) {base.setRGB(x,y,elementToColor(elem).brighter.getRGB)}
					if (base.getRGB(x,y) == 0xFF7F7F7F) {base.setRGB(x,y,elementToColor(elem).getRGB)}
					if (base.getRGB(x,y) == 0xFF747474) {base.setRGB(x,y,elementToColor(elem).darker.getRGB)}
				}
			}
		}
		
		val returnValue = new ImageIcon(base)
	//	genericIconCache += ((tokenClass, returnValue))
		returnValue
	}
	
	
	
	/**
	 * @version 2013 Aug 06
	 */
	val tokenClassNameToIcon:Map[String, Icon] =
	{
		import scala.collection.JavaConversions.iterableAsScalaIterable
		import com.rayrobdod.util.services.ResourcesServiceLoader
		
		val a:Seq[Path] = new ResourcesServiceLoader(CannonicalTokenClass.SERVICE).toSeq
		
		// Binary version
		val b:Seq[Map[String, Icon]] = a.map{(jsonPath:Path) =>
			if (jsonPath.toString.endsWith(".rrd-dt-tokenClass")) {
			
				new TokenClassNameToIconFromBinary(Seq(jsonPath)).map
			} else { // assume JSON
				new TokenClassNameToIconFromJson(Seq(jsonPath)).map
			}
		}
		val e = b.foldLeft(IMap.empty[String, Icon]){_ ++ _}
		
		e
	}
	
	/**
	 * @since a.5.1
	 */
	def tokenClassToIcon(tokenClass:TokenClass) = {
		tokenClassNameToIcon.getOrElse(tokenClass.name,
				generateGenericIcon(tokenClass))
	}
	
	/** @since a.5.0 */
	def elementToColor(e:Element):Color = e match {
		case Elements.Light    => new Color(253,253,187)
		case Elements.Electric => Color.yellow
		case Elements.Fire     => Color.red
		case Elements.Frost    => new Color(170,170,255)
		case Elements.Sound    => new Color(0,255,0)
		case _                 => Color.gray
	}
	/** @since a.5.0 */
	def genericTokenClassFile(k:Weaponkind):String = {
		"/com/rayrobdod/deductionTactics/tokenClasses/sprites/generic/" + k.classType + ".png"
	}
	/** @since a.5.0 */
	def attackEffectFile(k:Weaponkind):String = {
		"/com/rayrobdod/deductionTactics/tokenClasses/sprites/effects/" + k.name + " strike.png"
	}
	
	
	
	
	final class MyNameAndIcon(val name:String, val icon:Icon) extends NameAndIcon
	final val UnsetNameAndIcon:NameAndIcon = new MyNameAndIcon("Unset", unknownIcon())
}
