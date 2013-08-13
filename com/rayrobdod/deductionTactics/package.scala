package com.rayrobdod

// Icon Loading
import java.net.URL
import javax.swing.{ImageIcon, Icon}
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Image.{SCALE_SMOOTH => imageScaleSmooth}

// Field Generation
import com.rayrobdod.commaSeparatedValues.parser.{CSVParser, ToSeqSeqCSVParseListener, CSVPatterns}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.mapValuesFromObjectNameToSpaceClassConstructor
import com.rayrobdod.boardGame.view.{FieldComponent, JSONTilesheet, TokenComponent}
import scala.collection.immutable.Seq
import java.io.InputStreamReader


import deductionTactics.Elements.Element

/**
 * An enumeration for the elements
 * @author Raymond Dodge
 * @version 22 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod
			to com.rayrobdod
 * @version 27 Feb 2012 - started generateGenericIcon
 * @version 15 Apr 2012 - figured out salamander's SVGICon, so loadSVGIcon works now
 * @version 20 Apr 2012 - modifying the location of a few resources
 * @version 03 Jun 2012 - Adding VERSION variable
 */
package object deductionTactics
{
	val VERSION = "a.2.1"
	private val ICON_DIMENSION = 32
	
	/**
	 * Turns an SVG file at the given URL into an icon
	 */
	def loadSVGIcon(url:URL):Icon =
	{
		import scala.swing.Swing.pair2Dimension
		import com.kitfox.svg.app.beans.SVGIcon
		
		val icon = new SVGIcon()
		icon.setSvgURI(url.toURI)
		icon.setPreferredSize( ((ICON_DIMENSION, ICON_DIMENSION)) )
		icon.setScaleToFit(true)
		icon.setAntiAlias(true)
		return icon
	}
	
	/**
	 * Turns an PNG file at the given URL into an icon
	 */
	def loadPNGIcon(url:URL):ImageIcon =
	{
		val image = ImageIO.read(url)
		val image32 = image.getScaledInstance(ICON_DIMENSION, ICON_DIMENSION, imageScaleSmooth)
		
		new ImageIcon(image32)
	}
	
	def loadIcon(url:URL):Icon = 
	{
		url.getPath.split('.').last match
		{
			case "svg" => loadSVGIcon(url)
			case "png" => loadPNGIcon(url)
		}
	}
	
	def generateField:RectangularField = {
		val letterToNameMapReader = new InputStreamReader(this.getClass().getResourceAsStream("/com/rayrobdod/tilemaps/Supermarket/letterMapping.json"))
		val letterToNameMap:Map[String,String] = {
			val listener = new ToSeqJSONParseListener()
			JSONParser.parse(listener, letterToNameMapReader)
			listener.resultMap.mapValues{_.toString}
		}
		val letterToSpaceClassConsMap = mapValuesFromObjectNameToSpaceClassConstructor(letterToNameMap)
		
		val fieldLetterReader = new InputStreamReader(this.getClass().getResourceAsStream("/com/rayrobdod/tilemaps/emptyField.csv"))

		val fieldLetterTable:Seq[Seq[String]] = {
			val listener = new ToSeqSeqCSVParseListener()
			new CSVParser(CSVPatterns.commaDelimeted).parse(listener, fieldLetterReader)
			listener.result
		}
		val fieldSpaceClassConsTable = fieldLetterTable.map{_.map{letterToSpaceClassConsMap}}
		
		RectangularField.applySCC(fieldSpaceClassConsTable)
	}
	
	def generateGenericIcon(tokenClass:TokenClass) =
	{
		val fileName = tokenClass.atkWeapon.map{_.genericTokenClassFile}.getOrElse("/sprites/generic/Gray shirt.png")
		//System.out.println(fileName)
		val base = ImageIO.read(this.getClass().getResource(fileName))
		
		tokenClass.atkElement.foreach{(elem:Element) => {
			(0 until base.getWidth).foreach{(x:Int) => {
				(0 until base.getHeight).foreach{(y:Int) => {
					if (base.getRGB(x,y) == 0xFF949494) {base.setRGB(x,y,elem.color.brighter.getRGB)}
					if (base.getRGB(x,y) == 0xFF7F7F7F) {base.setRGB(x,y,elem.color.getRGB)}
					if (base.getRGB(x,y) == 0xFF747474) {base.setRGB(x,y,elem.color.darker.getRGB)}
				}}
			}}
		}}
		
		new ImageIcon(base)
	}
}
