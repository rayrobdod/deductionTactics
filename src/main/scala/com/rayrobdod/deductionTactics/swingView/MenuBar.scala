package com.rayrobdod.deductionTactics.swingView

import javax.swing.{JMenuBar, JMenu, JMenuItem, JFrame, JScrollPane, JDialog}
import java.awt.event.{ActionListener, ActionEvent}
import java.awt.event.{FocusAdapter, FocusEvent}
import javax.swing.ScrollPaneConstants.{
		VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever
}
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
 * @version 19 Nov 2012 - modifying the "Filter Classes…" frame's title
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 2013 Aug 19 - reducing number of anonymous inner classes
 */
class MenuBar() extends JMenuBar
{
	this.add({ val a = new JMenu("Game")
		a.setMnemonic('g')
		
		a.add(new MyMenuItem("New Game", 'n', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				com.rayrobdod.deductionTactics.main.Main.startNewGame
			}
		}))
		
		a.addSeparator()
		
		a.add(new MyMenuItem("Options…", 'o', new ActionListener{
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
		
		a.add(new MyMenuItem("View Classes…", 'c', new ActionListener{
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
		
		a.add(new MyMenuItem("Filter Classes…", 'f', new ActionListener{
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
		
		a.add(new MyMenuItem("View Elements…", 'e', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val frame:JDialog = new JDialog(getWindowAncestor(MenuBar.this))
				frame.add(new ElementPentagonReminderComponent)
				frame.setTitle("Element Pentagon - Deduction Tactics")
				frame.pack()
				frame.setVisible(true)
			}
		}))
		
		a.addSeparator() 
		
		a.add(new MyMenuItem("About…", 'a', new ActionListener{
			def actionPerformed(e:ActionEvent) = {
				val dialog:JDialog = new AboutDialog(getWindowAncestor(MenuBar.this))
				dialog.setVisible(true)
			}
		}))
		
		a;
	})
	
	private class MyMenuItem(title:String, mnemonic:Char, action:ActionListener)
			extends JMenuItem(title)
	{
		setMnemonic(mnemonic)
		addActionListener(action)
	}
}
