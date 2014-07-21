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
import javax.swing.{JPanel, JScrollPane}
import java.awt.{BorderLayout, GridLayout}
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}
import com.rayrobdod.boardGame.swingView.{RectangularFieldComponent => FieldComponent, RectangularTilesheet}
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


/**
 * @author Raymond Dodge
 * @version a.6.0
 */
class BoardGamePanel(tokens:ListOfTokens, playerNumber:Int, val field:RectangularField[SpaceClass]) extends JPanel
{
	setLayout(new BorderLayout)
	
	
	val centerpiece = 
	{
		val tilesheetInfo = BoardGamePanel.currentTilesheet
		new FieldComponent(tilesheetInfo,field)
	}
	
	
	/** @since a.6.0 */
	val tokenComps:Map[(Int, Int), TokenComponent] = {
		val a:Seq[((Int, Int), TokenComponent)] = tokens.tokens.zipWithIndex.flatMap({(ts:Seq[Token], i:Int) =>
			ts.zipWithIndex.map({(t:Token, j:Int) =>
				(( ((i, j)), new TokenComponent(
					centerpiece,
					t.tokenClass.map{(tc) =>
						tokenClassToIcon(tc)
					}.getOrElse{generateGenericIcon(None, None)}
				) ))
			}.tupled)
		}.tupled)
		a.toMap
	}
	
	tokenComps.values.foreach{centerpiece.tokenLayer.add(_)}
	this.addAncestorListener(new AncestorListener() {
		
		override def ancestorAdded(e:AncestorEvent) {
			tokenComps.foreach{(a) => a._2.moveToSpace(
				tokens.tokens(a._1._1)(a._1._2).currentSpace
			)}
			BoardGamePanel.this.removeAncestorListener(this)
		}
		
		override def ancestorMoved(e:AncestorEvent) {}  
		override def  ancestorRemoved (e:AncestorEvent) {}  
	})
	
	
	
	private var m_tokenPanels:Map[(Int, Int), TokenPanel] = Map.empty[(Int, Int), TokenPanel]
	def tokenPanels:Map[(Int, Int), TokenPanel] = this.m_tokenPanels
	private val gridColCount = tokenComps.map{_._1._2}.max + 1
	private val gridRowCount = tokenComps.map{_._1._1}.max + 1
	
	private val westPanel = new JPanel(new GridLayout(gridColCount, 1))
	private val eastPanel = new JPanel(new GridLayout(gridColCount, gridRowCount - 1))
	this.resetTokenPanels(tokens)
	
	// make so that pack doesn't cause a screen-consuming size
	private val westScrollPane = new JScrollPane(westPanel,
			scrollVerticalAlways, scrollHorizontalNever)
	westScrollPane.setPreferredSize(new java.awt.Dimension(westScrollPane.getPreferredSize().width + 5, 1))
	private val eastScrollPane = new JScrollPane(eastPanel,
			scrollVerticalAlways, scrollHorizontalAsNeeded)
	eastScrollPane.setPreferredSize(new java.awt.Dimension(westScrollPane.getPreferredSize().width + 5, 1))
	
	private val centerScrollPane = new JScrollPane(centerpiece,
			scrollVerticalAsNeeded, scrollHorizontalAsNeeded)
	
	this.add(centerScrollPane, BorderLayout.CENTER)
	this.add(westScrollPane, BorderLayout.WEST)
	this.add(eastScrollPane, BorderLayout.EAST)
	
	
	def resetTokenPanels(tokens:ListOfTokens) = {
		val a:Seq[((Int, Int), TokenPanel)] = tokens.tokens.zipWithIndex.flatMap({(ts:Seq[Token], i:Int) =>
			ts.zipWithIndex.map({(t:Token, j:Int) =>
				(( ((i, j)), new TokenPanel(t) ))
			}.tupled)
		}.tupled)
		m_tokenPanels = a.toMap
		
		westPanel.removeAll()
		eastPanel.removeAll()
		tokenPanels.toSeq.sortBy{(x) => x._1._2 * 256 + x._1._1}.foreach{(x) =>
			(if (x._1._1 == playerNumber) {westPanel} else {eastPanel}).add(x._2)
		}
		
		eastPanel.revalidate()
		westPanel.revalidate()
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
		Preferences.userNodeForPackage(classOf[BoardGamePanel]);
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
