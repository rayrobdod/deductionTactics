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
package com.rayrobdod.deductionTactics.swingView

import scala.collection.immutable.Seq
import javax.swing.{JPanel, BoxLayout, JList, AbstractListModel, JScrollPane, ListSelectionModel, JLabel}
import javax.swing.BoxLayout.{X_AXIS => boxXAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever,
		HORIZONTAL_SCROLLBAR_AS_NEEDED => scrollHorizontalAsNeeded}
import com.rayrobdod.deductionTactics.PlayerAI
import java.awt.{GridLayout, GridBagConstraints, GridBagLayout, Component, Insets}
import com.rayrobdod.swing.{ScalaSeqListModel, GridBagConstraintsFactory}

/**
 * @author Raymond Dodge
 * @version a.4.?
 */
class ChooseAIsComponent extends JPanel
{
	// TODO: make lists dynamic or growable
	private val maxPlayers = 6
	
	private var _players:Int = 2
	def players:Int = _players
	def players_=(x:Int) = {
		_players = x
		
		this.removeAll()
		this.add(
			new JLabel("Players:"),
			GridBagConstraintsFactory( gridy = 0, gridx = 0, anchor = GridBagConstraints.FIRST_LINE_START)
		)
		this.add(
			new JLabel("<html>Primary<br/>(choose one)</html>"),
			GridBagConstraintsFactory( gridy = 1, gridx = 0, ipadx = 3)
		)
		this.add(
			new JLabel("<html>Addends<br/>(choose many)</html>"),
			GridBagConstraintsFactory( gridy = 2, gridx = 0, ipadx = 3)
		)
		(1 to x).foreach{(y:Int) => this.add(
				new JLabel("Player " + y),
				GridBagConstraintsFactory(gridy = 0, gridx = y)
		)}
		(aiListsScrollPane.take(x).zipWithIndex).foreach({(c:Component, i:Int) => this.add(c,
				GridBagConstraintsFactory(weightx = 1d, weighty = 1d, gridy = 1, gridx = i+1, fill = GridBagConstraints.BOTH, insets = new Insets(2,6,2,6) )
		)}.tupled)
		(aiDListsScrollPane.take(x).zipWithIndex).foreach({(c:Component, i:Int) => this.add(c,
				GridBagConstraintsFactory(weightx = 1d, weighty = 1d, gridy = 2, gridx = i+1, fill = GridBagConstraints.BOTH, insets = new Insets(2,6,2,6) )
		)}.tupled)
	}
	
	val aiLists:Seq[JList[PlayerAI]] = Seq.fill(maxPlayers){
		val ret = new JList[PlayerAI](new ScalaSeqListModel(PlayerAI.baseServiceSeq))
		ret.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		ret
	}
	val aiDLists:Seq[JList[Class[_ <: PlayerAI]]] = Seq.fill(maxPlayers){new JList[Class[_ <: PlayerAI]](new ScalaSeqListModel(PlayerAI.decoratorServiceSeq))}
	
	val aiListsScrollPane = aiLists.map{(list:JList[PlayerAI]) =>
			new JScrollPane(list, scrollVerticalAsNeeded, scrollHorizontalAsNeeded)
	}
	val aiDListsScrollPane = aiDLists.map{(list:JList[Class[_ <: PlayerAI]]) =>
			new JScrollPane(list, scrollVerticalAsNeeded, scrollHorizontalAsNeeded)
	}
	aiLists.foreach{_.setSelectedIndex(0)}
	
	
	def getAIs:Seq[PlayerAI] = {
		import java.util.ServiceConfigurationError;
		import java.lang.reflect.Constructor;
		import scala.collection.JavaConversions.iterableAsScalaIterable
		
		aiLists.zip(aiDLists).map({(baseList:JList[PlayerAI], decList:JList[Class[_ <: PlayerAI]]) =>
			val base:PlayerAI = baseList.getSelectedValue
			val decs:Iterable[Class[_ <: PlayerAI]] = decList.getSelectedValuesList
			
			decs.foldLeft(base){(base:PlayerAI, dec:Class[_ <: PlayerAI]) =>
				try
				{
					val builder:Constructor[_ <: PlayerAI] = dec.getConstructor(classOf[PlayerAI])
					
					builder.newInstance(base).asInstanceOf[PlayerAI]
				}
				catch
				{
					case e:NoSuchMethodException => throw new ServiceConfigurationError(
							"Class " + dec + " does not have required constructor <init>(PlayerAI)",
							e)
					case e:InstantiationException => throw new ServiceConfigurationError(
							"Class " + dec + " is abstract.", e)
				}
			}
			
		}.tupled).take(players);
	}
	
	this.setLayout(new GridBagLayout())
	this.players = 2;
}

/**
 * @author Raymond Dodge
 * @version ?? ??? ????
 * @version 12 Jul 2012 - adapting due to change in PlayerAI$
 * @deprecated use ScalaSeqListModel(PlayerAI.baseServiceSeq) instead
 */
@deprecated("use ScalaSeqListModel(PlayerAI.baseServiceSeq) instead", "2012 Nov 28")
object AvailibleAIListModel extends AbstractListModel[PlayerAI]
{
	def getElementAt(index:Int):PlayerAI = PlayerAI.baseServiceSeq(index)
	def getSize:Int = PlayerAI.baseServiceSeq.size
}

/**
 * @author Raymond Dodge
 * @version 12 Jul 2012
 * @deprecated use ScalaSeqListModel(PlayerAI.decoratorServiceSeq) instead
 */
@deprecated("use ScalaSeqListModel(PlayerAI.decoratorServiceSeq) instead", "2012 Nov 28")
object AvailibleAIDecoratorListModel extends AbstractListModel[Class[_ <: PlayerAI]]
{
	def getElementAt(index:Int):Class[_ <: PlayerAI] = PlayerAI.decoratorServiceSeq(index)
	def getSize:Int = PlayerAI.decoratorServiceSeq.size
}
