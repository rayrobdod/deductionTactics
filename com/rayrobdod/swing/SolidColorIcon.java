package com.rayrobdod.swing;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;

/**
 * @author Raymond Dodge
 * @version 11 Jun 2012
 * @version 28 Oct 2012 - implementing toString.
 */
public class SolidColorIcon implements Icon
{
	private final int width;
	private final int height;
	private final Color color;
	
	public SolidColorIcon(Color color, int width, int height)
	{
		this.color = color;
		this.width = width;
		this.height = height;
	}
	
	public int getIconWidth() {return width;}
	public int getIconHeight() {return height;}
	
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		g.setColor(color);
		g.fillRect(x, y, width, height);
	}
	
	public String toString() {
		return this.getClass().getName() + "[" +
		        color + ", " +
				"w=" + width + ", " +
				"h=" + height + "]";
	}
}
