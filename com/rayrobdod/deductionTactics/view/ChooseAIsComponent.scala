package com.rayrobdod.deductionTactics.view

import scala.collection.immutable.Seq
import javax.swing.{JPanel, BoxLayout, JList, AbstractListModel, JScrollPane, ListSelectionModel}
import javax.swing.BoxLayout.{X_AXIS => boxXAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		VERTICAL_SCROLLBAR_ALWAYS => scrollVerticalAlways,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.PlayerAI
import java.awt.GridLayout

/**
 * @author Raymond Dodge
 * @version 23 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 * @version 12 Jul 2012 - Now shows both the AI bases and the decorators, now that they're availible
 */
class ChooseAIsComponent extends JPanel
{
	val players:Int = 2
	
	val aiLists:Seq[JList[PlayerAI]] = Seq.fill(players){
		val ret = new JList[PlayerAI](AvailibleAIListModel)
		ret.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		ret
	}
	val aiDLists:Seq[JList[Class[_ <: PlayerAI]]] = Seq.fill(players){new JList[Class[_ <: PlayerAI]](AvailibleAIDecoratorListModel)}
	
	this.setLayout(new GridLayout(2, players))
	aiLists.foreach{(list:JList[PlayerAI]) => this.add(new JScrollPane(list, scrollVerticalAsNeeded, scrollHorizontalNever))}
	aiDLists.foreach{(list:JList[Class[_ <: PlayerAI]]) => this.add(new JScrollPane(list, scrollVerticalAsNeeded, scrollHorizontalNever))}
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
			
		}.tupled)
	}
}

/**
 * @author Raymond Dodge
 * @version ?? ??? ????
 * @version 12 Jul 2012 - adapting due to change in PlayerAI$
 */
object AvailibleAIListModel extends AbstractListModel[PlayerAI]
{
	def getElementAt(index:Int):PlayerAI = PlayerAI.baseServiceSeq(index)
	def getSize:Int = PlayerAI.baseServiceSeq.size
}

/**
 * @author Raymond Dodge
 * @version 12 Jul 2012
 */
object AvailibleAIDecoratorListModel extends AbstractListModel[Class[_ <: PlayerAI]]
{
	def getElementAt(index:Int):Class[_ <: PlayerAI] = PlayerAI.decoratorServiceSeq(index)
	def getSize:Int = PlayerAI.decoratorServiceSeq.size
}
