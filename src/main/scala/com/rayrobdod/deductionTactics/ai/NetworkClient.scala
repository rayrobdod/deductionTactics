package com.rayrobdod.deductionTactics
package ai

import scala.collection.immutable.{Seq => ISeq, Map}
import scala.collection.mutable.{Set => MSet}
import com.rayrobdod.deductionTactics.swingView.{NetworkClientSetupPanel, InputFrame}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import java.awt.event.{ActionListener, ActionEvent}
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace, Space}
import java.net.{Socket, ServerSocket, InetAddress}
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.util.Scanner
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser
import com.rayrobdod.deductionTactics.LoggerInitializer.{networkClientLogger => Logger}
import com.rayrobdod.commonFunctionNotation.Parser.{parse => cfnParse}

/**
 * 
 * @author Raymond Dodge
 * @version 03 Jul 2012
 * @version 05 Jul 2012
 * @version 12 Jul 2012 - minor notify-before-lock condition fixed
 * @version 03 Aug 2012 - replacing an anonymous inner class with an instance of InputFrame
 * @version 09 Aug 2012 - changed a line of logging to show the received tokenclass
 * @version 09 Aug 2012 - Instead of collecting values in a MSet in a range's foreach function,
 			using the same range's map function instead
 * @version 10 Aug 2012 - removing instance variables in exchange for `TokenClassWithHiddenData`
 * @version 26 Dec 2012 - the status attack function had RequestAttackForDamage changed to RequestAttackForStatus
 * @version 2013 Jun 23 - responding to rename of ToScalaCollection
 * @version 2013 Aug 16 - ripples from changes in Token
 */
class NetworkClient extends PlayerAI
{
	private var field:Option[Field]   = None
	private var input:Option[Scanner] = None
	
	
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
			val in = socket.getInputStream
			val inS = new Scanner(in)
			
			val count = inS.nextInt
			inS.nextLine // get rid of \n after count
			val resultSeq = (1 to count).map{(x:Int) =>
				CannonicalTokenClassDecoder.decode(inS.nextLine())
			}
			
			NetworkClient.this.input = Some(inS)
			resultSeq
		}
	}
	
	def takeTurn(player:Player):Any = {
		
		val functions = NetworkClient.cfnFunctions(field.get, player);
		
		// I could do this with Streams, but I'd rather a more real-time observation
		// stream.takeWhile{_ != EndOfTurn}.foreach{player ! _}
		var nextCommand:Any = 0;
		Logger.fine("Waiting for commands")
		do {
			val nextCommandString = input.get.nextLine.trim
			Logger.finer("Recieved: " + nextCommandString)
			nextCommand = cfnParse(nextCommandString, functions)
			
			nextCommand match {
				case NetworkClient.EndOfTurn => {}
				case NetworkClient.RequestMove(t:CannonicalToken, s:Space) => {
					t.requestMoveTo(s)
				}
				case NetworkClient.RequestAttackForDamage(mine:CannonicalToken, other:Token) => {
					mine.tryAttackDamage(other)
				}
				case NetworkClient.RequestAttackForStatus(mine:CannonicalToken, other:Token) => {
					mine.tryAttackStatus(other)
				}
			}
		} while (nextCommand != NetworkClient.EndOfTurn);
		
		Logger.fine("Ending receiving of commands")
	}
	
	/** do nothing */
	def initialize(player:Player, field:Field) = {
		this.field = Some(field)
	}
	
	
	// default equals; by instance
	
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}

object NetworkClient {
	
	/** AKA, the easy way out */
	case object EndOfTurn
	case class RequestMove(t:CannonicalToken, s:Space)
	case class RequestAttackForDamage(mine:CannonicalToken, other:Token)
	case class RequestAttackForStatus(mine:CannonicalToken, other:Token)
	
	
	def cfnFunctions(field:Field, player:Player) = Map(
		"EndOfTurn" -> {() => EndOfTurn},
		"MyTokens" -> {(i:Int) => player.tokens.myTokens(i)},
		"OtherTokens" -> {(team:Int,i:Int) => player.tokens.otherTokens(team)(i)},
		"Field" -> {(x:Int,y:Int) => field.space(x,y)},
		"RequestMove" -> {(t:CannonicalToken, s:Space) => RequestMove(t,s)},
		"RequestAttackForDamage" -> {(m:CannonicalToken, o:MirrorToken) => RequestAttackForDamage(m,o)},
		"RequestAttackForStatus" -> {(m:CannonicalToken, o:MirrorToken) => RequestAttackForStatus(m,o)}
	)
}
