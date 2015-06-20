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
package com.rayrobdod.deductionTactics.swingView.game

import com.rayrobdod.deductionTactics._
import javax.swing.{JPanel, JScrollPane, WindowConstants}
import java.awt.{BorderLayout, GridLayout}
import java.awt.event.{MouseEvent, MouseListener}
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}
import com.rayrobdod.boardGame.swingView.{RectangularTilesheet, RectangularFieldComponent}
import com.rayrobdod.deductionTactics.swingView.{TokenClassList, tokenClassToIcon, TokenClassPanel, generateGenericIcon, AvailibleTilesheetListModel, tilesheets}
import scala.collection.immutable.Seq
import javax.swing.event.{AncestorListener, AncestorEvent}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_AS_NEEDED => scrollHorizontalAsNeeded
}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.{BoxLayout, Icon}
import javax.swing.{JFrame, JToolTip}
import java.text.MessageFormat


/**
 * @author Raymond Dodge
 * @version a.6.0
 */
class Top(tokens:ListOfTokens, playerNumber:Int, val field:RectangularField[SpaceClass]) {
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(MessageFormat.format(resources.getString("playGameFrameTitle"), playerNumber:java.lang.Integer))
	
	private val centerpiece = {
		val rv = new JPanel(new com.rayrobdod.swing.layouts.LayeredLayout)
		val tilesheet = BoardGamePanel.currentTilesheet
		
		val fieldLayers = RectangularFieldComponent(field, tilesheet)
		val tokenLayer = new TokenLayer
		val highlightLayer = new HighlightMovableSpacesLayer(fieldLayers._2)
		val cursorLayer = new CursorLayer(fieldLayers._2)
		
		field.keySet.foreach{x =>
			fieldLayers._2.addMouseListener(x, new MouseListener() {
				def mouseEntered(e:MouseEvent):Unit  = {}
				def mouseExited(e:MouseEvent):Unit   = {}
				def mousePressed(e:MouseEvent):Unit  = {}
				def mouseReleased(e:MouseEvent):Unit = {}
				
				def mouseClicked(e:MouseEvent):Unit = {
					cursorLayer.update(x)
				}
			})
		}
		
		rv.add(cursorLayer)
		rv.add(highlightLayer)
		rv.add(fieldLayers._2)
		rv.add(tokenLayer)
		rv.add(fieldLayers._1)
		rv
	}
	
	frame.add(centerpiece)
	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
	frame.pack()
	
	/*
	override def createToolTip():JToolTip = {
		val retVal = new JToolTip() {
			this.setUI(TokenClassList.MyToolTipUI)
			override def setTipText(tipText:String) {
				this.removeAll();
				val a = new TokenClassPanel(dataModel.getElementAt(HoveringIndexMouseListener.index));
				a.doLayout();
				a.setBackground(new java.awt.Color(0, true))
				this.add(a);
			}
		}
		retVal.setComponent(this);
		retVal.setLayout(new java.awt.BorderLayout())
		retVal;
	}
	
	this.setToolTipText("asdfghjkl");
	*/
	
	
	def setVisible(visible:Boolean) = {
		frame.setVisible(visible);
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
		Preferences.userNodeForPackage(this.getClass);
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
		def getSpi(key:String) = null
		def putSpi(key:String, value:String) {}
		def removeSpi(key:String) {}
		def removeNodeSpi() {}
		def keysSpi() = new Array(0)
		def childrenNamesSpi() = new Array(0)
		def flushSpi() {}
		def syncSpi() {}
		def childSpi(key:String) = NilPreferences
	}
}

object Top {
	type NextListener = Function3[Seq[PlayerAI], String, Seq[Seq[(Int,Int)]], Unit]
	
	def main(args:Array[String]):Unit = {
		val field = RectangularField(Seq.fill(7,7){FreePassageSpaceClass.apply})
		val tokens = new ListOfTokens(Seq(Seq(
			new Token(
				tokenClass = Some(new TokenClassBlunt(
					name = "Flaming Spearman",
					body = BodyTypes.Humanoid,
					atkElement = Elements.Fire,
					atkWeapon = Weaponkinds.Spearkind,
					atkStatus = Statuses.Blind,
					range = 1,
					speed = 3,
					weakDirection = Directions.Left,
					weakWeapon = Map(
						Weaponkinds.Bladekind -> .75f,
						Weaponkinds.Bluntkind -> 2f,
						Weaponkinds.Spearkind -> 1f,
						Weaponkinds.Whipkind -> 1.5f,
						Weaponkinds.Powderkind -> .5f
					),
					weakStatus = Statuses.Sleep
				)),
				currentSpace = field((0,0))
			)
		)))
		
		val t = new Top(tokens, 0, field);
		t.setVisible(true);
	}
}
