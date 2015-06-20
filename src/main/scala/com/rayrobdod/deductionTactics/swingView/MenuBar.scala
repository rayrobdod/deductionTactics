/*
	Deduction Tactics
	Copyright (C) 2012-2014  Raymond Dodge

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

import javax.swing.{JMenuBar, JMenu, JMenuItem, JFrame, JScrollPane, JDialog, JLabel}
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.event.{ChangeListener, ChangeEvent}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever
}
import javax.swing.SwingUtilities.getWindowAncestor
import com.rayrobdod.deductionTactics.TokenClass
import com.rayrobdod.deductionTactics.{CannonicalTokenClassTemplate => TokenClassBuilder}
import java.awt.BorderLayout.{NORTH, SOUTH}
		
/**
 * @author Raymond Dodge
 * @version a.6.0
 */
class MenuBar() extends JMenuBar
{
	this.add({ val a = new JMenu("Game")
		a.setMnemonic('g')
		
		a.add(myMenuItem("New Game", 'n', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
		//		com.rayrobdod.deductionTactics.main.Main.startNewGame
			}
		}))
		
		a.addSeparator()
		
		// TODO: disable menu item if Preferences are denied
		a.add(myMenuItem("Options…", 'o', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val optionsPanel = new OptionsPanel
				
				val result = javax.swing.JOptionPane.showOptionDialog(
						getWindowAncestor(MenuBar.this),
						optionsPanel,
						"Options",
						javax.swing.JOptionPane.DEFAULT_OPTION,
						javax.swing.JOptionPane.PLAIN_MESSAGE,
						null,
						Array[Object](
							"Apply",
							"Cancel"
						),
						"Apply"
				)
				
				if (result == 0) { // Apply selected
					optionsPanel.apply.actionPerformed(null)
				}
			}
		}))
		
		a.addSeparator()
		
		a.add(new com.rayrobdod.swing.ExitMenuItem)
		
		a;
	})
	
	this.add({ val a = new JMenu("Help")
		a.setMnemonic('h')
		
		a.add(myMenuItem("View Classes…", 'c', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val classesComp = new AllKnownTokenClassesComponent()
				val classesPane = new JScrollPane(classesComp,
								scrollVerticalAsNeeded, scrollHorizontalNever)
				
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				frame.add(classesPane)
				frame.add(new TokenClassPanelTypeSelector(classesComp), SOUTH)
				frame.setTitle("Known Classes - Deduction Tactics")
				frame.pack()
				frame.setVisible(true)
			}
		}))
		
		/* a.add(myMenuItem("Filter Classes…", 'f', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				
				val display = new FilterKnownTokenClassesComponent()
				frame.add(new JScrollPane(display,
						scrollVerticalAsNeeded, scrollHorizontalNever))
					
				val filterClass = new TokenClassBuilder
				val filterPanel = new HumanSuspicionsPanel(filterClass)
				frame.add(filterPanel, NORTH)
				filterPanel.addChangeListener(new ChangeListener(){
					override def stateChanged(e:ChangeEvent) {
						display.filter(filterClass)
					}
				})
				frame.setTitle("Filter Classes - Deduction Tactics")
				frame.setSize(250,600)
				frame.setVisible(true)
			}
		})) */
		
		a.add(myMenuItem("View Elements…", 'e', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				frame.add(new ElementPentagonReminderComponent)
				frame.setTitle("Element Pentagon - Deduction Tactics")
				frame.pack()
				frame.setVisible(true)
			}
		}))
		
		a.addSeparator() 
		
		a.add(myMenuItem("About…", 'a', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				javax.swing.JOptionPane.showMessageDialog(
						getWindowAncestor(MenuBar.this),
						new JLabel(aboutDialogString),
						"About Deduction Tactics",
						javax.swing.JOptionPane.PLAIN_MESSAGE
				)
			}
		}))
		
		a;
	})
	
	private def myMenuItem(title:String, mnemonic:Char, action:ActionListener):JMenuItem =
	{
		val retVal = new JMenuItem(title)
		retVal.setMnemonic(mnemonic)
		retVal.addActionListener(action)
		retVal
	}
}
