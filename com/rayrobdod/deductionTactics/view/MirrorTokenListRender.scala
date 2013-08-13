package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics._
import javax.swing.{JList, ListCellRenderer}

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction

/**
 * @author Raymond Dodge
 * @version 06 Feb 2012
 * @version 11 Feb 2012 - fixed an incompatibility issue between this and {@link TokenListRenderer}
 * Doesn't actually work: the CellRendercomponents don't support listeners
 */
object MirrorTokenListRenderer extends ListCellRenderer[Token]
{
	def getListCellRendererComponent(list:JList[_ <: Token], token:Token, index:Int,
			isSelected:Boolean, cellHasFocus:Boolean) =
	{
		val returnValue = TokenListRenderer.getListCellRendererComponent(list, token, index, isSelected, cellHasFocus)
		token match {
			case x:MirrorToken => {
				returnValue.tokenClass.atkWeapon.addMouseListener(new NameAndIconSetterChooserFrameMaker[Element](
						Elements.values, x.tokenClass.atkElement_=_, returnValue))
				returnValue.tokenClass.atkWeapon.addMouseListener(new NameAndIconSetterChooserFrameMaker[Weaponkind](
						Weaponkinds.values, x.tokenClass.atkWeapon_=_, returnValue))
				returnValue.tokenClass.atkStatus.addMouseListener(new NameAndIconSetterChooserFrameMaker[Status](
						Statuses.values, x.tokenClass.atkStatus_=_, returnValue))
				returnValue.tokenClass.weakStatus.addMouseListener(new NameAndIconSetterChooserFrameMaker[Status](
						Statuses.values, x.tokenClass.weakStatus_=_, returnValue))
				returnValue.tokenClass.weakDirection.addMouseListener(new NameAndIconSetterChooserFrameMaker[Direction](
						Directions.values, x.tokenClass.weakDirection_=_, returnValue))
				returnValue.tokenClass.speed.addMouseListener(new IntSetterChooserFrameMaker(
						x.tokenClass.speed_=_, returnValue))
				returnValue.tokenClass.range.addMouseListener(new IntSetterChooserFrameMaker(
						x.tokenClass.range_=_, returnValue))
						
				returnValue.tokenClass.weakRow.setBackground(list.getSelectionForeground)

			}
			case _ => {}
		}
		
		returnValue
	}
}
