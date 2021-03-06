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
package com.rayrobdod.deductionTactics

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.Directions.Direction

import java.awt.{Dimension, Color}
import java.nio.charset.StandardCharsets.UTF_8
import javax.swing.{ImageIcon, Icon, ListModel}
import java.awt.Image.{SCALE_SMOOTH => imageScaleSmooth}

import javax.imageio.ImageIO
import scala.collection.immutable.{Seq, Map}
import com.kitfox.svg.app.beans.SVGIcon
import com.rayrobdod.swing.{NameAndIcon, ScalaSeqListModel}
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.RectangularSpace
import com.rayrobdod.boardGame.RectangularIndex
import com.rayrobdod.boardGame.view.Tilesheet
import com.rayrobdod.boardGame.view.RectangularDimension
import com.rayrobdod.boardGame.view.Swing


package object swingView
{
	type RectangularTilesheet = Tilesheet[SpaceClass, RectangularIndex, RectangularDimension, Icon]
	
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
	
	/** @since a.6.0 */
	val teamColors:Function1[Int,Color] = Seq(new Color(64,64,255), new Color(255,64,64), new Color(64,255,64), new Color(192,192,64) /*,... */)
	
	
	/**
	 * @since 2013 Jun 14
	 */
	private val DEFAULT_SIZE = 32
	/**
	 * @version 2013 Jun 14
	 */
	def unknownIcon(size:Int = DEFAULT_SIZE):Icon = makeSVGIcon("/com/rayrobdod/glyphs/unknown.svg", size)
	
	
	private var iconCache = Map.empty[(AnyRef, Int), Icon];
	
	
	/**
	 * Makes an icon to represent an object
	 * 
	 * @author Raymond Dodge
	 * @version a.6.0
	 * @note I tried having a bunch of overloads, rather than one mega-match
	 		but the compiler didn't seem to recognise some of the overloads
	 */
	def makeIconFor(e:AnyRef, size:Int = DEFAULT_SIZE):Icon = {
		if (iconCache.isDefinedAt( ((e, size)) )) {
			iconCache( ((e, size)) )
		} else {
			val retVal = e match {
				case None => unknownIcon(size)
				case Some(e:AnyRef) => makeIconFor(e, size)
				case e:Element   => makeSVGIcon("/com/rayrobdod/glyphs/elements/"
						+ e.name.toLowerCase + ".svg", size)
				case e:Direction => makeSVGIcon("/com/rayrobdod/glyphs/direction/"
						+ e.name.toLowerCase + ".svg", size)
				case e:Status    => if(Statuses.Normal == e) {
						makeSVGIcon("/com/rayrobdod/glyphs/unknown.svg", size)
					} else {
						makeSVGIcon("/com/rayrobdod/glyphs/status/"
								+ e.name.toLowerCase + ".svg", size)
					}
				case e:Weaponkind => makeSVGIcon("/com/rayrobdod/glyphs/weapon/"
						+ e.name.toLowerCase.dropRight(4) + ".svg", size)
				
				case _ => unknownIcon(size)
			}
			iconCache = iconCache + ((((e, size)), retVal));
			retVal
		}
	}
	
	/**
	 * To load a resource and create an icon using SVGSalamander
	 * @version 2013 Jun 14
	 */
	private def makeSVGIcon(resource:String, size:Int):Icon = {
		if (resource == null) throw new NullPointerException("resource")
		if (this.getClass().getResource(resource) == null) throw new NullPointerException("this.getClass().getResource(\"" + resource + "\")")
		val uri = this.getClass().getResource(resource).toURI
		if (uri == null) throw new NullPointerException("uri")
		
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
		val (path:String, frac:String) = resource.split('#').to[Seq] match {
			case Seq(a, b) => (a, b)
			case Seq(a)    => (a, "")
		}
		val fracMatcher = xywhPattern.matcher(frac)
		val fracRect = if (fracMatcher.matches) {
			Some(( fracMatcher.group(1).toInt, fracMatcher.group(2).toInt,
				fracMatcher.group(3).toInt, fracMatcher.group(4).toInt ))
		} else { None }
		
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
	 * @version a.6.0
	 */
	def generateGenericIcon(atkElement:Option[Element], atkWeapon:Option[Weaponkind]):Icon =
	{
		val fileName = atkWeapon.map{genericTokenClassFile(_)}.getOrElse(
				"/com/rayrobdod/deductionTactics/tokenClasses/sprites/generic/Gray shirt.png")
		val base = ImageIO.read(this.getClass().getResource(fileName))
		
		atkElement.foreach{(elem:Element) => 
			(0 until base.getWidth).foreach{(x:Int) => 
				(0 until base.getHeight).foreach{(y:Int) => 
					if (base.getRGB(x,y) == 0xFF949494) {base.setRGB(x,y,elementToColor(elem).brighter.getRGB)}
					if (base.getRGB(x,y) == 0xFF7F7F7F) {base.setRGB(x,y,elementToColor(elem).getRGB)}
					if (base.getRGB(x,y) == 0xFF747474) {base.setRGB(x,y,elementToColor(elem).darker.getRGB)}
				}
			}
		}
		
		val returnValue = new ImageIcon(base)
		returnValue
	}
	
	
	
	/**
	 * @version a.6.0
	 */
	val tokenClassNameToIcon:Map[String, Icon] = Map.empty
	
	/**
	 * @since a.6.0
	 */
	def tokenClassToIcon(tokenClass:TokenClass):Icon = {
		tokenClassNameToIcon.getOrElse(tokenClass.name,
				generateGenericIcon(Option(tokenClass.atkElement), Option(tokenClass.atkWeapon)))
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
	
	
	val tilesheets:Seq[RectangularTilesheet] = {
		def readResource(name:String):RectangularTilesheet = {
			val baseUrl = this.getClass.getClassLoader.getResource(name)
			if (baseUrl == null) {throw new IllegalStateException("Resource missing")}
			var asStream:java.io.Reader = new java.io.StringReader("")
			try {
				asStream = new java.io.InputStreamReader(this.getClass.getClassLoader.getResourceAsStream(name), UTF_8)
				val builder = Swing.VisualizationRuleBasedRectangularTilesheetBuilder(baseUrl, SpaceClassMatcherFactory)
				val parser = new com.rayrobdod.json.parser.JsonParser
				
				parser.parse(builder, asStream).fold(
					{x => x},
					{x:Nothing => x},
					{x => throw new java.text.ParseException(x.toString, 0)},
					{(x,extra) => throw new java.text.ParseException(x.toString, extra.charIndex)}
				)
			} finally {
				asStream.close()
			}
		}
		
		Seq(
			FieldChessTilesheet,
			readResource("com/rayrobdod/tilemaps/Castle Dungeon/rules.json"),
			readResource("com/rayrobdod/tilemaps/Field Contrast/rules.json")
		)
	}
	
	/**
	 * A ListModel of all tilesheets.
	 * @version a.6.0
	 */
	final val AvailibleTilesheetListModel:ListModel[RectangularTilesheet] = new ScalaSeqListModel(tilesheets)
	
	
	
	final class MyNameAndIcon(val name:String, val icon:Icon) extends NameAndIcon
	final val UnsetNameAndIcon:NameAndIcon = new MyNameAndIcon("Unset", unknownIcon())
	
	
	import com.rayrobdod.deductionTactics.BuildInfo
	/** @since a.5.2 */
	private val runningOn = "Running on Java; " + System.getProperty("java.vendor") + " " + System.getProperty("java.version")
	
	/**
	 * @since a.5.2
	 * @version a.6.0
	 */
	val aboutDialogString:String = {
		var reader:java.io.Reader = new java.io.StringReader("")
		try { 
			reader = new java.io.InputStreamReader(
				this.getClass().getResourceAsStream("/com/rayrobdod/deductionTactics/swingView/about.html"),
				UTF_8
			)
			val readAllChars:String = {
				val array = new Array[Char](2048)
				val len = reader.read(array)
				new String(array, 0, len)
			}
			java.text.MessageFormat.format(readAllChars, BuildInfo.name, BuildInfo.version, runningOn)
		} finally {
			reader.close()
		}
	}
	
	
	
	implicit class RectangularFieldOps(base:RectangularField[SpaceClass]) {
		def indexOfSpace(space:RectangularSpace[_]):Option[RectangularIndex] = {
			base.mapIndex{idx => ((base.space(idx) == Some(space), idx))}.find{_._1}.map{_._2}
		}
	}
	
}
