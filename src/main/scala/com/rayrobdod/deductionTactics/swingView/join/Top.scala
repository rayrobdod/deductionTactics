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
package com.rayrobdod.deductionTactics.swingView.join

import java.awt._
import java.awt.event._
import javax.swing._
import javax.swing.event._
import java.net.InetAddress
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}
import com.rayrobdod.deductionTactics.swingView.ChooseAIsComponent

class Top {
	
	private val resources = java.util.ResourceBundle.getBundle("com.rayrobdod.deductionTactics.swingView.text")
	
	private val frame = new JFrame(resources.getString("joinNewGameFrameTitle"))
	private val nextButton = new JButton(resources.getString("okButton"))
	private val cancelButton = new JButton(resources.getString("cancelButton"))
	private val maps:ListModel[Nothing] = new ScalaSeqListModel(Nil)
	private val aisPanel = new ChooseAIsComponent;
	
	{
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		
		val mapPreviewPanel = new JPanel(new GridBagLayout());
		{
			val mapPreviewPicture = new JLabel("Map Preview Picture")
			val tokensPerPlayer = new JLabel("4")
			val playerCount = new JLabel("4")
			
			mapPreviewPanel.add(mapPreviewPicture, GridBagConstraintsFactory(gridx = 0, gridy = 0, gridwidth = 2, weighty = 2, weightx = 1, fill = GridBagConstraints.BOTH))
			mapPreviewPanel.add(new JLabel(resources.getString("tokensLabel")), GridBagConstraintsFactory(gridx = 0, gridy = 1, fill = GridBagConstraints.BOTH))
			mapPreviewPanel.add(tokensPerPlayer, GridBagConstraintsFactory(gridx = 1, gridy = 1, fill = GridBagConstraints.BOTH))
			mapPreviewPanel.add(new JLabel(resources.getString("playersLabel")), GridBagConstraintsFactory(gridx = 0, gridy = 2, fill = GridBagConstraints.BOTH))
			mapPreviewPanel.add(playerCount, GridBagConstraintsFactory(gridx = 1, gridy = 2, fill = GridBagConstraints.BOTH))
		}
		
		val chooseHostPanel = new JPanel(new FlowLayout());
		{
			val addressField = new JFormattedTextField(new IPAddressFormat());
			val portField = new JFormattedTextField(java.text.NumberFormat.getInstance());
			
			addressField.setText("::1");
			addressField.setColumns(25);
			portField.setValue(17492);
			portField.setColumns(5);
			
			chooseHostPanel.add(addressField);
			chooseHostPanel.add(new JLabel(":"));
			chooseHostPanel.add(portField);
		}
		
		val buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		{
			buttonPanel.add(cancelButton)
			buttonPanel.add(nextButton)
			
			cancelButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent) {
					Top.this.frame.dispose();
				}
			})
			nextButton.addActionListener(new ActionListener() {
				def actionPerformed(e:ActionEvent) {
					Top.this.frame.dispose();
				}
			})
		}
		
		{
			aisPanel.players = 1;
		}
		
		val midPanel = new JPanel(new FlowLayout());
		{
			midPanel.add(aisPanel)
			midPanel.add(mapPreviewPanel)
		}
		
		frame.getContentPane().add(chooseHostPanel, BorderLayout.NORTH)
		frame.getContentPane().add(midPanel)
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH)
		frame.pack()
	}
	
	
	def show() = {
		frame.setVisible(true);
	}
	
	def addNextActionListener(a:ActionListener) {
		nextButton.addActionListener(a)
	}
	
}

object Top {
	def main(args:Array[String]):Unit = {
		val t = new Top();
		t.addNextActionListener(new ActionListener() {
			def actionPerformed(e:ActionEvent) {
				Top.main(new Array[String](0))
			}
		})
		t.show();
	}
}

class IPAddressFormat extends java.text.Format {
	def format(obj:Any, toAppendTo:StringBuffer, pos:java.text.FieldPosition):StringBuffer = obj match {
		case x:InetAddress => {
			toAppendTo.append(x.getHostName());
			return toAppendTo;
		}
		case _ => throw new IllegalArgumentException
	}
	
	def parseObject(source:String, pos:java.text.ParsePosition):InetAddress = {
		try {
			val retVal = InetAddress.getByName(source.substring(pos.getIndex()));
			pos.setIndex(source.length());
			return retVal;
		} catch {
			case e:java.net.UnknownHostException => {
				pos.setErrorIndex(pos.getIndex());
				return null;
			}
		}
	}
}
