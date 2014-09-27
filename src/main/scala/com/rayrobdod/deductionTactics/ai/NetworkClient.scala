/*
	Deduction Tactics
	Copyright (C) 2012-2013  Raymond Dodge

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
 * @version a.5.0
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
				TokenClassDecoder.decode(inS.nextLine())
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
				case NetworkClient.RequestMove(t:Token, s:Space) => {
					t.requestMoveTo(s)
				}
				case NetworkClient.RequestAttackForDamage(mine:Token, other:Token) => {
					mine.tryAttackDamage(other)
				}
				case NetworkClient.RequestAttackForStatus(mine:Token, other:Token) => {
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
	case class RequestMove(t:Token, s:Space)
	case class RequestAttackForDamage(mine:Token, other:Token)
	case class RequestAttackForStatus(mine:Token, other:Token)
	
	
	def cfnFunctions(field:Field, player:Player) = Map(
		"EndOfTurn" -> {() => EndOfTurn},
		"MyTokens" -> {(i:Int) => player.tokens.myTokens(i)},
		"OtherTokens" -> {(team:Int,i:Int) => player.tokens.otherTokens(team)(i)},
		"Field" -> {(x:Int,y:Int) => field.space(x,y)},
		"RequestMove" -> {(t:Token, s:Space) => RequestMove(t,s)},
		"RequestAttackForDamage" -> {(m:Token, o:Token) => RequestAttackForDamage(m,o)},
		"RequestAttackForStatus" -> {(m:Token, o:Token) => RequestAttackForStatus(m,o)}
	)
}
