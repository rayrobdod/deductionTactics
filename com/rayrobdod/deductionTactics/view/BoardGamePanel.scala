package com.rayrobdod.deductionTactics.view

import javax.swing.{JButton, JList, JPanel, ImageIcon,
		DefaultListModel, JScrollPane}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import javax.imageio.ImageIO
import java.awt.BorderLayout
import java.awt.event.{ActionListener, ActionEvent}
import java.io.{InputStreamReader}
import java.net.URL
import com.rayrobdod.commaSeparatedValues.parser.{CSVParser, ToSeqSeqCSVParseListener, CSVPatterns}
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.boardGame.RectangularField
import com.rayrobdod.boardGame.mapValuesFromObjectNameToSpaceClassConstructor
import com.rayrobdod.boardGame.view.{FieldComponent, JSONTilesheet}
import com.rayrobdod.deductionTactics._
import scala.collection.immutable.Seq
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.BoxLayout


import com.rayrobdod.deductionTactics.view.{TokenComponent => DTTokenComponent}
import com.rayrobdod.boardGame.view.{TokenComponent => BGTokenComponent}

/**
 * @author Raymond Dodge
 * @version 19 Jan 2012
 * @version 14 Feb 2012 - playerTokenLists are now JPanels, not JLists.
 * @version 21 Mar 2012 - removed paramter from Died since it no longer exists
 * @version 30 May 2012 - making so that prefered size does not ignore the ability of things to scroll
 */
class BoardGamePanel(tokens:ListOfTokens, val field:RectangularField) extends JPanel
{
	setLayout(new BorderLayout)
	
	val fieldComp = {
		val tilesheetInfoURL = this.getClass().getResource("/com/rayrobdod/tilemaps/GrassyField/info.json")
//		val tilesheetInfo = new JSONTilesheet(tilesheetInfoURL)
		val tilesheetInfo = Options.currentTilesheet
		
		new FieldComponent(tilesheetInfo,field)
	}
		
	val centerpiece = 
	{
		import com.rayrobdod.swing.layouts.{MoveToLayout,
				MoveToInstantLayout, MoveToGradualLayout2,
				MoveToGradualLayout, SequentialMoveToLayout
		}

		val layout:MoveToLayout = new MoveToGradualLayout(15)
		val panel = new JPanel(layout)
		
		val tokenComponents = tokens.tokens.flatMap{_.map{(t:Token) =>
//			val comp = new BGTokenComponent(t, fieldComp, layout, t.tokenClass.icon)
			val comp = new DTTokenComponent(t, fieldComp, layout, tokens)
			
			t.reactions += RemoveComponentUponDeathAct
			object RemoveComponentUponDeathAct extends Reaction
			{
				override def apply(e:Event) = {e match {
					case Died() => 
					{
						panel remove comp
						panel.repaint()
					}
					case _ => {}
				}}
				
				override def isDefinedAt(e:Event) = {e match {
					case Died() => true
					case _ => false
				}}
			}
			
			comp
		}}
		
		tokenComponents.foreach{panel.add(_)}
		panel.add(fieldComp)
		
		panel
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
				token.reactions += panel.UpdateAct
			}
		}
		
		container
	}
	
	// make so that pack doesn't cause a screen-consuming size
	val westScrollPane = new JScrollPane(playerTokenLists(0),
			scrollVerticalAlways, scrollHorizontalNever)
	westScrollPane.setPreferredSize(new java.awt.Dimension(westScrollPane.getPreferredSize().width, 1))
	val eastScrollPane = new JScrollPane(playerTokenLists(1),
			scrollVerticalAlways, scrollHorizontalNever)
	eastScrollPane.setPreferredSize(new java.awt.Dimension(eastScrollPane.getPreferredSize().width, 1))
	
	this.add(centerpiece, BorderLayout.CENTER)
	this.add(westScrollPane, BorderLayout.WEST)
	this.add(eastScrollPane, BorderLayout.EAST)
}
