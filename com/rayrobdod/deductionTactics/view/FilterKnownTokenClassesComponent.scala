package com.rayrobdod.deductionTactics.view

import javax.swing.{JList, JButton, JPanel, JFrame, JScrollPane, BoxLayout}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.{TokenClass, CannonicalTokenClass, SuspicionsTokenClass}
import scala.collection.immutable.Seq

/**
 * @author Raymond Dodge
 * @version 29 Feb 2012
 */
class FilterKnownTokenClassesComponent extends JPanel
{
	val tokenClassesAndComps = CannonicalTokenClass.allKnown.map{(x:TokenClass) => (( x, new TokenClassPanel(x) ))}.toMap
	
	this.setLayout(new BoxLayout(this, boxYAxis))
	this.filter(new SuspicionsTokenClass)
	
	def filter(tokenClass:TokenClass) {
		val applicable = tokenClassesAndComps.filterKeys(new TokenClassMatcher(tokenClass))
		
		this.removeAll()
		applicable.values.foreach{this.add(_)}
	}
	
	class TokenClassMatcher(template:TokenClass) extends Function1[TokenClass,Boolean]
	{
		def apply(tc:TokenClass) = {
			(
				(eitherIsNoneOrBothAreEqual(template.atkElement, tc.atkElement)) &&
				(eitherIsNoneOrBothAreEqual(template.atkWeapon, tc.atkWeapon)) &&
				(eitherIsNoneOrBothAreEqual(template.atkStatus, tc.atkStatus)) &&
				(eitherIsNoneOrBothAreEqual(template.range, tc.range)) &&
				(eitherIsNoneOrBothAreEqual(template.speed, tc.speed)) &&
				
				(eitherIsNoneOrBothAreEqual(template.weakDirection, tc.weakDirection)) &&
				(eitherIsNoneOrBothAreEqual(template.weakWeapon, tc.weakWeapon)) &&
				(eitherIsNoneOrBothAreEqual(template.weakStatus, tc.weakStatus))
			)
		}
		
		private def eitherIsNoneOrBothAreEqual[A](a:Option[A], b:Option[A]) =
		{
			!a.isDefined || !b.isDefined || (a.get == b.get) 
		}
	}
}
