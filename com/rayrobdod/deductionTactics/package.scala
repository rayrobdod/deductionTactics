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
import scala.collection.mutable.{Map => MMap}

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
 * @version 28 Jun 2012 - adding a cache to generateGenericIcon
 * @version 14 Jul 2012 - moving a resource from /com/rayrobdod/tilemaps/Supermarket/letterMapping.json
 			to /com/rayrobdod/deductionTactics/letterMapping.json
 */
package object deductionTactics
{
	val VERSION = "a.3.2"
	private val ICON_DIMENSION = 32
	
	// Icon loading
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
	
	/**
	 * Turns an file at the given URL into an icon, based on the url's last
	 * filetype extension
	 */
	def loadIcon(url:URL):Icon = 
	{
		url.getPath.split('.').last match
		{
			case "svg" => loadSVGIcon(url)
			case "png" => loadPNGIcon(url)
		}
	}
	
	// Field Generation
	
	def generateField:RectangularField = {
		val letterToNameMapReader = new InputStreamReader(this.getClass().getResourceAsStream("/com/rayrobdod/deductionTactics/letterMapping.json"))
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
	
	
	private[this] val genericIconCache = MMap.empty[TokenClass, ImageIcon]
	/**
	 * Creates an undetailed icon which matches some of the traits of the TokenClass
	 */
	def generateGenericIcon(tokenClass:TokenClass) =
	{
		if (genericIconCache.contains(tokenClass))
		{
			genericIconCache(tokenClass)
		}
		else
		{
			val fileName = tokenClass.atkWeapon.map{_.genericTokenClassFile}.getOrElse(
					"/com/rayrobdod/deductionTactics/tokenClasses/sprites/generic/Gray shirt.png")
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
			
			val returnValue = new ImageIcon(base)
			genericIconCache += ((tokenClass, returnValue))
			returnValue
		}
	}
}
