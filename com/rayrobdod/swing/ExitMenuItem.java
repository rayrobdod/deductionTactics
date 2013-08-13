package com.rayrobdod.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;

/**
 * This is a standard Exit menu item that can be used ot exit a program.
 * 
 * Using this will call {@link System#exit(int)}. Do not use unless you
 * want this to be called. 
 * 
 * @author Raymond Dodge
 * @version Aug 1, 2010
 * @version Apr 21, 2011 - moved from <code>net.verizon.rayrobdod.util</code> to <code>net.verizon.rayrobdod.util</code>
 * @version 12 Feb 2012 - moved from net.verizon.rayrobdod.swing to com.rayrobdod.swing
 */
public class ExitMenuItem extends JMenuItem
{
	/**
	 * Creates an ExitMenuItem with default "Exit" text and X mnemonic
	 */
	public ExitMenuItem()
	{
		this("Exit", KeyEvent.VK_X);
	}
	
	/**
	 * Creates an ExitMenuItem with custom text and mnemonic
	 * 
	 * @param text the text to display
	 * @param mnemonic the key to set as a mnemonic.
	 */
	public ExitMenuItem(String text, int mnemonic)
	{
		super(text, mnemonic);
		this.addActionListener(new ExitActionListener());
		this.getAccessibleContext().setAccessibleDescription("Exit the program");
	}
	
	private class ExitActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			System.exit(0);
		}
	}
}
