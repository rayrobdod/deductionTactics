package com.rayrobdod.deductionTactics.view

import com.rayrobdod.deductionTactics.Elements.Element
import com.rayrobdod.deductionTactics.Weaponkinds.Weaponkind
import com.rayrobdod.deductionTactics.Statuses.Status
import com.rayrobdod.deductionTactics.BodyTypes.{Value => BodyType}
import com.rayrobdod.deductionTactics.Directions.Direction
import com.rayrobdod.deductionTactics.loadIcon

import javax.swing.{JPanel, JLabel, Icon, JProgressBar,
		DefaultBoundedRangeModel}
import java.awt.{GridBagLayout, GridBagConstraints, FlowLayout}
import com.rayrobdod.deductionTactics.{TokenClass, Weaponkinds}

/**
 * @author Raymond Dodge
 * @version 21 Aug 2011
 * @version 13 Jan 2012 - moved from net.verizon.rayrobdod.deductionTactics.view
			to com.rayrobdod.deductionTactics.view
 * @version 20 Jan 2012 - modifying due to change in TokenClass
 * @version 01 Feb 2012 - implemented paint(Graphics), which updates the children and calls the super
 * @version 14 Feb 2012 - child panels now change color with the TokenClassPanel
 * @version 20 Apr 2012 - reducing the number of anonimous classes by replacing a few identiacl ones with one object
 * @version 05 Jun 2012 - adding JProgressbars indicating weapon weaknesses
 * @version 05 Jun 2012 - changes due to weakWeapon change in [[com.rayrobdod.deductionTactics.TokenClass]]
 * @version 11 Jun 2012 - adding command to update weapon weakness bars
 */
class TokenClassPanel(val tokenClass:TokenClass) extends JPanel
{
	setLayout(new GridBagLayout)
	
	import com.rayrobdod.swing.NameAndIcon
	private object getIcon extends Function1[NameAndIcon, Icon] {
		def apply(x:NameAndIcon) = x.icon
	}
	
	val icon = new JLabel(tokenClass.icon)
	val name = new JLabel(tokenClass.name)
	val range = new JLabel("Range: " + tokenClass.range.getOrElse("?"))
	val speed = new JLabel("Speed: " + tokenClass.speed.getOrElse("?"))
	val atkWeapon = new JLabel(tokenClass.atkWeapon.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
	val atkElement = new JLabel(tokenClass.atkElement.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
	val atkStatus = new JLabel(tokenClass.atkStatus.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
	val weakWeapon = new JLabel(getWeakWeaponIcon())
	val weakStatus = new JLabel(tokenClass.weakStatus.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
	val weakDirection = new JLabel(tokenClass.weakDirection.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
	
	val atkRow = new JPanel()
	atkRow.add(atkElement)
	atkRow.add(atkWeapon)
	atkRow.add(atkStatus)
	atkRow.setBackground(null)
	
	val weakRow = new JPanel()
	weakRow.add(weakDirection)
	weakRow.add(weakWeapon)
	weakRow.add(weakStatus)
	weakRow.setBackground(null)
	
	val weaponWeakPanel = new JPanel(new java.awt.GridLayout(5,1)){
		val addends = Weaponkinds.values.map{(e:Weaponkind) => 
			new JProgressBar(new TokenClassPanel.TokenWeakRangeModel(tokenClass, e)){
				setString(e.name)
			//	setStringPainted(true)
				
				override def paint(g:java.awt.Graphics)
				{
					setIndeterminate(this.getModel.getValueIsAdjusting)
					super.paint(g)
				}
			}
		}
		addends.foreach{this.add(_)}
	}
	
	add(icon, new GridBagConstraints() {gridheight = 2})
	add(name, new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER})
	add(range, new GridBagConstraints())
	add(speed, new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER})
	add(new JLabel("Atk:"), new GridBagConstraints())
	add(atkRow, new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER})
	add(new JLabel("Weak:"), new GridBagConstraints())
	add(weakRow, new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER})
	add(weaponWeakPanel, new GridBagConstraints() {gridwidth = GridBagConstraints.REMAINDER})
	
	def canEquals(other:Any) = other.isInstanceOf[TokenClassPanel]
	override def equals(other:Any) = {
		if (this.canEquals(other))
		{
			val other2 = other.asInstanceOf[TokenClassPanel]
			if (other2.canEquals(this))
			{
				this.tokenClass == other2.tokenClass
			}
			else false
		}
		else false
	}
	
	override def paint(g:java.awt.Graphics)
	{
		this.icon.setIcon(tokenClass.icon)
		this.name.setText(tokenClass.name)
		this.range.setText("Range: " + tokenClass.range.getOrElse("?"))
		this.speed.setText("Speed: " + tokenClass.speed.getOrElse("?"))
		this.atkWeapon.setIcon(tokenClass.atkWeapon.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
		this.atkElement.setIcon(tokenClass.atkElement.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
		this.atkStatus.setIcon(tokenClass.atkStatus.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
		this.weakWeapon.setIcon(getWeakWeaponIcon())
		this.weakStatus.setIcon(tokenClass.weakStatus.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
		this.weakDirection.setIcon(tokenClass.weakDirection.map(getIcon).getOrElse(TokenClassPanel.unknownIcon))
		this.weaponWeakPanel.addends.zip(Weaponkinds.values).foreach(
			{(bar:JProgressBar, e:Weaponkind) =>
				bar.setModel(new TokenClassPanel.TokenWeakRangeModel(tokenClass, e))
		}.tupled)
		super.paint(g)
	}
	
	private def getWeakWeaponIcon() = {
		val maxWeakness = tokenClass.weakWeapon.map{_._2.getOrElse(0f)}.max
		
		if (maxWeakness == 0f) {
			TokenClassPanel.unknownIcon
		} else {
			tokenClass.weakWeapon.maxBy{_._2.getOrElse(0f)}._1.icon
		}
	}
}

object TokenClassPanel
{
	val unknownIcon:Icon = loadIcon(this.getClass().getResource(
			"/com/rayrobdod/glyphs/unknown.svg"))
	
	class TokenWeakRangeModel(tokenClass:TokenClass, kind:Weaponkind) extends DefaultBoundedRangeModel(10, 0, 5, 20)
	{
		(tokenClass.weakWeapon(kind)) match
		{
			case None => this.setValueIsAdjusting(true)
			case Some(x:Float) => this.setValue((x * 10).intValue)
		}
	}
}
