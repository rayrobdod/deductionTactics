package com.rayrobdod.deductionTactics
package ai

import scala.collection.immutable.{Seq => ISeq, Map}
import scala.collection.mutable.{Set => MSet}
import com.rayrobdod.deductionTactics.view.{NetworkClientSetupPanel, InputFrame}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import java.awt.BorderLayout
import java.awt.event.{ActionListener, ActionEvent}
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace, EndOfTurn, Space}
import java.net.{Socket, ServerSocket, InetAddress}
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.util.Scanner
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToSeqJSONParseListener
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.deductionTactics.LoggerInitializer.{networkClientLogger => Logger}
import com.rayrobdod.commonFunctionNotation.Parser.{parse => cfnParse}
import scala.parallel.Future

/**
 * 
 * @author Raymond Dodge
 * @version 03 Jul 2012
 * @version 05 Jul 2012
 * @version 12 Jul 2012 - minor notify-before-lock condition fixed
 * @version 03 Aug 2012 - replacing an annonymous inner class with an instance of InputFrame
 * @version 09 Aug 2012 - changed a line of logging to show the recived tokenclass
 * @version 09 Aug 2012 - Instead of collecting values in a MSet in a range's foreach function,
 			using the same range's map function instead
 * @version 10 Aug 2012 - removing instance variables in exchange for `TokenClassWithHiddenData`
 */
class NetworkClient extends PlayerAI
{
	def buildTeam = {
		val buildingLock = new Object()
		val network = new NetworkClientSetupPanel()
		
		val frame = new InputFrame("Setup Connection", network, new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized {
			frame.setVisible(true)
			buildingLock.wait
		}
		
		val socket = network.otherSocket
		Logger.fine(socket.toString)
		
		{
			val in = new Scanner(socket.getInputStream)
			val out = new OutputStreamWriter(socket.getOutputStream)
			
			var nextLine = ""
			do
			{
				Logger.finer("Sending a Ready")
				out.write("Ready\n")
				out.flush()
				nextLine = in.nextLine().trim() 
			} while (nextLine != "Ready");
			Logger.finer("Got a Ready; Sending a TokenClasses")
			
			out.write("TokenClasses\n")
			out.flush()
		}
		
		frame.setVisible(false)
		
		{
			val in = new Scanner(socket.getInputStream)
			val jsonListener = new ToSeqJSONParseListener()
			val count = Integer.parseInt(in.nextLine().trim())
			Logger.finer("Got a count: " + count)
			
			(1 to count).map{(i:Int) =>
				val line = in.nextLine
				Logger.finer("Reading an instance of TokenClass: " + line)
				JSONParser.parse(jsonListener, line)// new InputStreamReader(socket.getInputStream))
				
				Logger.finer("Got an instance of TokenClass")
				new CannonicalTokenClassFromMap(jsonListener.resultMap)
			}.map{new TokenClassWithHiddenData(_, in)}
		}
	}
	
	def takeTurn(player:Player):Any = {
		val (field, in) = player.tokens.myTokens.head.tokenClass match {
			case x:TokenClassWithHiddenData => (( x.field(), x.in ))
		}
		
		val functions = Map(
			"EndOfTurn" -> {() => EndOfTurn},
			"MyTokens" -> {(i:Int) => player.tokens.myTokens(i)},
			"OtherTokens" -> {(team:Int,i:Int) => player.tokens.otherTokens(team)(i)},
			"Field" -> {(x:Int,y:Int) => field.space(x,y)},
			"RequestMove" -> {(t:CannonicalToken, s:Space) => RequestMove(t,s)},
			"RequestAttackForDamage" -> {(m:CannonicalToken, o:MirrorToken) => RequestAttackForDamage(m,o)},
			"RequestAttackForStatus" -> {(m:CannonicalToken, o:MirrorToken) => RequestAttackForDamage(m,o)}
		)
		
		// I could do this with Streams, but I'd rather a more real-time observation
		// stream.takeWhile{_ != EndOfTurn}.foreach{player ! _}
		var nextCommand:Any = 0;
		Logger.fine("Waiting for commands")
		do {
			val nextCommandString = in.nextLine.trim
			Logger.finer("Recieved: " + nextCommandString)
			nextCommand = cfnParse(nextCommandString, functions)
			
			player ! nextCommand
		} while (nextCommand != EndOfTurn);
		
		Logger.fine("Ending recieveing of commands")
	}
	
	/** do nothing */
	def initialize(player:Player, field:Field) = {
		player.tokens.myTokens.foreach{_.tokenClass match {
			// TODO: dig to find TokenClassWithHiddenData - else make something reusable
			case x:TokenClassWithHiddenData => {
				x.field.apply_=(field)
			}
			case _ => {
				Logger.warning("A tokenclass without needed hidden data")
			}
		}}
	}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[BlindAttackAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[BlindAttackAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
	
	
	
	
	
	private[this] class TokenClassWithHiddenData(
			base:CannonicalTokenClass,
			val in:Scanner
	) extends CannonicalTokenClass
	{
		val field:SetFuture[Field] = new SetFuture[Field]
		
		
		def name = base.name
		def icon = base.icon
		
		def body = base.body
		def atkElement = base.atkElement
		def atkWeapon = base.atkWeapon
		def atkStatus = base.atkStatus
		def range = base.range
		def speed = base.speed
		
		def weakDirection = base.weakDirection
		def weakWeapon = base.weakWeapon
		def weakStatus = base.weakStatus
	}
	
	class SetFuture[A] extends Future[A]
	{
		private var _apply:Option[A] = None
		def apply = {this.synchronized{
			while (_apply == None) {this.wait}
			_apply.get
		}}
		def apply_=(a:A) = {this.synchronized{
			_apply = Some(a)
			this.notifyAll
		}}
		
		def isDone = {this.synchronized{
			!(_apply == None)
		}}
	}
}
