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
package swingView.game

import javax.swing.KeyStroke
import scala.collection.immutable.Map
import com.rayrobdod.boardGame.swingView.RectangularTilesheet
import com.rayrobdod.deductionTactics.swingView.{AvailibleTilesheetListModel, tilesheets}
import KeyboardActions.KeyboardAction


/**
 * A utility for storing and retrieving preferences related to the game view
 * 
 * Previuosly known as object BoardGamePanel
 * @since a.5.1
 * @version next
 */
object preferences {
	import java.util.prefs.Preferences;
	
	private val movementSpeedPrefsKey:String = "tokenMoveSpeed";
	private val tilesheetPrefsKey:String = "tilesheetIndex";
	private val keyboardPrefsKey:String = "keyboardKeys";
	
	private def myPrefs = try {
		Preferences.userNodeForPackage(this.getClass);
	} catch {
		case e:java.security.AccessControlException => NilPreferences
	}
	
	def movementSpeed:Int = {
		myPrefs.getInt( movementSpeedPrefsKey, 15 );
	}
	def movementSpeed_=(x:Int):Unit = {
		myPrefs.putInt( movementSpeedPrefsKey, x );
	}
	
	/* ... ... ... */
	def currentTilesheet:RectangularTilesheet[SpaceClass] = {
		val size = AvailibleTilesheetListModel.getSize()
		val pref = myPrefs.getInt(tilesheetPrefsKey, 0)
		
		AvailibleTilesheetListModel.getElementAt(
			math.min(size - 1, pref)
		)
	}
	
	def currentTilesheet_=(x:RectangularTilesheet[SpaceClass]):Unit = {
		val index = tilesheets.indexOf(x);
		myPrefs.putInt(tilesheetPrefsKey, index);
	}
	
	/* @since next */
	def inputMap:Map[KeyboardAction, KeyStroke] = {
		val keyPrefs = myPrefs.node(keyboardPrefsKey)
		
		Map.empty ++ KeyboardActions.values.map{(a:KeyboardAction) =>
			((a, KeyStroke.getKeyStroke(
					keyPrefs.getInt(a.name + "Code", a.defaultKey.getKeyCode),
					keyPrefs.getInt(a.name + "Mods", a.defaultKey.getModifiers)
			)))
		}
	}
	
	/* @since next */
	def inputMap_=(x:Map[KeyboardAction, KeyStroke]) = {
		val keyPrefs = myPrefs.node(keyboardPrefsKey)
		
		x.foreach({(a:KeyboardAction, s:KeyStroke) =>
			keyPrefs.putInt(a.name + "Code", s.getKeyCode)
			keyPrefs.putInt(a.name + "Mods", s.getModifiers)
		}.tupled)
	}
	
	
	
	private object NilPreferences extends java.util.prefs.AbstractPreferences(null, "") {
		def getSpi(key:String) = null
		def putSpi(key:String, value:String):Unit = {}
		def removeSpi(key:String):Unit = {}
		def removeNodeSpi():Unit = {}
		def keysSpi() = new Array(0)
		def childrenNamesSpi() = new Array(0)
		def flushSpi():Unit = {}
		def syncSpi():Unit = {}
		def childSpi(key:String) = NilPreferences
	}
}
