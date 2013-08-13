package com.rayrobdod.deductionTactics
package ai

import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace, EndOfTurn, Space}
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
import scala.swing.Reactions.Reaction
import scala.swing.event.Event
import scala.parallel.Future

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
 */
class WithNetworkServer(base:PlayerAI) extends PlayerAI
{
	/** Forwards action to base */
	def takeTurn(player:Player) = base.takeTurn(player)
	
	def initialize(player:Player, field:Field)
	{
		base.initialize(player, field)
		
		player.tokens.myTokens.foreach{_.tokenClass match {
			// TODO: dig to find TokenClassWithHiddenData - else make something reusable
			case x:TokenClassWithHiddenData => {
				x.server.field.apply_=(field)
				x.server.player.apply_=(player)
			}
			case _ => {
				Logger.finer("A tokenclass without needed hidden data")
			}
		}}
	}
	
	def buildTeam = {
		val buildingLock = new Object()
		val network = new NetworkServerSetupPanel()
		
		// TODO: figure out how to combine into one frame
		val frame = new InputFrame("Network Server", network, new ActionListener {
			override def actionPerformed(e:ActionEvent) = {
				buildingLock.synchronized { buildingLock.notifyAll }
			}
		})
		
		frame.setVisible(true)
		
		val returnValue = base.buildTeam
		
		buildingLock.synchronized {
			buildingLock.wait
		}
		
		val server = {
			val socket = network.mySocket
			Logger.fine(socket.toString)
			new StartServer(socket, returnValue)
		}
		
		threadFactory.newThread(server).start()
		
		frame.setVisible(false)
		returnValue.map{new TokenClassWithHiddenData(_,server)}
	}
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[WithNetworkServer]}
	override def equals(other:Any) = {
		// TODO: test instance variables
		this.canEquals(other) && other.asInstanceOf[WithNetworkServer].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 21
	
	override def toString = base.toString + " with " + this.getClass.getName
	
	class StartServer(socket:ServerSocket, myTokenClasses:Seq[CannonicalTokenClass]) extends Runnable {
		val player = new SetFuture[Player]
		val field = new SetFuture[Field]
		
		
		def run() = {
			while (true)
			{
				val child = socket.accept
				val thread = RunServerThreadFactory.newThread(
						new RunServer(child, player, field, myTokenClasses))
				Logger.finer("Started a Server Thread: " + thread.getName())
				thread.start()
			}
		}
		
		object RunServerThreadFactory extends ThreadFactory {
			private var _count = 0
			private def count( ) = {
				_count = _count + 1
				_count
			}
		
			def newThread(r:Runnable):Thread = {
				val returnValue = new Thread(r);
				returnValue.setName(Thread.currentThread.getName
						+ ".RunServer-" + count())
				returnValue.setDaemon(false)
				returnValue
			}
		}
	}
	
	class RunServer(
			child:Socket,
			player:SetFuture[Player],
			field:SetFuture[Field],
			myTokenClasses:Seq[CannonicalTokenClass]
	) extends Runnable {
		val in = new BufferedReader(new InputStreamReader(child.getInputStream))
		val out = child.getOutputStream()
		
		def run() = {
			Logger.finer("Waiting for command")

			val firstLine = in.readLine().trim()
			if (firstLine == "Ping")
			{
				Logger.finer("Got a Ping; Sending a PingBack")
				out.write("PingBack".getBytes)
			}
			else if (firstLine == "Ready")
			{
				Logger.finer("Got a Ready; Sending a Ready")
				out.write("Ready\n".getBytes)
				
				Logger.finer("Waiting for TokenClasses")
				while (in.readLine().trim() != "TokenClasses") {}
				Logger.finer("Got TokenClasses")
				
				out.write(myTokenClasses.length.toString.getBytes)
				out.write('\n')
				Logger.finer("Sent: " + myTokenClasses.length.toString)
				myTokenClasses.foreach{(tc:CannonicalTokenClass) =>
					out.write(tc.toJSONObject.getUnparsed.toString.getBytes)
					out.write('\n')
					Logger.finer("Sent TokenClass: " + tc.toJSONObject.toString)
				}
				out.flush()
				
				player().reactions += new SendCommandsToNetworkReaction(out, player, field)
				while (true)
				{
					Thread.sleep(1000000)
				}
			}
			else
			{
				Logger.info("Got a an unknown: " + firstLine)
				out.write("Unknown Command".getBytes)
			}
			
			in.close()
			child.close()
		}
	}
	
	class SendCommandsToNetworkReaction(
			out:OutputStream, player:SetFuture[Player], field:SetFuture[Field]
	) extends Reaction {
		def apply(e:Event)
		{
			val writen = convertToCFN(e)
			
			out.write( writen.getBytes() )
			out.write('\n')
			out.flush()
			Logger.finer("Writing: " + writen)
		}
		
		def isDefinedAt(e:Event) = {e match {
			case EndOfTurn => true
			case x:RequestMove => true
			case x:RequestAttackForDamage => true
			case x:RequestAttackForStatus => true
			case _ => false
		}}
		
		
		
		
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
		
		def convertToCFN(e:Object):String = {e match{
			case EndOfTurn => "EndOfTurn"
			case x:CannonicalToken =>
					"MyTokens(" + player().tokens.myTokens.indexOf(x) + ")"
			case x:MirrorToken =>
					"OtherTokens" + player().tokens.otherTokens.twoDIndexOf(x)
			case x:RectangularSpace =>
					"Field" + field().spaces.twoDIndexOf(x)
			case RequestMove(t:CannonicalToken, s:Space) =>
					"RequestMove(" + convertToCFN(t) + "," + convertToCFN(s) + ")"
			case RequestAttackForDamage(m:CannonicalToken, o:MirrorToken) =>
					"RequestAttackForDamage(" + convertToCFN(m) + "," + convertToCFN(o) + ")"
			case RequestAttackForStatus(m:CannonicalToken, o:MirrorToken) =>
					"RequestAttackForStatus(" + convertToCFN(m) + "," + convertToCFN(o) + ")"
			case _ => {
					Logger.warning("Unexpected object: " + e.toString)
					""
			}
		}}
	}
	
	/**
	 * A CannonicalTokenClass that holds data for a player
	 * 
	 * Forwards all calls to CannonicalTokenClass to base,
	 * and has two other vals: socket and field
	 */
	private[this] class TokenClassWithHiddenData(
			base:CannonicalTokenClass,
			val server:StartServer
	) extends CannonicalTokenClass
	{
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
	
	object threadFactory extends ThreadFactory
	{
		var count = 0;
		
		override def newThread(r:Runnable) =
		{
			val t = new Thread(r);
			t.setDaemon(false)
			
			this.synchronized{
				count = count + 1
				t.setName("NetworkServer-"+count)
			}
			t
		}
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
