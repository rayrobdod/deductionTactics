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

import javax.swing.{SwingUtilities, JFrame, JPanel, JButton, WindowConstants, KeyStroke, AbstractAction, JScrollPane}
import java.text.MessageFormat
import java.awt.event.{MouseEvent, MouseListener, MouseAdapter}
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.event.KeyEvent
import scala.collection.mutable.Buffer
import scala.collection.immutable.{Seq, Map}
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.swingView.{RectangularTilesheet, RectangularFieldComponent}
import com.rayrobdod.deductionTactics.swingView.{AvailibleTilesheetListModel, tilesheets, TokenPanel}


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
	private[this] val memoUpdates:Buffer[Function1[ai.Memo, ai.Memo]] = Buffer.empty
	
	private[this] var currentTokens:ListOfTokens = tokens
	private[this] var currentSuspicions:Map[TokenIndex, ai.TokenClassSuspicion] = Map.empty
	private[this] def afterUpdateSuspicions:Map[TokenIndex, ai.TokenClassSuspicion] = {
		val currentMemo:ai.Memo = new ai.SimpleMemo(suspicions = currentSuspicions)
		val updatedMemo = memoUpdates.foldLeft(currentMemo){(s,f) => f(s)}
		val updatedSusps = updatedMemo.suspicions
		updatedSusps
	}
	
	private[this] val tokenInfoPanel = new JPanel(new java.awt.BorderLayout)
	tokenInfoPanel.setPreferredSize({
		val a = new TokenPanel(new Token(field(0,0)), new ai.TokenClassSuspicion(), {x => })
		a.doLayout
		a.getPreferredSize
	})
	
	private[this] val centerpiece = {
		val rv = new JPanel(new com.rayrobdod.swing.layouts.LayeredLayout)
		val tilesheet = BoardGamePanel.currentTilesheet
		
		val fieldLayers = RectangularFieldComponent(field, tilesheet)
		val tokenLayer = new TokenLayer(field, fieldLayers._2)
		val highlightLayer = new HighlightMovableSpacesLayer(fieldLayers._2)
		val cursorLayer = new CursorLayer(fieldLayers._2.spaceBounds _)
		val pieMenuLayout = new PieMenuLayout
		val pieMenuLayer = new JPanel(pieMenuLayout)
		val spaceClassDisplay = new DisplaySpaceClassInfoInCorner
		
		val selectedSpace = new CurrentlySelectedSpaceProperty
		val selectedTokenIndex = new CurrentlySelectedTokenProperty
		
		val clearSelectionAction = new ClearSelectionAction(selectedSpace, selectedTokenIndex, pieMenuLayer)
		
		def generateButton(resourceKey:String, action:GameState.Action):JButton = {
			val button = new JButton(resources.getString(resourceKey))
			button.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent):Unit = {
					actionPerformedListeners.foreach{f => f(action)}
				}
			})
			button.addActionListener(clearSelectionAction)
			button
		}
		
		val selectAction = new SelectAction(
				selectedSpace.get _,
				{() => currentTokens},
				field,
				selectedTokenIndex,
				pieMenuLayer,
				generateButton _,
				playerNumber
		)
		
		pieMenuLayer.setBackground(new java.awt.Color(0, true))
		pieMenuLayer.setOpaque(false)
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
					
					if (SwingUtilities.isRightMouseButton(e)) {
						clearSelectionAction.actionPerformed(null)
						
					} else if (SwingUtilities.isLeftMouseButton(e)) {
						selectedSpace.set(x)
						selectAction.actionPerformed(null)
						
					} else {
						// ignore middle button clicks
					}
				}
			})
		}
		this.addNotificationListener{(a, gs, memo) => 
			tokenLayer.tokens = gs.tokens
			currentTokens = gs.tokens
			currentSuspicions = memo.suspicions
			memo
		}
		this.addTurnStartListener{(gs, memo) =>
			tokenLayer.tokens = gs.tokens
			currentTokens = gs.tokens
			currentSuspicions = memo.suspicions
		}
		selectedSpace.addChangeListener{x =>
			cursorLayer.update(x)
		}
		selectedTokenIndex.addChangeListener{x =>
			val selectedToken:Option[Token] = x.map{currentTokens.tokens _}
			val susp = x.map{afterUpdateSuspicions}
			highlightLayer.update(selectedToken, currentTokens, field, susp.flatMap{_.speed}.getOrElse(0), susp.flatMap{_.range}.getOrElse(0))
		}
		selectedSpace.addChangeListener{x =>
			val spaceClass = field(x).typeOfSpace
			val putInNorth = x._2 > (field.keySet.map{_._2}.max / 2)
			val putInWest  = x._1 > (field.keySet.map{_._1}.max / 2)
			val anchor = {
				(if (putInNorth == putInWest) {2} else {0}) +
				(if (putInWest) {16} else {12})
			}
			spaceClassDisplay.showDetailsOf(spaceClass, anchor)
		}
		selectedSpace.addChangeListener{x =>
			val tokenOnSpace:Option[Token] = currentTokens.tokens.flatten.find{_.currentSpace == field(x)}
			tokenInfoPanel.removeAll()
			tokenOnSpace.map{t =>
				val tokenIndex = currentTokens.indexOf(t)
				val susp = afterUpdateSuspicions(tokenIndex)
				val tp = new TokenPanel(t, susp, {x => memoUpdates += {y => y.updateSuspicion(tokenIndex, x(y.suspicions(tokenIndex)))}})
				tokenInfoPanel.add(tp)
				tokenInfoPanel.validate()
				tp.validate()
				tp.doLayout()
			}
			tokenInfoPanel.validate()
			tokenInfoPanel.repaint()
		}
		
		rv.setFocusable(true)
		val inputMap = rv.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
		val actionMap = rv.getActionMap()
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "MoveLeft")
		actionMap.put("MoveLeft", new MoveCursorAction("MoveLeft", {x => x.left.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._2.spaceBounds _))
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "MoveUp")
		actionMap.put("MoveUp", new MoveCursorAction("MoveUp", {x => x.up.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._2.spaceBounds _))
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "MoveRight")
		actionMap.put("MoveRight", new MoveCursorAction("MoveRight", {x => x.right.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._2.spaceBounds _))
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "MoveDown")
		actionMap.put("MoveDown", new MoveCursorAction("MoveDown", {x => x.down.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._2.spaceBounds _))
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "Select")
		actionMap.put("Select", selectAction)
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "Clear")
		actionMap.put("Clear", clearSelectionAction)
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "FindNextActionableToken")
		actionMap.put("FindNextActionableToken", new SelectNextActionableTokenAction(selectedSpace, selectedTokenIndex, {() => currentTokens}, field, playerNumber))
		
		rv.add(pieMenuLayer)
		rv.add(cursorLayer)
		rv.add(spaceClassDisplay.component)
		rv.add(highlightLayer)
		rv.add(fieldLayers._2)
		rv.add(tokenLayer)
		rv.add(fieldLayers._1)
		rv
	}
	
	frame.getContentPane.add(centerpiece)
	frame.getContentPane.add(tokenInfoPanel, java.awt.BorderLayout.EAST)
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
	def fireTurnStartListeners(gs:GameState, memo:ai.Memo):Unit = {
		turnStartListeners.foreach{f => f(gs, memo)}
	}
	
	def addNotificationListener(f:NotificationListener):Unit = {
		notificationListeners += f
	}
	def fireNotificationListeners(res:GameState.Result, gs:GameState, memo:ai.Memo):ai.Memo = {
		val retVal = notificationListeners.foldLeft(
				memoUpdates.foldLeft(memo){(m, f) => f(m)}
		){(m, f) => f(res,gs,m)}
		memoUpdates.clear()
		retVal
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
	type NotificationListener = Function3[GameState.Result,GameState,ai.Memo,ai.Memo]
	type StartOfTurnListener  = Function2[GameState,ai.Memo,Unit]
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
				),
				new Token(
					canMoveThisTurn = 1,
					canAttackThisTurn = true,
					currentSpace = field((5,0))
				)
			), Seq(
				Token( currentSpace = field((3,5)) ),
				Token( currentSpace = field((5,5)) )
			)
		))
		
		val t = new Top(tokens, 0, field);
		t.fireTurnStartListeners(
				new GameState(field, tokens),
				(new ai.SimpleMemo()).updateSuspicion((1,0), ai.TokenClassSuspicion(atkElement = Some(Elements.Fire)))
		)
		t.addActionPerformedListener(System.out.println _)
		t.setVisible(true);
	}
}
