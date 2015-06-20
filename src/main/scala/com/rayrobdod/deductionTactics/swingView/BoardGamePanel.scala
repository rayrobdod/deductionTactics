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
package com.rayrobdod.deductionTactics.swingView

import com.rayrobdod.deductionTactics._
import java.awt.Component
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}
import com.rayrobdod.boardGame.swingView.{RectangularTilesheet, RectangularTilemapComponent}
import scala.collection.immutable.Seq
import javax.swing.JPanel
import javax.swing.event.{AncestorListener, AncestorEvent}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_AS_NEEDED => scrollHorizontalAsNeeded
}
import com.rayrobdod.boardGame.swingView.RectangularFieldComponent


/**
 * @author Raymond Dodge
 * @version a.6.0
 */
class BoardGameViewModel(tokens:ListOfTokens, playerNumber:Int, val field:RectangularField[SpaceClass])
{
	import BoardGamePanel.currentTilesheet
	
	private val fieldLayers = RectangularFieldComponent.apply(field, currentTilesheet)
	
	val bottomFieldLayer:RectangularTilemapComponent = fieldLayers._1
	val topFieldLayer:RectangularTilemapComponent = fieldLayers._2
	val tokenLayer:TokenLayer = new TokenLayer
	val moveHilightLayer:HighlightMovableSpacesLayer = new HighlightMovableSpacesLayer(bottomFieldLayer)
//	val interactionLayer:InteractionLayer
	
	val comp:Component = {
		val a = new JPanel(new com.rayrobdod.swing.layouts.LayeredLayout)
		
		a.add(bottomFieldLayer)
		a.add(tokenLayer)
		a.add(topFieldLayer)
		a.add(moveHilightLayer)
	}
}

/**
 * constants used by the BoardGamePanel class
 * @author Raymond Dodge
 * @since a.5.1
 */
object BoardGamePanel {
	import java.util.prefs.Preferences;
	
	val movementSpeedPrefsKey:String = "tokenMoveSpeed";
	val tilesheetPrefsKey:String = "tilesheetIndex";
	private def myPrefs = try {
		Preferences.userNodeForPackage(classOf[BoardGameViewModel]);
	} catch {
		case e:java.security.AccessControlException => NilPreferences
	}
	
	def movementSpeed:Int = {
		myPrefs.getInt( movementSpeedPrefsKey, 15 );
	}
	def movementSpeed_=(x:Int) {
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
	
	def currentTilesheet_=(x:RectangularTilesheet[SpaceClass]) {
		val index = tilesheets.indexOf(x);
		myPrefs.putInt(tilesheetPrefsKey, index);
	}
	
	
	
	private object NilPreferences extends java.util.prefs.AbstractPreferences(null, "") {
		override def getSpi(key:String):String = null
		override def putSpi(key:String, value:String) {}
		override def removeSpi(key:String) {}
		override def removeNodeSpi() {}
		override def keysSpi():Array[String] = new Array(0)
		override def childrenNamesSpi():Array[String] = new Array(0)
		override def flushSpi() {}
		override def syncSpi() {}
		override def childSpi(key:String):NilPreferences.type = NilPreferences
	}
}
