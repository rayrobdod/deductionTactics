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
package com.rayrobdod.deductionTactics.swingView.menuBar

import java.awt.event.{ActionListener, ActionEvent}
import java.awt.BorderLayout.{NORTH, SOUTH}
import javax.swing.{JMenuBar, JMenu, JMenuItem, JScrollPane, JDialog, JLabel, JTabbedPane}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever
}
import javax.swing.SwingUtilities.getWindowAncestor
import com.rayrobdod.deductionTactics.swingView.HumanSuspicionsPanel
import com.rayrobdod.deductionTactics.swingView.aboutDialogString

/**
 * @author Raymond Dodge
 * @version next
 */
class MenuBar() extends JMenuBar
{
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	this.add({ val a = new JMenu(resources.getString("fileMenu"))
		a.setMnemonic(resources.getString("fileMenuMnemonic").charAt(0))
		
		a.add(myMenuItem("newGameMenu", new ActionListener{
			def actionPerformed(e:ActionEvent):Unit = {
				com.rayrobdod.deductionTactics.main.Main.startNewGame
			}
		}))
		
		a.addSeparator()
		
		// TODO: disable menu item if Preferences are denied
		a.add(myMenuItem("optionsMenu", new ActionListener{
			def actionPerformed(e:ActionEvent):Unit = {
				val tabbedPane = new JTabbedPane()
				val appearancePanel = new AppearanceOptionsPanel
				val keyboardPanel = new KeyInputsOptionsPanel
				tabbedPane.add(appearancePanel, resources.getString("optionsAppearance"))
				tabbedPane.add(keyboardPanel, resources.getString("optionsKeyboard"))
				
				val result = javax.swing.JOptionPane.showOptionDialog(
						getWindowAncestor(MenuBar.this),
						tabbedPane,
						resources.getString("optionsFrameTitle"),
						javax.swing.JOptionPane.DEFAULT_OPTION,
						javax.swing.JOptionPane.PLAIN_MESSAGE,
						null,
						Array[Object](
							resources.getString("applyButton"),
							resources.getString("cancelButton")
						),
						resources.getString("applyButton")
				)
				
				if (result == 0) { // Apply selected
					appearancePanel.apply.actionPerformed(null)
					keyboardPanel.apply.actionPerformed(null)
				}
			}
		}))
		
		a.addSeparator()
		
		a.add(new com.rayrobdod.swing.ExitMenuItem)
		
		a;
	})
	
	this.add({ val a = new JMenu(resources.getString("helpMenu"))
		a.setMnemonic(resources.getString("helpMenuMnemonic").charAt(0))
		
		a.add(myMenuItem("classesAllMenu", new ActionListener{
			def actionPerformed(e:ActionEvent):Unit = {
				val classesComp = new AllKnownTokenClassesComponent()
				val classesPane = new JScrollPane(classesComp,
								scrollVerticalAsNeeded, scrollHorizontalNever)
				
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				frame.add(classesPane)
				frame.add(new TokenClassPanelTypeSelector(classesComp), SOUTH)
				frame.setTitle(resources.getString("classesAllFrameTitle"))
				frame.pack()
				frame.setVisible(true)
			}
		}))
		
		a.add(myMenuItem("classesFilterMenu", new ActionListener{
			def actionPerformed(e:ActionEvent):Unit = {
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				
				val display = new FilterKnownTokenClassesComponent()
				frame.add(new JScrollPane(display,
						scrollVerticalAsNeeded, scrollHorizontalNever))
					
				var filterClass = new com.rayrobdod.deductionTactics.ai.TokenClassSuspicion
				val filterPanel = new HumanSuspicionsPanel(filterClass)
				frame.add(filterPanel, NORTH)
				filterPanel.addUpdateListener{(update) =>
					filterClass = update(filterClass)
					display.applyFilter(filterClass)
					frame.validate()
				}
				frame.setTitle(resources.getString("classesFilterFrameTitle"))
				frame.setSize(250,600)
				frame.setVisible(true)
			}
		}))
		
		a.add(myMenuItem("elementsMenu", new ActionListener{
			def actionPerformed(e:ActionEvent):Unit = {
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				frame.add(new ElementPentagonReminderComponent)
				frame.setTitle(resources.getString("elementsFrameTitle"))
				frame.pack()
				frame.setVisible(true)
			}
		}))
		
		a.addSeparator() 
		
		a.add(myMenuItem("aboutMenu", new ActionListener{
			def actionPerformed(e:ActionEvent):Unit = {
				javax.swing.JOptionPane.showMessageDialog(
						getWindowAncestor(MenuBar.this),
						new JLabel(aboutDialogString),
						resources.getString("aboutFrameTitle"),
						javax.swing.JOptionPane.PLAIN_MESSAGE
				)
			}
		}))
		
		a;
	})
	
	private def myMenuItem(propertyName:String, action:ActionListener):JMenuItem =
	{
		val title = resources.getString(propertyName)
		val mnemonic = resources.getString(propertyName + "Mnemonic").charAt(0)
		
		val retVal = new JMenuItem(title)
		retVal.setMnemonic(mnemonic)
		retVal.addActionListener(action)
		retVal
	}
}
