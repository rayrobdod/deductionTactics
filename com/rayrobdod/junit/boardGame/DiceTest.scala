package com.rayrobdod.junit.boardGame

import scala.swing.{SimpleSwingApplication, Button, Label, Frame, GridPanel, BoxPanel}
import scala.swing.Reactions.Reaction
import scala.swing.event.{Event, ButtonClicked}
import com.rayrobdod.boardGame.{Die, DieComponent, Unfixed, RollDie}
import java.util.logging.{Logger, Level, ConsoleHandler}
import javax.swing.SwingWorker
import java.awt.Color.{red, white, green, black, blue, cyan, magenta, yellow}
import java.util.Random
import scala.swing.Orientation.{Vertical, Horizontal}
import scala.swing.Swing.pair2Dimension

/**
 * A component that shows a label with the current die's value, a die, and a button to roll the die.
 * 
 * @author Raymond Dodge
 * @version 19 May 2011
 */
object DiceVisualTest extends SimpleSwingApplication
{
	import VisualDiceTestSetLoggerInitializer.{testLogger => logger}
	
	logger.entering("DiceVisualTest", "this()")
	
	val die4 = new Die with Unfixed {def size = 4}
	val die6 = new Die with Unfixed {def size = 6}
	val die12 = new Die with Unfixed {def size = 12}
	val die20 = new Die with Unfixed {def size = 20}
	
	val dieMap = Map(4 -> die4, 6 -> die6, 12 -> die12, 20 -> die20)
	dieMap.values.foreach{_.start}
	
	val dieComponentMap = dieMap.map{x:(Int, Die) => (x._1, DieComponent(x._2))}
	
	val colors = Set(red, green, blue, cyan, magenta, yellow)
	val random = new Random
	
	dieComponentMap.values.foreach{_.background = colors.toSeq(random.nextInt(3))}
	dieComponentMap.values.foreach{_.foreground = colors.toSeq(random.nextInt(3) + 3)}
	
	val rowPanelMap = dieComponentMap.map{x:(Int, DieComponent) => 
		val (sides:Int, dieComp:DieComponent) = x
		val button = new Button("Roll " + sides)
		val label = new MyLabel(sides, button)
		
		label listenTo button
		
		val rowPanel = new BoxPanel(Horizontal)
		rowPanel.contents += label
		rowPanel.contents += dieComp
		rowPanel.contents += button
		
		(sides, rowPanel)
	}
	
	class MyLabel(val dieSize:Int, val listenToButton:Button) extends Label
	{
		this listenTo listenToButton
		
		object ButtonClickToRollDie extends Reaction
		{
			class WaitForDieRoller extends SwingWorker[Unit, Unit]
			{
				override def doInBackground:Unit =
				{
					logger.entering("DiceVisualTest.MyLabel.ButtonClickToRollDie.WaitForDieRoller", "doInBackground")
					val future = dieMap(dieSize) !! RollDie
					logger.finer("Future collected")
					MyLabel.this.text = future().toString
					logger.exiting("DiceVisualTest.MyLabel.ButtonClickToRollDie.WaitForDieRoller", "doInBackground")
				}
			}
			
			override def apply(e:Event) =
			{
				logger.entering("DiceVisualTest.MyLabel.ButtonClickToRollDie", "apply(Event)", e)
				
				e match
				{
					case ButtonClicked(_) =>
					{
						new WaitForDieRoller().execute()
					}
					case _ => {}
				}
				logger.exiting("DiceVisualTest.MyLabel.ButtonClickToRollDie", "apply(Event)")
			}
			
			override def isDefinedAt(e:Event) =
			{
				e match
				{
					case ButtonClicked(_) => true
					case _ => false
				}
			}
		}
		
		reactions += ButtonClickToRollDie
	}
	
	val top = new Frame
	{
		title = "DiceVisualTest"
		contents = new BoxPanel(Vertical)
		{
			contents ++ rowPanelMap.values
		}
		
		peer.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
		
		size = ((200,100 * dieMap.size))
	}
}

object VisualDiceTestSetLoggerInitializer
{
	val warningConsoleHander = new ConsoleHandler()
	warningConsoleHander.setLevel(Level.WARNING)
	
	val finerConsoleHander = new ConsoleHandler()
	finerConsoleHander.setLevel(Level.FINER)
	
	val testLogger = Logger.getLogger("net.verizon.rayrobdod.junit.boardGame.DiceVisualTest")
	testLogger.addHandler(finerConsoleHander)
	testLogger.setLevel(Level.WARNING)
}
