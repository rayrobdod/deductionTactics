package com.rayrobdod.deductionTactics.view

import javax.swing.{JMenuBar, JMenu, JMenuItem, JFrame, JScrollPane, JDialog}
import java.awt.event.{ActionListener, ActionEvent}
import java.awt.event.{FocusAdapter, FocusEvent}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import javax.swing.KeyStroke.{getKeyStroke => KeyStroke}
import javax.swing.SwingUtilities.getWindowAncestor
import com.rayrobdod.deductionTactics.SuspicionsTokenClass
import java.awt.BorderLayout.{NORTH, SOUTH}
		
/**
 * @author Raymond Dodge
 * @version 12 Feb 2012
 * @version 13 Feb 2012 - discovered some useful things in {@link javax.swing.SwingUtilities}, so this doesn't need parameters anymore
 * @version 20 Apr 2012 - Adding action to new game
 * @version 20 Apr 2012 - reducing repetion by creating MyMenuItem and adding those instead of having each MenuItem be a new anonymous class
 * @version 14 Aug 2012 - modifying the "View Classes…" frame to include a TokenClassPanelTypeSelector
 */
class MenuBar() extends JMenuBar
{
	this.add(new JMenu("Game"){
		setMnemonic('g')
		
		add(new MyMenuItem("New Game", 'n', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				com.rayrobdod.deductionTactics.main.Main.startNewGame
			}
		}))
		
		addSeparator()
		
		add(new MyMenuItem("Options…", 'o', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val dialog:JDialog = new OptionsDialog(getWindowAncestor(MenuBar.this))
				dialog.setVisible(true)
			}
		}))
		
		addSeparator()
		
		add(new com.rayrobdod.swing.ExitMenuItem)
			
		
	})
	
	this.add(new JMenu("Help"){
		setMnemonic('h')
		
		add(new MyMenuItem("View Classes…", 'c', new ActionListener{
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
			})
		)
		add(new MyMenuItem("Filter Classes…", 'f', new ActionListener{
				def actionPerformed(e:ActionEvent) = {
					val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this)) {
						val display = new FilterKnownTokenClassesComponent()
						add(new JScrollPane(display,
							scrollVerticalAsNeeded, scrollHorizontalNever))
						
						val filterClass = new SuspicionsTokenClass
						add(new HumanSuspicionsPanel(filterClass), NORTH)
						
						addFocusListener(new FocusAdapter(){
							override def focusGained(e:FocusEvent) {
								display.filter(filterClass)
							}
						})
					}
					frame.setTitle("FilterKnownTokenClassesComponentTest")
					frame.setSize(250,600)
					frame.setVisible(true)
				}
			})
		)
		add(new MyMenuItem("View Elements…", 'e', new ActionListener{
				def actionPerformed(e:ActionEvent) = {
					val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this)) {
						add(new ElementPentagonReminderComponent)
					}
					frame.setTitle("Element Pentagon - Deduction Tactics")
					frame.pack()
					frame.setVisible(true)
				}
			})
		)
		
		addSeparator() 
		
		add(new MyMenuItem("About…", 'a', new ActionListener{
				def actionPerformed(e:ActionEvent) = {
					val dialog:JDialog = new AboutDialog(getWindowAncestor(MenuBar.this))
					dialog.setVisible(true)
				}
			})
		)
	})
	
	private class MyMenuItem(title:String, mnemonic:Char, action:ActionListener)
			extends JMenuItem(title)
	{
		setMnemonic(mnemonic)
		addActionListener(action)
	}
}
