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

import javax.swing.{JButton, JList, JPanel, ImageIcon,
		DefaultListModel, JScrollPane}
import javax.imageio.ImageIO
import java.awt.{BorderLayout, GridLayout}
import java.awt.event.{ActionListener, ActionEvent}
import java.io.{InputStreamReader}
import java.net.URL
import com.rayrobdod.commaSeparatedValues.parser.{CSVParser, ToSeqSeqCSVParseListener, CSVPatterns}
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.boardGame.{RectangularField, RectangularSpace}
import com.rayrobdod.boardGame.mapValuesFromObjectNameToSpaceClassConstructor
import com.rayrobdod.boardGame.swingView.FieldComponent
import com.rayrobdod.deductionTactics._
import scala.collection.immutable.Seq
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_AS_NEEDED => scrollHorizontalAsNeeded}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.BoxLayout


import com.rayrobdod.deductionTactics.swingView.{TokenComponent => DTTokenComponent}
import com.rayrobdod.boardGame.swingView.{TokenComponent => BGTokenComponent}

/**
 * @author Raymond Dodge
 * @version a.5.0
 */
class BoardGamePanel(tokens:ListOfTokens, val field:RectangularField) extends JPanel
{
	setLayout(new BorderLayout)
	
	def tokenLayer:JPanel = centerpiece.tokenLayer
	
	val centerpiece = 
	{
		import com.rayrobdod.swing.layouts.MoveToLayout
		
		val layout:MoveToLayout = Options.movementLayout
		val tilesheetInfo = Options.currentTilesheet
		
		val fieldComp = new FieldComponent(tilesheetInfo,field)
		fieldComp.tokenLayer.setLayout(layout)
		
		val tokenComponents = tokens.tokens.flatten.map{(t:Token) =>
			val comp = new DTTokenComponent(t, fieldComp, layout, tokens)
			
			t.diedReactions_+=(RemoveComponentUponDeathAct)
			object RemoveComponentUponDeathAct extends Function0[Unit] {
				override def apply() = {
						tokenLayer remove comp
						tokenLayer.repaint()
				}
			}
			
			comp
		}
		
		tokenComponents.foreach{fieldComp.tokenLayer.add(_)}
		
		fieldComp
	}
	
	val playerTokenLists = tokens.tokens.map{(onePlayersTokenList:Seq[Token]) =>
		val container = new JPanel()
		container.setLayout(new BoxLayout(container, boxYAxis))
		
		val tokenPanels = onePlayersTokenList.map{new TokenPanel(_)}
		tokenPanels.foreach{(t:TokenPanel) =>
			t.token match {
				case mt:MirrorToken => {
					t remove t.tokenClass
					t add (new HumanSuspicionsPanel(mt.tokenClass) {
							setBackground(null)
					})
				}
				case _ => {}
			}
		}
		tokenPanels.foreach{container add _}
		
		tokenPanels.foreach{(panel:TokenPanel) => 
			tokens.tokens.flatten.foreach{(token:Token) =>
				token.updateReactions_+=(panel.UpdateAct)
			}
		}
		
		container
	}
	
	val eastPanel = new JPanel(new GridLayout(1,playerTokenLists.length - 1))
	playerTokenLists.tail.foreach{eastPanel.add(_)}
	
	// make so that pack doesn't cause a screen-consuming size
	val westScrollPane = new JScrollPane(playerTokenLists(0),
			scrollVerticalAlways, scrollHorizontalNever)
	westScrollPane.setPreferredSize(new java.awt.Dimension(westScrollPane.getPreferredSize().width, 1))
	val eastScrollPane = new JScrollPane(eastPanel,
			scrollVerticalAlways, scrollHorizontalAsNeeded)
	eastScrollPane.setPreferredSize(new java.awt.Dimension(westScrollPane.getPreferredSize().width, 1))
	
	val centerScrollPane = new JScrollPane(centerpiece,
			scrollVerticalAsNeeded, scrollHorizontalAsNeeded)
	
	this.add(centerScrollPane, BorderLayout.CENTER)
	this.add(westScrollPane, BorderLayout.WEST)
	this.add(eastScrollPane, BorderLayout.EAST)
}
