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

import com.rayrobdod.deductionTactics.tokenClassToJSON
import com.rayrobdod.util.MultiForwardWriter
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace, Space,
				Token => BoardGameToken}
import com.rayrobdod.deductionTactics.swingView.{NetworkServerSetupPanel, InputFrame}
import com.rayrobdod.deductionTactics.LoggerInitializer.{networkServerLogger => Logger}
import java.awt.event.{ActionListener, ActionEvent}
import java.net.{Socket, ServerSocket, InetAddress}
import java.io.{OutputStreamWriter}
import scala.collection.immutable.Seq
import java.nio.charset.StandardCharsets.UTF_8
import scala.runtime.{AbstractFunction2 => AFunction2, AbstractFunction1 => AFunction1}

/**
 * A decorator that sets up a bunch of sockets which broadcast that player's
 * moves to anyone willing to listen.
 *
 * @author Raymond Dodge
 * @version a.5.0
 */
class WithNetworkServer(base:PlayerAI) extends PlayerAI {
	
	private val output:MultiForwardWriter = new MultiForwardWriter()
	private var player:Option[Player] = None
	private var field:Option[Field]   = None
	
	// logging
	// output.addForward(new OutputStreamWriter(System.out))
	
	/** Forwards action to base */
	def takeTurn(player:Player):Unit = {
		base.takeTurn(player)
		output.write("EndOfTurn\n")
		output.flush();
	}
	
	def initialize(player:Player, field:Field):Unit = {
		base.initialize(player, field)
		this.player = Some(player);
		this.field  = Some(field);
		
		player.tokens.myTokens.zipWithIndex.foreach({ (token:Token, index:Int) =>
			val index2 = index.toString;
			
			token.tryDamageAttackedReactions_+=(new PrintRequestDamageAttack(index2))
			token.tryStatusAttackedReactions_+=(new PrintRequestStatusAttack(index2))
			token.moveReactions_+=(new PrintMove(index2))
		}.tupled)
	}
	
	def buildTeam() = {
		val buildingLock = new Object()
		val network = new NetworkServerSetupPanel()
		
		val returnValue = buildingLock.synchronized {
			// TODO: figure out how to combine into one frame
			val frame = new InputFrame("Network Server", network, new ActionListener {
				override def actionPerformed(e:ActionEvent) = {
					buildingLock.synchronized { buildingLock.notifyAll }
				}
			})
			
			frame.setVisible(true)
			
			val returnValue2 = base.buildTeam
			buildingLock.wait
			
			frame.setVisible(false)
			returnValue2
		}
		
		val server = new StartServer(network.mySocket)
		new Thread(server, "WithNetworkServer").start()
		
		
		output.write( returnValue.length.toString )
		output.write( '\n' )
		returnValue.zipWithIndex.foreach({(tclass:TokenClass, index:Int) =>
			output.write(tokenClassToJSON(tclass))
			output.write('\n')
		}.tupled)
		output.flush()
		
		returnValue
	}
	
	
	// default equals - only if same instance.
	
	// arbitrary number (17)
	override def hashCode:Int = 21
	
	override def toString:String = base.toString + " with " + this.getClass.getName
	
	
	
	class StartServer(socket:ServerSocket) extends Runnable {
		
		Logger.fine(socket.getLocalPort.toString)
			
		def run() = while (true) {
			val child:Socket = socket.accept
			
			new Thread(new Runnable() { def run = {
				val childOutStream = child.getOutputStream;
				val childWriter = new OutputStreamWriter(childOutStream, UTF_8)
				val childInStream = child.getInputStream;
				val childReader = new java.util.Scanner(childInStream, UTF_8.toString)
				
				var line:String = "";
				do {
					line = childReader.nextLine()
					
					Logger.fine("Recieved a " + line)
					if (line == "Ping") {
						childWriter.write("PingBack\n")
					}
					if (line == "Ready") {
						childWriter.write("Ready\n")
					}
					childWriter.flush()
				} while (line != "TokenClasses")
				
				
				WithNetworkServer.this.output.addForward(childWriter)
			}}, "WithNetworkServer-child").start()
		}
	}
	
	
	private implicit def twoDSeq[A](x:Seq[Seq[A]]) = new TwoDSeq(x)
	
	private class TwoDSeq[A](haystack:Seq[Seq[A]]) {
		def twoDIndexOf(needle:A):Tuple2[Int,Int] = {
			val ys = haystack.map{_.indexOf(needle)}
			val pairs = ys.zipWithIndex.filter({(y:Int, x:Int) => y != -1}.tupled)
			
			if (pairs.isEmpty) {(-1, -1)} else {pairs.head}
		}
	}
	
	
	final class PrintMove(tokenIndex:String) extends AFunction2[Space, Boolean, Unit] with BoardGameToken.MoveReactionType {
		def apply(space:Space, landedOn:Boolean):Unit = space match {
			case s:RectangularSpace => {
				output.write("RequestMove(MyTokens(")
				output.write(tokenIndex)
				output.write("),Field")
				output.write(field.get.spaces.twoDIndexOf(s).toString)
				output.write(")\n")
			}
		}
	}
	
	final class PrintRequestDamageAttack(tokenIndex:String) extends AFunction1[Token, Unit] with Token.RequestAttackType {
		def apply(target:Token):Unit = target match {
			case target2:Token => {
				output.write("RequestAttackForDamage(MyTokens(")
				output.write(tokenIndex)
				output.write("),OtherTokens")
				output.write(player.get.tokens.otherTokens.twoDIndexOf(target2).swap.toString)
				output.write(")\n")
			}
		}
	}
	
	final class PrintRequestStatusAttack(tokenIndex:String) extends AFunction1[Token, Unit] with Token.RequestAttackType {
		def apply(target:Token):Unit = target match {
			case target2:Token => {
				output.write("RequestAttackForStatus(MyTokens(")
				output.write(tokenIndex)
				output.write("),OtherTokens")
				output.write(player.get.tokens.otherTokens.twoDIndexOf(target2).swap.toString)
				output.write(")\n")
			}
		}
	}
}
