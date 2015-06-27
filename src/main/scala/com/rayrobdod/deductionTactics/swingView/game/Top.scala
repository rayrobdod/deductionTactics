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
package swingView.game

import javax.swing.{SwingUtilities, JFrame, JPanel, JButton, WindowConstants}
import java.text.MessageFormat
import java.awt.event.{MouseEvent, MouseListener, MouseAdapter}
import java.awt.event.{ActionEvent, ActionListener}
import scala.collection.mutable.Buffer
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.swingView.{RectangularTilesheet, RectangularFieldComponent}
import com.rayrobdod.deductionTactics.swingView.{AvailibleTilesheetListModel, tilesheets}


/**
 * @author Raymond Dodge
 * @version a.6.0
 */
class Top(tokens:ListOfTokens, playerNumber:Int, val field:RectangularField[SpaceClass]) {
	import Top._
	
	private[this] val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	private[this] val frame = new JFrame(MessageFormat.format(resources.getString("playGameFrameTitle"), playerNumber:java.lang.Integer))
	
	private[this] val turnStartListeners:Buffer[StartOfTurnListener] = Buffer.empty
	private[this] val notificationListeners:Buffer[NotificationListener] = Buffer.empty
	private[this] val actionPerformedListeners:Buffer[ActionPerformedListener] = Buffer.empty
	private[this] var currentTokens:ListOfTokens = tokens
	private[this] var selectedTokenIndex:Option[TokenIndex] = None
	
	
	private[this] val centerpiece = {
		val rv = new JPanel(new com.rayrobdod.swing.layouts.LayeredLayout)
		val tilesheet = BoardGamePanel.currentTilesheet
		
		val fieldLayers = RectangularFieldComponent(field, tilesheet)
		val tokenLayer = new TokenLayer(field, fieldLayers._2)
		val highlightLayer = new HighlightMovableSpacesLayer(fieldLayers._2)
		val cursorLayer = new CursorLayer(fieldLayers._2.spaceBounds _)
		val pieMenuLayout = new PieMenuLayout
		val pieMenuLayer = new JPanel(pieMenuLayout) 
		
		def generateButton(resourceKey:String, action:GameState.Action):JButton = {
			val button = new JButton(resources.getString(resourceKey))
			button.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent):Unit = {
					actionPerformedListeners.foreach{f => f(action)}
					
					// deselect everything
					selectedTokenIndex = None
					cursorLayer.clear()
					highlightLayer.update(None, currentTokens, field)
					pieMenuLayer.removeAll()
				}
			})
			button
		}
		
		pieMenuLayer.setBackground(new java.awt.Color(0, true))
		tokenLayer.tokens = tokens
		fieldLayers._2.addMouseListener(new MouseAdapter() {
			override def mouseClicked(e:MouseEvent):Unit  = {
				pieMenuLayout.center = e.getPoint()
				pieMenuLayer.invalidate()
				pieMenuLayer.validate()
			}
		})
		field.keySet.foreach{x =>
			fieldLayers._2.addMouseListener(x, new MouseListener() {
				def mouseEntered(e:MouseEvent):Unit  = {}
				def mouseExited(e:MouseEvent):Unit   = {}
				def mousePressed(e:MouseEvent):Unit  = {}
				def mouseReleased(e:MouseEvent):Unit = {}
				
				def mouseClicked(e:MouseEvent):Unit  = {
					pieMenuLayer.removeAll()
					
					if (SwingUtilities.isRightMouseButton(e)) {
						// deselect everything
						selectedTokenIndex = None
						cursorLayer.clear()
						highlightLayer.update(None, currentTokens, field)
						
					} else if (SwingUtilities.isLeftMouseButton(e)) {
						cursorLayer.update(x)
						val tokenOnThisSpace:Option[Token] = currentTokens.aliveTokens.flatten.filter{_.currentSpace == field(x)}.headOption
						val tokenOnThisSpaceIndex:Option[TokenIndex] = tokenOnThisSpace.map{currentTokens.indexOf _}
						
						
						selectedTokenIndex = selectedTokenIndex.fold[Option[TokenIndex]]{
							// no token is selected
							
							tokenOnThisSpaceIndex.getOrElse{
								pieMenuLayer.add(generateButton("endTurnButton", GameState.EndOfTurn))
							}
							
							tokenOnThisSpaceIndex
						}{(index) =>
							if (index._1 == playerNumber) {
								// selected token is mine
								
								tokenOnThisSpace.fold{
									pieMenuLayer.add(generateButton("moveToButton", GameState.TokenMove(currentTokens.tokens(index), field(x))))
								}{t =>
									pieMenuLayer.add(generateButton("damageAttackButton", GameState.TokenAttackDamage(currentTokens.tokens(index), t)))
									pieMenuLayer.add(generateButton("statusAttackButton", GameState.TokenAttackStatus(currentTokens.tokens(index), t)))
								}
								
								
								selectedTokenIndex
								
							} else {
								// selected token is not mine
								tokenOnThisSpaceIndex
							}
						}
						
						val selectedToken:Option[Token] = selectedTokenIndex.map{currentTokens.tokens _}
						
						highlightLayer.update(selectedToken, currentTokens, field)
					} else {
						// ignore middle button clicks
					}
					pieMenuLayer.validate()
				}
			})
		}
		this.addNotificationListener{(a,gs) => 
			tokenLayer.tokens = gs.tokens
			currentTokens = gs.tokens
		}
		this.addTurnStartListener{(gs) =>
			tokenLayer.tokens = gs.tokens
			currentTokens = gs.tokens
		}
		
		rv.add(pieMenuLayer)
		rv.add(cursorLayer)
		rv.add(highlightLayer)
		rv.add(fieldLayers._2)
		rv.add(tokenLayer)
		rv.add(fieldLayers._1)
		rv
	}
	
	frame.add(centerpiece)
	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
	frame.setJMenuBar(new com.rayrobdod.deductionTactics.swingView.MenuBar)
	frame.pack()
	
	
	def setVisible(visible:Boolean):Unit = {
		frame.setVisible(visible);
	}
	
	/* * * OBVERVABLE * * */
	
	def addTurnStartListener(f:StartOfTurnListener):Unit = {
		turnStartListeners += f
	}
	def fireTurnStartListeners(gs:GameState):Unit = {
		turnStartListeners.foreach{f => f(gs)}
	}
	
	def addNotificationListener(f:NotificationListener):Unit = {
		notificationListeners += f
	}
	def fireNotificationListeners(res:GameState.Result, gs:GameState):Unit = {
		notificationListeners.foreach{f => f(res,gs)}
	}
	
	def addActionPerformedListener(f:ActionPerformedListener):Unit = {
		actionPerformedListeners += f
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
	type NotificationListener = Function2[GameState.Result,GameState,Unit]
	type StartOfTurnListener = Function1[GameState,Unit]
	type ActionPerformedListener = Function1[GameState.Action,Unit]
	
	
	def main(args:Array[String]):Unit = {
		val field = RectangularField(Seq.fill(7,7){FreePassageSpaceClass.apply})
		val tokens = new ListOfTokens(Seq(
			Seq(
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
					currentSpace = field((1,2))
				),
				new Token(
					canMoveThisTurn = 3,
					canAttackThisTurn = true,
					tokenClass = Some(new TokenClassBlunt(
						name = "Frosty Whipman",
						body = BodyTypes.Humanoid,
						atkElement = Elements.Frost,
						atkWeapon = Weaponkinds.Whipkind,
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
					currentSpace = field((6,3))
				)
			), Seq(
				Token( currentSpace = field((3,5)) ),
				Token( currentSpace = field((5,5)) )
			)
		))
		
		val t = new Top(tokens, 0, field);
		t.addActionPerformedListener(System.out.println _)
		t.setVisible(true);
	}
}
