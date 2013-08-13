package com.rayrobdod

// Icon Loading
import java.net.URL
import javax.swing.{ImageIcon, Icon}
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Image.{SCALE_SMOOTH => imageScaleSmooth}


import deductionTactics.Elements.Element
import scala.collection.mutable.{Map => MMap}

/**
 * classes for DeductionTactics
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
 * @version 19 Nov 2012 - adding parameter to generate field
 * @version 19 Nov 2012 - implementing placeUnits
 * @version 28 Nov 2012 - placeUnits and generateField removed; functionality now proived by Maps
 * @version 10 Dec 2012 - changed VERSION from a static string to being read from the MANIFEST.MF file
 */
package object deductionTactics
{
	/** Returns a formatted version number for this package based on a found MANIFEST.MF */
	def VERSION = {
		val v = java.lang.Package.getPackage("com.rayrobdod.deductionTactics").getImplementationVersion();
		
		// Manifest doesn't like alpha chars in version numbers
		if (v != null) {
			if (v.take(8) == "000.010.")
				"a" + v.drop(7);
			else if (v.take(8) == "000.011.")
				"b" + v.drop(7);
			else
				v;
		} else "Unversioned";
	}
	
	def TITLE = "Deduction Tactics" //java.lang.Package.getPackage("com.rayrobdod.deductionTactics").getImplementationTitle();
		
	@deprecated("Getting rid of icons in model data", "2013 Jun 12")
	private val ICON_DIMENSION = 32
	
	// Icon loading
	/**
	 * Turns an SVG file at the given URL into an icon
	 */
	@deprecated("Getting rid of icons in model data", "2013 Jun 12")
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
	 * @todo use a reader and reader options
	 */
	@deprecated("Getting rid of icons in model data", "2013 Jun 12")
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
	@deprecated("Getting rid of icons in model data", "2013 Jun 12")
	def loadIcon(url:URL):Icon = 
	{
		url.getPath.split('.').last match
		{
			case "svg" => loadSVGIcon(url)
			case "png" => loadPNGIcon(url)
		}
	}
	
	@deprecated("Getting rid of icons in model data", "2013 Jun 12")
	private[this] val genericIconCache = MMap.empty[TokenClass, ImageIcon]
	/**
	 * Creates an undetailed icon which matches some of the traits of the TokenClass
	 */
	@deprecated("Getting rid of icons in model data", "2013 Jun 12")
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
