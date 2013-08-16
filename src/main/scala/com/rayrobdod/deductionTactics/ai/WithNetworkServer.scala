package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.util.MultiForwardWriter
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace, Space,
				Token => BoardGameToken}
import com.rayrobdod.deductionTactics.swingView.{NetworkServerSetupPanel, InputFrame}
import com.rayrobdod.deductionTactics.LoggerInitializer.{networkServerLogger => Logger}
import java.awt.BorderLayout
import java.awt.event.{ActionListener, ActionEvent}
import java.net.{Socket, ServerSocket, InetAddress}
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, OutputStream}
import java.util.concurrent.ThreadFactory
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import scala.collection.immutable.Seq
import scala.collection.mutable.{Map => MMap}
import scala.parallel.Future
import java.nio.charset.StandardCharsets.UTF_8

/**
 * A decorator that sets up a bunch of sockets which broadcast that player's
 * moves to anyone willing to listen.
 *
 * @author Raymond Dodge
 * @version 03 Jul 2012
 * @version 05 Jul 2012
 * @version 09 Jul 2012 - renaming from SwingInterfaceWithNetworkServer to WithNetworkServer; making a decorator
 * @version 12 Jul 2012 - fixing issue where this didn't send tokens to clients, due to not setting myTokens
 * @version 19 Jul 2012 - corecting equals with new class name
 * @version 03 Aug 2012 - replacing an annonymous inner class with an instance of InputFrame
 * @version 09 Aug 2012 - removing instance variables in exchange for `TokenClassWithHiddenData`
 * @version 2012 Nov 30 - modifying toString to include the base
 * @version 2013 Aug 08 - removing certain parenthesis that Scala 2.11 doesn't like
 * @version 2013 Aug 16 - complete rewrite
 */
class WithNetworkServer(base:PlayerAI) extends PlayerAI {
	
	val output:MultiForwardWriter = new MultiForwardWriter()
	var player:Option[Player] = None
	var field:Option[Field]   = None
	
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
		returnValue.foreach{ x:CannonicalTokenClass => 
			output.write( '[' )
			output.write( x.toJSONObject.toString )
			output.append( ']' )
		}
		new Thread(server, "WithNetworkServer")
		
		
		returnValue
	}
	
	
	// default equals - only if same instance.
	
	// arbitrary number (17)
	override def hashCode = 21
	
	override def toString = base.toString + " with " + this.getClass.getName
	
	
	
	class StartServer(socket:ServerSocket) extends Runnable {
		
		def run() = {
			while (true)
			{
				val child:Socket = socket.accept
				
				val childStream = child.getOutputStream;
				val childWriter = new OutputStreamWriter(childStream, UTF_8)
				
				WithNetworkServer.this.output.addForward(childWriter)
			}
		}
	}
	
	
		private implicit def twoDSeq[A](x:Seq[Seq[A]]) = new TwoDSeq(x)
		
		private class TwoDSeq[A](haystack:Seq[Seq[A]])
		{
			def twoDIndexOf(needle:A):Tuple2[Int,Int] =
			{
				val ys = haystack.map{_.indexOf(needle)}
				val pairs = ys.zipWithIndex.filter({(y:Int, x:Int) => y != -1}.tupled)
				
				if (pairs.isEmpty) {(-1, -1)} else {pairs.head.swap}
			}
		}
	
	
	final class PrintMove(tokenIndex:String) extends BoardGameToken.MoveReactionType {
		def apply(space:Space, landedOn:Boolean):Unit = space match {
			case s:RectangularSpace => {
				output.write("RequestMove(MyTokens(")
				output.write(tokenIndex)
				output.write("),Field")
				output.write(field.get.spaces.twoDIndexOf(s).toString)
				output.write("\n")
			}
		}
	}
	
	final class PrintRequestDamageAttack(tokenIndex:String) extends CannonicalToken.RequestAttackType {
		def apply(target:Token):Unit = target match {
			case target2:MirrorToken => {
				output.write("RequestAttackForDamage(MyTokens(")
				output.write(tokenIndex)
				output.write("),OtherTokens")
				output.write(player.get.tokens.otherTokens.twoDIndexOf(target).toString)
				output.write("\n")
			}
		}
	}
	
	final class PrintRequestStatusAttack(tokenIndex:String) extends CannonicalToken.RequestAttackType {
		def apply(target:Token):Unit = target match {
			case target2:MirrorToken => {
				output.write("RequestAttackForStatus(MyTokens(")
				output.write(tokenIndex)
				output.write("),OtherTokens")
				output.write(player.get.tokens.otherTokens.twoDIndexOf(target).toString)
				output.write("\n")
			}
		}
	}
}
