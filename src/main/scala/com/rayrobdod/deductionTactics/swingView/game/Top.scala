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

import javax.swing.{SwingUtilities, JFrame, JPanel, JButton, WindowConstants}
import java.text.MessageFormat
import java.awt.event.{MouseEvent, MouseListener, MouseAdapter}
import java.awt.event.{ActionEvent, ActionListener}
import scala.collection.mutable.Buffer
import scala.collection.immutable.{Seq, Map}
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.swingView.RectangularFieldComponent
import com.rayrobdod.deductionTactics.swingView.TokenPanel


/**
 * @author Raymond Dodge
 * @version next
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
		val tilesheet = preferences.currentTilesheet
		
		val fieldLayers = RectangularFieldComponent(field, tilesheet)
		val tokenLayer = new TokenLayer(field, fieldLayers._1)
		val highlightLayer = new HighlightMovableSpacesLayer(fieldLayers._1)
		val cursorLayer = new CursorLayer(fieldLayers._1.spaceBounds _)
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
		fieldLayers._1.addMouseListener(new MouseAdapter() {
			override def mouseClicked(e:MouseEvent):Unit  = {
				pieMenuLayout.center = e.getPoint()
				pieMenuLayer.invalidate()
				pieMenuLayer.validate()
			}
		})
		field.keySet.foreach{x =>
			fieldLayers._1.addMouseListener(x, new MouseListener() {
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
			val tokenOnSpace:Option[Token] = currentTokens.aliveTokens.flatten.find{_.currentSpace == field(x)}
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
		preferences.inputMap.foreach{x =>
			inputMap.put(x._2, x._1)
		}
		actionMap.put(KeyboardActions.MoveLeft, new MoveCursorAction("MoveLeft", {x => x.left.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._1.spaceBounds _))
		actionMap.put(KeyboardActions.MoveUp, new MoveCursorAction("MoveUp", {x => x.up.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._1.spaceBounds _))
		actionMap.put(KeyboardActions.MoveRight, new MoveCursorAction("MoveRight", {x => x.right.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._1.spaceBounds _))
		actionMap.put(KeyboardActions.MoveDown, new MoveCursorAction("MoveDown", {x => x.down.getOrElse(x)}, selectedSpace, field, pieMenuLayer, pieMenuLayout, fieldLayers._1.spaceBounds _))
		actionMap.put(KeyboardActions.Select, selectAction)
		actionMap.put(KeyboardActions.Clear, clearSelectionAction)
		actionMap.put(KeyboardActions.FindNextToken, new SelectNextActionableTokenAction(selectedSpace, selectedTokenIndex, {() => currentTokens}, field, playerNumber))
		
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
	frame.setJMenuBar(new com.rayrobdod.deductionTactics.swingView.menuBar.MenuBar)
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

object Top {
	type NotificationListener = Function3[GameState.Result,GameState,ai.Memo,ai.Memo]
	type StartOfTurnListener  = Function2[GameState,ai.Memo,Unit]
	type ActionPerformedListener = Function1[GameState.Action,Unit]
	
	
	def main(args:Array[String]):Unit = {
		val field = RectangularField(Seq.fill(7,7){FreePassageSpaceClass.apply})
		val tokens = new ListOfTokens(Seq(
			Seq(
				new Token(
					tokenClass = Some(new TokenClass(
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
						weakStatus = Statuses.Sleep,
						isSpy = false,
						stanceGroup = TokenClass.SingleStanceGroup
					)),
					currentSpace = field((1,2))
				),
				new Token(
					canMoveThisTurn = 3,
					canAttackThisTurn = true,
					tokenClass = Some(new TokenClass(
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
						weakStatus = Statuses.Sleep,
						isSpy = false,
						stanceGroup = TokenClass.SingleStanceGroup
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
