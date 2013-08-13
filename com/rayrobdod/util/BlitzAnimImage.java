package com.rayrobdod.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This is an image like the <a href="http://www.blitzbasic.com/bpdocs/command.php?name=LoadAnimImage">
 * AnimImage found in BlitzBasic</a>, which loads an image in equal-sized parts
 * 
 * @author Raymond Dodge
 * @version Feb 7, 2010
 * @version 16 Dec 2011 - moved from net.verizon.rayrobdod.util to com.rayrobdod.util
 */
public class BlitzAnimImage
{
	private final BufferedImage[] images;
	private final int offset;
	
	/**
	 * Loads a single image that is made up of 'frames' of seperate images
	 * 
	 * @param filename the file of the image
	 * @param width the width of each frame
	 * @param height the height of each frame
	 * @param first which frame should be called zero.
	 * @param count the number of frames to load
	 * @throws IOException if the image did not load correctly
	 * @throws NullPointerException if the filename is null
	 * @throws IllegalArgumentException if width, height, first or count are incompatable
	 * 			with the image.
	 */
	public BlitzAnimImage(File filename, int width, int height, int first, int count)
			throws IOException, NullPointerException, IllegalArgumentException
	{
		if (filename == null) throw new NullPointerException("The file was null");
		
		BufferedImage image = ImageIO.read(filename);
		
		int framesX = image.getWidth() / width;
		int framesY = image.getHeight()/ height;
		
		if (framesX == 0 || framesY == 0)
		{
			throw new IllegalArgumentException("width and height must be less than the" +
					"image's width and height");
		}
		if (framesX * framesY < count)
		{
			System.err.println("File: " + filename.getAbsolutePath() );
			System.err.println("framesX: " + framesX + "; framesY: " + framesY + "; count: " + count);
			throw new IllegalArgumentException("image does not have enough frames for" +
					" the count value.");
		}
		
		offset = first;
		images = new BufferedImage[count];
		for (int i = 0; i < count; i++)
		{
			int frameX = i % framesX;
			int frameY = i / framesX;
			
			images[i] = image.getSubimage(frameX * width, frameY * height, width, height);
		}
	}
	
	/**
	 * Loads a single image that is made up of 'frames' of seperate images
	 * 
	 * @param image the image
	 * @param width the width of each frame
	 * @param height the height of each frame
	 * @param first which frame should be called zero.
	 * @param count the number of frames to load
	 * @throws IOException if the image did not load correctly
	 * @throws NullPointerException if the filename is null
	 * @throws IllegalArgumentException if width, height, first or count are incompatable
	 * 			with the image.
	 */
	public BlitzAnimImage(BufferedImage image, int width, int height, int first, int count)
			throws IOException, NullPointerException, IllegalArgumentException
	{
		if (image == null) throw new NullPointerException("The image was null");
		
		int framesX = image.getWidth(null) / width;
		int framesY = image.getHeight(null)/ height;
		
		if (framesX == 0 || framesY == 0)
		{
			throw new IllegalArgumentException("width and height must be less than the" +
					"image's width and height");
		}
		if (framesX * framesY < count)
		{
			System.err.print("framesX: " + framesX + "; framesY: " + framesY + "; count: " + count);
			throw new IllegalArgumentException("image does not have enough frames for" +
					" the count value.");
		}
		
		offset = first;
		images = new BufferedImage[count];
		for (int i = 0; i < count; i++)
		{
			int frameX = i % framesX;
			int frameY = i / framesX;
			
			images[i] = image.getSubimage(frameX * width, frameY * height, width, height);
		}
	}
	
	/**
	 * creates a BlitzAnimImage with a mask
	 * @param image the image
	 * @param width the width of each frame
	 * @param height the height of each frame
	 * @param first which frame should be called zero.
	 * @param count the number of frames to load
	 * @param mask the mask color
	 * @throws IOException if the image did not load correctly
	 * @throws NullPointerException if the filename is null
	 * @throws IllegalArgumentException if width, height, first or count are incompatable
	 * 			with the image.
	 */
	public BlitzAnimImage(BufferedImage image, int width, int height, int first, int count, Color mask) throws NullPointerException, IllegalArgumentException, IOException
	{
		this(image, width, height, first, count);
		this.setMask(mask);
	}
	
	/**
	 * creates a BlitzAnimImage with a mask
	 * @param file the image's file
	 * @param width the width of each frame
	 * @param height the height of each frame
	 * @param first which frame should be called zero.
	 * @param count the number of frames to load
	 * @param mask the mask color
	 * @throws IOException if the image did not load correctly
	 * @throws NullPointerException if the filename is null
	 * @throws IllegalArgumentException if width, height, first or count are incompatable
	 * 			with the image.
	 */
	public BlitzAnimImage(File file, int width, int height, int first, int count, Color mask) throws NullPointerException, IllegalArgumentException, IOException
	{
		this(file, width, height, first, count);
		this.setMask(mask);
	}

	/**
	 * Sets a color to be transparent in the image
	 * @param color the color to be transparent
	 */
	public void setMask(Color color)
	{
		for (int i = 0; i < images.length; i++)
		{
			BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
			BufferedImage imageOriginal = images[i];
			
			for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
			{
				if (imageOriginal.getRGB(x, y) == color.getRGB())
				{
					image.setRGB(x, y, new Color(color.getRed(),
							color.getGreen(), color.getBlue(), 0).getRGB());
				}
				else
				{
					image.setRGB(x, y, imageOriginal.getRGB(x, y));
				}
			}
			
			images[i] = image;
		}
	}

	/**
	 * returns the iamge specified by the index
	 * @param index the index of the image
	 * @return the image corresponding to the offset
	 * @throws IndexOutOfBoundsException if <code>!(-getOffset() < index && index < 
	 * 		size() - getOffset())</code>
	 */
	public BufferedImage getFrame(int index) throws IndexOutOfBoundsException
	{
		return images[index + offset];
	}
	
	/**
	 * returns the number of images
	 * @return the number of images
	 */
	public int size()
	{
		return images.length;
	}
	
	/**
	 * returns the offset
	 * @return the offset
	 */
	public int getOffset()
	{
		return offset;
	}
	
	/**
	 * returns the images' width
	 * @return the images' width
	 */
	public int getWidth()
	{
		return images[0].getWidth();
	}
	
	/**
	 * returns the images' height
	 * @return the images' height
	 */
	public int getHeight()
	{
		return images[0].getHeight();
	}
	
	/**
	 * returns the images this split the original image into
	 * @return the images this split the original image into
	 */
	public BufferedImage[] getImages()
	{
		return images;
	}
}
