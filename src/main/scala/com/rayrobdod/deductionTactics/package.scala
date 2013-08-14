package com.rayrobdod


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
 * @version 2013 Aug 07 - dropping image-related functions
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
	
}