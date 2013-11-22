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
package com.rayrobdod


/**
 * classes for DeductionTactics
 * @author Raymond Dodge
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
	
	/** A title for use in About dialogs */
	def TITLE = "Deduction Tactics" //java.lang.Package.getPackage("com.rayrobdod.deductionTactics").getImplementationTitle();
	
	
	/** Creates a valid JSON string representing the specified TokenClass. */
	def tokenClassToJSON(tclass:CannonicalTokenClass):String = {
		val writer = new java.io.StringWriter();
		
		writer.write("{\"name\":\"");
		writer.write(tclass.name);
		
		writer.write("\",\"element\":\"");
		writer.write(tclass.atkElement.get.name);
		
		writer.write("\",\"atkWeapon\":\"");
		writer.write(tclass.atkWeapon.get.name.dropRight(4));
		
		writer.write("\",\"atkStatus\":\"");
		writer.write(tclass.atkStatus.get.name)
		
		writer.write("\",\"body\":\"");
		// writer.write(tclass.body.get.name)
		writer.write("Human");
		
		writer.write("\",\"range\":");
		writer.write(tclass.range.get.toString)
		
		writer.write(",\"speed\":");
		writer.write(tclass.speed.get.toString)
		
		writer.write(",\"weakStatus\":\"");
		writer.write(tclass.weakStatus.get.name)
		
		writer.write("\",\"weakDirection\":\"");
		writer.write(tclass.weakDirection.get.name)
		
		writer.write("\",\"weakWeapon\":{");
		val weakWeapon = tclass.weakWeapon.foldLeft(new java.lang.StringBuilder){(a,b) => 
			a.append(",\"")
			a.append(b._1.name.dropRight(4))
			a.append("\":")
			a.append(b._2.get)
		}.toString.tail
		writer.write(weakWeapon)
		
		writer.write("}}");
		
		writer.toString();
	}
}
