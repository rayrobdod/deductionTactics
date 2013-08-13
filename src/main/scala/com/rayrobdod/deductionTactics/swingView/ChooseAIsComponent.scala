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
 * @version 23 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 * @version 12 Jul 2012 - Now shows both the AI bases and the decorators, now that they're availible
 * @version 26 Nov 2012 - Moved from com.rayrobdod.deductionTactics.view to com.rayrobdod.deductionTactics.swingView
 * @version 28 Nov 2012 - Using ScalaSeqListModel intead of custom objects
 * @version 28 Nov 2012 - Making number of players changable
 * @version 19 Jan 2013 - using ++ to remove foreach invocations.
 * @version 28-29 Jan 2013 - Adding JLabels and changing layout from Grid to GridBag.
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
