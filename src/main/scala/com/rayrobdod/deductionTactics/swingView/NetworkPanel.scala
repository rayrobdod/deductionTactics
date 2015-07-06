/*
	Deduction Tactics
	Copyright (C) 2012-2015  Raymond Dodge

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
package com.rayrobdod.deductionTactics.swingView

import java.awt.GridLayout
import javax.swing.{JTextField, JPanel, JLabel, JButton}
import java.net.{Socket, ServerSocket, InetAddress}
import java.awt.event.{ActionListener, ActionEvent}
import java.util.concurrent.ThreadFactory

/**
 * @author Raymond Dodge
 * @todo make JTextFields into JFormattedTextFields
 */
class NetworkClientSetupPanel extends JPanel
{
	def otherSocket = new Socket(otherIP.getText, Integer.parseInt(otherPort.getText))
	
	private val otherIP = new JTextField(20) 
	private val otherPort = new JTextField(6) 
	
	private val pingLabel = new JLabel
	private val pingButton = new JButton("Ping")
	pingButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			import NetworkClientSetupPanel._
			
			PingThreadFactory.newThread(new PingClient(
					NetworkClientSetupPanel.this.otherSocket, pingLabel.setText)).start()
		}
	})
	
	this.setLayout(new GridLayout(2,1))
	this.add(new JPanel(){
		add(otherIP)
		add(new JLabel(":"))
		add(otherPort)
	})
	this.add(new JPanel(){
		add(pingLabel)
		add(pingButton)
	})
}

object NetworkClientSetupPanel
{
	object PingThreadFactory extends ThreadFactory {
		private var _clientCount = 0
		def clientCount( ) = {
			_clientCount = _clientCount + 1
			_clientCount
		}
		
		private var _serverCount = 0
		def serverCount( ) = {
			_serverCount = _serverCount + 1
			_serverCount
		}
		
		private var _otherCount = 0
		def otherCount( ) = {
			_otherCount = _otherCount + 1
			_otherCount
		}
		
		def newThread(r:Runnable):Thread = {
			val returnValue = new Thread(r);
			
			r match {
				case c:PingClient => returnValue.setName(
						"NetworkPanel.PingClient-" + clientCount())
				case c:PingServer => returnValue.setName(
						"NetworkPanel.PingServer-" + serverCount())
				case _ => returnValue.setName(
						"NetworkPanel.PingOther-" + otherCount())
			}
			
			returnValue
		}
	}
	
	
	class PingClient(socket:Socket, setText:Function1[String,Any]) extends Runnable
	{
		override def run() = {
			socket.getOutputStream().write("Ping".getBytes)
			setText("Sent Ping")
			socket.getInputStream().read()
			setText("Recieved PingBack")
			
			//socket.getInputStream().close()
			//socket.getOutputStream().close()
			socket.close()
		}
	}
	
	class PingServer(socket:ServerSocket, setText:Function1[String,Any]) extends Runnable
	{
		override def run() = {
			setText("Waiting for Ping")
			val child = socket.accept()
			if (child.getInputStream().read() == 'P')
			{
				setText("Recieved Ping; Sending PingBack")
				child.getOutputStream().write("PingBack".getBytes)
			} else {
				setText("Recieved something other than Ping")
				child.getOutputStream().write("PingBack".getBytes)
			}
			
			/*
			   apparently one should always close the streams before
			   the socket, but the socket closes when its streams are
			   closed? And you can't get the stream after it closes
			 */
			
			//child.getInputStream().close()
			//child.getOutputStream().close()
			child.close()
		}
	}
}

/**
 * @author Raymond Dodge
 */
class NetworkServerSetupPanel extends JPanel
{
	val mySocket = new ServerSocket(0)
	
	//private val myIP = new JLabel(mySocket.getInetAddress.toString)
	private val myIP = new JLabel(InetAddress.getLocalHost.toString)
	private val myPort = new JLabel(mySocket.getLocalPort.toString)
	
	private val pingTestLabel = new JLabel
	private val pingTestButton = new JButton("TestPing")
	pingTestButton.addActionListener(new ActionListener(){
		override def actionPerformed(e:ActionEvent) = {
			import NetworkClientSetupPanel._
			
			PingThreadFactory.newThread(new PingServer(
					NetworkServerSetupPanel.this.mySocket, pingTestLabel.setText)).start()
		}
	})
	
	this.setLayout(new GridLayout(2,1))
	this.add(new JPanel(){
		add(myIP)
		add(new JLabel(":"))
		add(myPort)
	})
	this.add(new JPanel(){
		add(pingTestButton)
		add(pingTestLabel)
	})
}
