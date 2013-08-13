package com.rayrobdod.deductionTactics.view

import javax.swing.{JList, JButton, JPanel, JFrame, JScrollPane, BoxLayout}
import javax.swing.BoxLayout.{Y_AXIS => boxYAxis}
import javax.swing.ScrollPaneConstants.{VERTICAL_SCROLLBAR_AS_NEEDED => scrollVerticalAsNeeded,
		HORIZONTAL_SCROLLBAR_NEVER => scrollHorizontalNever}
import com.rayrobdod.deductionTactics.{TokenClass, CannonicalTokenClass, SuspicionsTokenClass}
import scala.collection.immutable.Seq
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind

/**
 * @author Raymond Dodge
 * @version 29 Feb 2012
 * @version 06 Jun 2012 - change due to change of weakWeapon in tokenClass
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
}

/**
 * @author Raymond Dodge
 * @version 29 Feb 2012
 * @version 06 Jun 2012 - change due to change of weakWeapon in tokenClass
 * @version 11 Jun 2012 - extracted from FilterKnownTokenClassesComponent
 * @version 27 Jun 2012 - changing the numerical equal to checks to a numerical less than or equal to check
 */
class TokenClassMatcher(template:TokenClass) extends Function1[TokenClass,Boolean]
	{
		def apply(tc:TokenClass) = {
			(
				eitherIsNoneOrBothAreEqual(template.atkElement, tc.atkElement) &&
				eitherIsNoneOrBothAreEqual(template.atkWeapon, tc.atkWeapon) &&
				eitherIsNoneOrBothAreEqual(template.atkStatus, tc.atkStatus) &&
				eitherIsNoneOrAIsLessThanOrEqualToB(template.range, tc.range) &&
				eitherIsNoneOrAIsLessThanOrEqualToB(template.speed, tc.speed) &&
				
				eitherIsNoneOrBothAreEqual(template.weakDirection, tc.weakDirection) &&
				eitherIsNoneOrBothAreEqual(template.weakStatus, tc.weakStatus) &&
				template.weakWeapon.keys.forall{(key:Weaponkind) =>
						eitherIsNoneOrBothAreEqual(template.weakWeapon(key), tc.weakWeapon(key))} 
			)
		}
		
		private def eitherIsNoneOrBothAreEqual[A](a:Option[A], b:Option[A]) =
		{
			!a.isDefined || !b.isDefined || (a.get == b.get) 
		}
		
		private def eitherIsNoneOrAIsLessThanOrEqualToB(a:Option[Int], b:Option[Int]) =
		{
			!a.isDefined || !b.isDefined || (a.get <= b.get) 
		}
	}
