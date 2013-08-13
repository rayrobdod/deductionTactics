package com.rayrobdod.deductionTactics
package ai

import scala.collection.immutable.Seq
import scala.collection.mutable.{Map => MMap}
import com.rayrobdod.boardGame.{RectangularField => Field}
import com.rayrobdod.deductionTactics.view.{TeamBuilderPanel, NetworkServerSetupPanel}
import javax.swing.{JButton, JFrame, JPanel, JLabel, JList}
import java.awt.BorderLayout
import java.awt.event.{ActionListener, ActionEvent}
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace}
import java.net.{Socket, ServerSocket, InetAddress}
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, OutputStream}
import com.rayrobdod.deductionTactics.LoggerInitializer.{networkServerLogger => Logger}
import java.util.concurrent.ThreadFactory
import com.rayrobdod.boardGame.{RectangularField => Field, RectangularSpace, EndOfTurn, Space}
import scala.swing.Reactions.Reaction
import scala.swing.event.Event

/**
 * A slight modification of a HumanAI that sends commands over
 * a server connection
 *
 * @author Raymond Dodge
 * @version 03 Jul 2012
 * @version 05 Jul 2012
 */
class SwingInterfaceWithNetworkServer extends PlayerAI
{
	val playerButtons = MMap[Player, JButton]()
	var socket = new ServerSocket()
	private var _myTokens:Seq[CannonicalTokenClass] = null
	def myTokens_=(x:Seq[CannonicalTokenClass]) = {this.synchronized {
		_myTokens = x
		this.notifyAll
	}}
	def myTokens = {this.synchronized {
		while (_myTokens == null) {this.wait()}
		_myTokens
	}}
	private var _myPlayer:Player = null
	def myPlayer_=(x:Player) = {this.synchronized {
		_myPlayer = x
		this.notifyAll
	}}
	def myPlayer = {this.synchronized {
		while (_myPlayer == null) {this.wait()}
		_myPlayer
	}}
	private var _myField:Field = null
	def myField_=(x:Field) = {this.synchronized {
		_myField = x
		this.notifyAll
	}}
	def myField = {this.synchronized {
		while (_myField == null) {this.wait()}
		_myField
	}}
	
	
	def takeTurn(player:Player)
	{
		playerButtons(player).setEnabled(true)
	}
	
	def initialize(player:Player, field:Field)
	{
		// set up interface
		val interface = new SwingInterface;
		interface.initialize(player, field)
		playerButtons ++= interface.playerButtons
		
		myPlayer = player
		myField = field
	}
	
	def buildTeam = {
		val buildingLock = new Object()
		val teamBuilder = new TeamBuilderPanel()
		val network = new NetworkServerSetupPanel()
		val okButton = new JButton("OK")
		
		val frame = new JFrame() {
			add(new JPanel(new BorderLayout){
				add(teamBuilder)
				add(network, BorderLayout.SOUTH)
			})
			add(new JPanel(){add(okButton)}, BorderLayout.SOUTH)
			setTitle("Choose Team")
			setSize(400, 600)
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
		socket = network.mySocket
		System.out.println(socket)
		val thread = new Thread(new StartServer(socket))
		thread.setDaemon(false)
		thread.setName("NetworkServer")
		thread.start()
		
		frame.setVisible(false)
		myTokens = teamBuilder.currentSelection
		myTokens
	}
	
	
	
	def canEquals(other:Any) = {other.isInstanceOf[SwingInterfaceWithNetworkServer]}
	override def equals(other:Any) = {
		// no instance variables to test
		this.canEquals(other) && other.asInstanceOf[SwingInterfaceWithNetworkServer].canEquals(this)
	}
	// arbitrary number (17)
	override def hashCode = 21
	
	override def toString = this.getClass.getName
	
	class StartServer(socket:ServerSocket) extends Runnable {
		def run() = {
			while (true)
			{
				val child = socket.accept
				val thread = RunServerThreadFactory.newThread(
						new RunServer(child))
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
				returnValue.setName("SwingInterfaceWithNetworkServer"
						+ ".RunServer-" + count())
				returnValue.setDaemon(false)
				returnValue
			}
		}
	}
	
	class RunServer(child:Socket) extends Runnable {
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
				
				out.write(myTokens.length.toString.getBytes)
				out.write('\n')
				Logger.finer("Sent: " + myTokens.length.toString)
				myTokens.foreach {(tc:CannonicalTokenClass) =>
					out.write(tc.toJSONObject.getUnparsed.toString.getBytes)
					out.write('\n')
					Logger.finer("Sent TokenClass: " + tc.toJSONObject.toString)
				}
				out.flush()
				
				myPlayer.reactions += new SendCommandsToNetworkReaction(out)
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
	
	class SendCommandsToNetworkReaction(out:OutputStream) extends Reaction {
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
					"MyTokens(" + myPlayer.tokens.myTokens.indexOf(x) + ")"
			case x:MirrorToken =>
					"OtherTokens" + myPlayer.tokens.otherTokens.twoDIndexOf(x)
			case x:RectangularSpace =>
					"Field" + myField.spaces.twoDIndexOf(x)
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
}
