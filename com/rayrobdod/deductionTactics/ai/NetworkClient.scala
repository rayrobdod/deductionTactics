package com.rayrobdod.deductionTactics
package ai

import scala.collection.immutable.{Seq => ISeq, Map}
import scala.collection.mutable.{Set => MSet}
import com.rayrobdod.deductionTactics.view.NetworkClientSetupPanel
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

/**
 * 
 * @author Raymond Dodge
 * @version 03 Jul 2012
 * @version 05 Jul 2012
 */
class NetworkClient extends PlayerAI
{
	var socket = new Socket()
	var field:Field = null
	
	def buildTeam = {
		val buildingLock = new Object()
		val network = new NetworkClientSetupPanel()
		val okButton = new JButton("OK")
		
		val frame = new JFrame() {
			add(network)
			add(new JPanel(){add(okButton)}, BorderLayout.SOUTH)
			setTitle("Setup Connection")
			pack()
			setVisible(true)
			getRootPane.setDefaultButton(okButton)
		}
		
		okButton.addActionListener(new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		buildingLock.synchronized {buildingLock.wait}
		
		// Change to Logger
		socket = network.otherSocket
		System.out.println(socket)
		
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
		
		val tokenClasses = MSet.empty[CannonicalTokenClass]
		
		{
			val in = new Scanner(socket.getInputStream)
			val jsonListener = new ToSeqJSONParseListener()
			val count = Integer.parseInt(in.nextLine().trim())
			Logger.finer("Got a count: " + count)
			
			(1 to count).foreach{(i:Int) =>
				val line = in.nextLine
				Logger.finer("Reading an instance of TokenClass")
				JSONParser.parse(jsonListener, line)// new InputStreamReader(socket.getInputStream))
				
				Logger.finer("Got an instance of TokenClass")
				tokenClasses += new CannonicalTokenClassFromMap(jsonListener.resultMap)
			}
		}
		
		ISeq.empty ++ tokenClasses
	}
	
	def takeTurn(player:Player):Any = {
		// TODO: read commands
		
		val functions = Map(
			"EndOfTurn" -> {() => EndOfTurn},
			"MyTokens" -> {(i:Int) => player.tokens.myTokens(i)},
			"OtherTokens" -> {(team:Int,i:Int) => player.tokens.otherTokens(team)(i)},
			"Field" -> {(x:Int,y:Int) => field.space(x,y)},
			"RequestMove" -> {(t:CannonicalToken, s:Space) => RequestMove(t,s)},
			"RequestAttackForDamage" -> {(m:CannonicalToken, o:MirrorToken) => RequestAttackForDamage(m,o)},
			"RequestAttackForStatus" -> {(m:CannonicalToken, o:MirrorToken) => RequestAttackForDamage(m,o)}
		)
		
		val in = new Scanner(socket.getInputStream)
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
	def initialize(player:Player, field:Field) = {this.field = field}
	
	
	def canEquals(other:Any) = {other.isInstanceOf[BlindAttackAI]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[BlindAttackAI].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 19
	
	override def toString = this.getClass.getName
}
