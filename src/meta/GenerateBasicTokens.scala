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
package meta

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.{Value => BodyType}
import Directions.Direction

import com.rayrobdod.deductionTactics.tokenClassToJSON

import scala.collection.immutable.{Map, Seq}

import java.nio.file.FileSystems.{getDefault => defaultFileSystem, newFileSystem}
import scala.collection.JavaConversions.{iterableAsScalaIterable, mapAsJavaMap}
import java.nio.charset.StandardCharsets.UTF_8
import com.rayrobdod.deductionTactics.CannonicalTokenClass

import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.binaryJSON.BSONWriter
import java.io.{ByteArrayOutputStream, DataOutputStream}
import java.nio.file.{Path, Files}

/**
 * 
 * 
 * @author Raymond Dodge
 * @since a.5.3
 */
object GenerateBasicTokens
{
	case class ElementAttributes(unitName:String, atkStatus:Status)
	case class WeaponkindAttributes(unitName:String, weakStatus:Status, weakWeapon:Map[Weaponkind, Double])
	
	val elements = Map(
		Elements.Light    -> ElementAttributes(unitName = "Shining", atkStatus = Statuses.Blind),
		Elements.Electric -> ElementAttributes(unitName = "Static",  atkStatus = Statuses.Neuro),
		Elements.Fire     -> ElementAttributes(unitName = "Flaming", atkStatus = Statuses.Burn),
		Elements.Frost    -> ElementAttributes(unitName = "Frosty",  atkStatus = Statuses.Sleep),
		Elements.Sound    -> ElementAttributes(unitName = "Sonic",   atkStatus = Statuses.Confuse)
	)
	val weapons = Map(
		Weaponkinds.Bladekind  -> WeaponkindAttributes(unitName = "Swordsman", weakStatus = Statuses.Sleep,   weakWeapon = Map(Weaponkinds.Bladekind -> .5 ,Weaponkinds.Bluntkind -> .75,Weaponkinds.Spearkind -> 1.5,Weaponkinds.Whipkind -> 2  ,Weaponkinds.Powderkind -> 1  )),
		Weaponkinds.Bluntkind  -> WeaponkindAttributes(unitName = "Clubsman",  weakStatus = Statuses.Burn,    weakWeapon = Map(Weaponkinds.Bladekind -> 1.5,Weaponkinds.Bluntkind -> .5 ,Weaponkinds.Spearkind -> 2  ,Weaponkinds.Whipkind -> 1  ,Weaponkinds.Powderkind -> .75)),
		Weaponkinds.Spearkind  -> WeaponkindAttributes(unitName = "Pikeman",   weakStatus = Statuses.Blind,   weakWeapon = Map(Weaponkinds.Bladekind -> 2  ,Weaponkinds.Bluntkind -> 1  ,Weaponkinds.Spearkind -> .5 ,Weaponkinds.Whipkind -> .75,Weaponkinds.Powderkind -> 1.5)),
		Weaponkinds.Whipkind   -> WeaponkindAttributes(unitName = "Whipman",   weakStatus = Statuses.Confuse, weakWeapon = Map(Weaponkinds.Bladekind -> 1  ,Weaponkinds.Bluntkind -> 1.5,Weaponkinds.Spearkind -> .75,Weaponkinds.Whipkind -> .5 ,Weaponkinds.Powderkind -> 2  )),
		Weaponkinds.Powderkind -> WeaponkindAttributes(unitName = "Powderman", weakStatus = Statuses.Neuro,   weakWeapon = Map(Weaponkinds.Bladekind -> .75,Weaponkinds.Bluntkind -> 2  ,Weaponkinds.Spearkind -> 1  ,Weaponkinds.Whipkind -> 1.5,Weaponkinds.Powderkind -> .5 ))
	)
	val DontCare = new Direction(-1, "DontCare", {(a) => Some(a)})
	
	private val classes = {
		elements.map({( a:Element, b:ElementAttributes ) =>
		weapons.map({( c:Weaponkind, d:WeaponkindAttributes) =>
			
			new CannonicalTokenClassBlunt(
				name = b.unitName + " " + d.unitName,
				atkElement = Some(a),
				atkWeapon = Some(c),
				atkStatus = Some(b.atkStatus),
				weakWeapon = d.weakWeapon.mapValues{(x) => Some(x.toFloat)},
				weakStatus = Some(d.weakStatus),
			
				body = Some(BodyTypes.Humanoid),
				range = Some(1),
				speed = Some(3),
				weakDirection = Some(DontCare)
			)
		}.tupled)}.tupled).flatten
	}
	
	
	def compile(outPath:Path) = {
		
		
		
		
		val writer = Files.newBufferedWriter(outPath, UTF_8);
		writer.write('[')
		
		classes.zipWithIndex.foreach({(tclass:CannonicalTokenClass, index:Int) =>
			if (index != 0) writer.write(',')
			writer.write( tokenClassToJSON(tclass) )
		}.tupled)
		
		writer.write(']');
		writer.close();
	}
	
	def main(args:Array[String]) {
		val (outDir) = {
			var outDir:Option[Path] = Some(defaultFileSystem getPath """C:\Users\Raymond\AppData\Local\Temp\basic.json""")
		
			var i = 0
			while (i < args.length) {
				args(i) match {
					case "-d" => {
						outDir = Some(defaultFileSystem getPath args(i+1))
						i = i + 1;
					}
					case _ => {
						
					}
				}
				i = i + 1;
			}
			
			(outDir.get)
		}
		
		this.compile(outDir)
	}
}
	
