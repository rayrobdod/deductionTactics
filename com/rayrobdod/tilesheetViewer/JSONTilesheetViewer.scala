package com.rayrobdod.jsonTilesheetViewer

import java.io.File
import java.net.{URL, URI}
import java.awt.BorderLayout
import javax.swing.{JFrame, JPanel, JTextField, JLabel, JButton, ImageIcon}
import java.awt.event.{ActionListener, ActionEvent, MouseAdapter, MouseEvent}
import javax.imageio.ImageIO
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.view.{FieldComponent, JSONTilesheet,
		NilTilesheet, Tilesheet, AnySpace, CheckerboardTilesheet}

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Path, Paths, Files}
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener

import scala.util.Random

/**
 * @author Raymond Dodge
 * @version 18 - 19 Aug 2011
 * @version 15 Apr 2012 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 19 Apr 2012 - put the fieldComp inside a JPanel so that there isn't the large gaps between tiles
 * @version 20 Apr 2012 - Adding CheckerboardTilesheet as an option
 * @version 20 Apr 2012 - Using URIs to maybe some effect, and giving Checkerboard a deconstructor so queries can effect it.
 * @version 24 Jun 2012 - Allowing specification of files via system notation ('C:\', '//') as well as the previous URI sintac ('file:')
 * @version 25 Jun 2012 - now responds to "TilesheetViewer::classes", making that the list of classes cycled between
 * @version 29 Jun 2012 - putting the urlbox on its own row; attempted to add rand parameter, but it doesn't actually do anything
 
 */
object JSONTilesheetViewer extends App
{
	val frame = new JFrame("JSON Tilesheet Viewer")
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	
	val url = if (args.size > 0) args(0).toString else "about:nil"
	val width = if (args.size > 1) args(1).toString else "10"
	val height = if (args.size > 2) args(2).toString else "12"
	val rand = if (args.size > 3) args(3).toString else ""
	
	val urlBox = new JTextField(url)
	val widthBox = new JTextField(width,2)
	val heightBox = new JTextField(height,2)
	val randBox = new JTextField(rand,5)
//	val goButton = new JButton(new ImageIcon(ImageIO.read(
//			this.getClass().getResource("GoIcon.bmp"))))
	val goButton = new JButton("Go")
	goButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent)
		{
			loadNewTilesheet()
		}
	})
	
	val paramPanel = new JPanel()
	paramPanel.add(new JLabel("dim=("))
	paramPanel.add(widthBox)
	paramPanel.add(new JLabel("X"))
	paramPanel.add(heightBox)
	paramPanel.add(new JLabel(")"))
	paramPanel.add(goButton)
	
	val navPanel = new JPanel()
	navPanel.setLayout(new BorderLayout())
	navPanel.add(paramPanel)
	navPanel.add(urlBox, BorderLayout.NORTH)
	
	var tilesheet:Tilesheet = null
	var field:RotateSpaceRectangularField = null
	var fieldComp:FieldComponent = null
	
	loadNewTilesheet()
	frame.setVisible(true)
	
	def loadNewTilesheet() = {
		val tilesheetURI = try {
			new URI(urlBox.getText)
		} catch {
			case e:java.net.URISyntaxException =>
						new File(urlBox.getText).toURI
		}
		
		tilesheet = tilesheetURI.getScheme match
		{
			case "about" => {
				tilesheetURI.getSchemeSpecificPart match
				{
					case "nil" => NilTilesheet
					case CheckerboardURIMatcher(checker) => checker
				}
			}
			case _ => new JSONTilesheet( tilesheetURI.toURL )
		}
		
		field = new RotateSpaceRectangularField(
				Seq.empty ++ (tilesheet match {
					case x:JSONTilesheet => {
						if (x.map.contains("TilesheetViewer::classes"))
						{
							val classesURL = new URL(tilesheetURI.toURL, x.map("TilesheetViewer::classes").toString)
							val classesReader = Files.newBufferedReader(Paths.get(classesURL.toURI), UTF_8)
							
							val listener = new ToSeqJSONParseListener()
							JSONParser.parse(listener, classesReader)
							classesReader.close()
							val classNames = listener.result.map{_.toString}
							
							classNames.map{x.sterilizeSpaceClassConstructorName(_)}
						}
						else
						{
							x.classMap.values
						}
					}
					case _ => Seq(AnySpace)
				}),
				Integer.parseInt(widthBox.getText),
				Integer.parseInt(heightBox.getText)
		)
		fieldComp = new FieldComponent(tilesheet, field)
		fieldComp.labels.zipWithIndex.foreach{labelIndex:(JLabel, Int) => labelIndex._1.addMouseListener(new RotateListener(labelIndex._2))}
		
		frame.getContentPane.removeAll()
		
		val fieldCompPane = new JPanel()
		fieldCompPane.add(fieldComp)
		
		frame.getContentPane.add(navPanel, BorderLayout.NORTH)
		frame.getContentPane.add(fieldComp)
		frame.pack()
	}
	
	class RotateListener(index:Int) extends MouseAdapter
	{
		override def mouseClicked(e:MouseEvent) =
		{
			field = field.rotate(index)
			
			frame.getContentPane.remove(fieldComp)
			
			fieldComp = new FieldComponent(tilesheet, field)
			fieldComp.labels.zipWithIndex.foreach{labelIndex:(JLabel, Int) => labelIndex._1.addMouseListener(new RotateListener(labelIndex._2))}
			frame.getContentPane.add(fieldComp)
			frame.getContentPane.validate()
		}
	}
	
	object CheckerboardURIMatcher {
		import java.awt.{Color, Dimension}
		
		def unapply(ssp:String):Option[CheckerboardTilesheet] = {
			val split = ssp.split("[\\?\\&]");
			
			if ("checker" == split.head)
			{
				var returnValue = new CheckerboardTilesheet()
				
				split.tail.foreach{(param:String) =>
					val splitParam = param.split("=");
					splitParam(0) match {
						case "size" => {
							returnValue = returnValue.copy(
								dim = new Dimension(Integer.parseInt(splitParam(1)),
										Integer.parseInt(splitParam(1)))
							)
						}
						case "light" => {
							returnValue = returnValue.copy(
								light = new Color(Integer.parseInt(splitParam(1)))
							)
						}
						case "dark" => {
							returnValue = returnValue.copy(
								dark = new Color(Integer.parseInt(splitParam(1)))
							)
						}
						case _ => {}
					}
				}
				
				return Some(returnValue)
			}
			else
			{
				return None;
			}
		}
	}
}
