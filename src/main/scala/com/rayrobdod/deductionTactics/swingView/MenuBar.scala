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

import javax.swing.{JMenuBar, JMenu, JMenuItem, JFrame, JScrollPane, JDialog, JLabel}
import java.awt.event.{ActionListener, ActionEvent}
import java.awt.event.{FocusAdapter, FocusEvent}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever
}
import javax.swing.SwingUtilities.getWindowAncestor
import com.rayrobdod.deductionTactics.SuspicionsTokenClass
import java.awt.BorderLayout.{NORTH, SOUTH}
		
/**
 * @author Raymond Dodge
 * @version a.5.2
 */
class MenuBar() extends JMenuBar
{
	this.add({ val a = new JMenu("Game")
		a.setMnemonic('g')
		
		a.add(myMenuItem("New Game", 'n', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				com.rayrobdod.deductionTactics.main.Main.startNewGame
			}
		}))
		
		a.addSeparator()
		
		a.add(myMenuItem("Options…", 'o', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val dialog:JDialog = new OptionsDialog(getWindowAncestor(MenuBar.this))
				dialog.setVisible(true)
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
		
		a.add(myMenuItem("Filter Classes…", 'f', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				
				val display = new FilterKnownTokenClassesComponent()
				frame.add(new JScrollPane(display,
						scrollVerticalAsNeeded, scrollHorizontalNever))
					
				val filterClass = new SuspicionsTokenClass
				frame.add(new HumanSuspicionsPanel(filterClass), NORTH)
					
				frame.addFocusListener(new FocusAdapter(){
					override def focusGained(e:FocusEvent) {
						display.filter(filterClass)
					}
				})
				frame.setTitle("Filter Classes - Deduction Tactics")
				frame.setSize(250,600)
				frame.setVisible(true)
			}
		}))
		
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
						MenuBar.this,
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
