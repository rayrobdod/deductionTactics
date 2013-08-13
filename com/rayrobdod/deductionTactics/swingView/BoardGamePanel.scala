package com.rayrobdod.deductionTactics.swingView

import javax.swing.{JButton, JList, JPanel, ImageIcon,
		DefaultListModel, JScrollPane}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
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
 * @version 19 Jan 2012
 * @version 14 Feb 2012 - playerTokenLists are now JPanels, not JLists.
 * @version 21 Mar 2012 - removed paramter from Died since it no longer exists
 * @version 30 May 2012 - making so that prefered size does not ignore the ability of things to scroll
 * @version 24 Jul 2012 - removing an unused code from fieldComp
 * @version 24 Jul 2012 - moving the centerpiece's layout to the Options
 * @version 28 Oct 2012 - changing imports from com.rayrobdod.boardGame.view to com.rayrobdod.boardGame.swingView
                          Also whatever changes need to happen to support the new archetecture
                          Includes removing centerpiece, renaming fieldComponent to centerpiece, removing tokenLayer
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 28 Nov 2012 - Now can show more than two players worth of tokens
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
		
		val tokenComponents = tokens.tokens.flatMap{_.map{(t:Token) =>
//			val comp = new BGTokenComponent(t, fieldComp, layout, t.tokenClass.icon)
			val comp = new DTTokenComponent(t, fieldComp, layout, tokens)
			
			t.reactions += RemoveComponentUponDeathAct
			object RemoveComponentUponDeathAct extends Reaction
			{
				override def apply(e:Event) = {e match {
					case Died() => 
					{
						tokenLayer remove comp
						tokenLayer.repaint()
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
				token.reactions += panel.UpdateAct
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
