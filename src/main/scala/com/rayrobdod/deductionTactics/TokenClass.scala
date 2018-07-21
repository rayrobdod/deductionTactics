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
package com.rayrobdod.deductionTactics

import Elements.Element
import Weaponkinds.Weaponkind
import Statuses.Status
import BodyTypes.BodyType
import Directions.Direction

import scala.collection.immutable.{Seq, Map}

/**
 * A description of the attributes of a unit.
 * 
 * @version next
 * 
 * @constructor
 * @param name A class's name
 * @param body A class's bodytype.
 * @param atkElement The element a unit attacks with. Also determines it's defenses against elements.
 * @param atkWeapon The weapon a unit attacks with.
 * @param atkStatus The status a unit attacks with.
 * @param isSpy Whether the unit can perform the "Spy" action
 * @param range How far away from itself a unit can attack.
 * @param speed How far a unit can move in one turn.
 * @param weakDirection When a unit is attacked from this direction, the attack is strongest
 * @param weakWeapon The weaknesses when a unit is attacked form a type of weapon
 * @param weakStatus When a unit is attacked while suffering this status, the attack is strongest
 */
final case class TokenClass(
	val name:String,
	val body:BodyType,
	val atkElement:Element,
	val atkWeapon:Weaponkind,
	val atkStatus:Status,
	val isSpy:Boolean,
	val range:Int,
	val speed:Int,
	val weakDirection:Direction,
	val weakWeapon:Map[Weaponkind,Float],
	val weakStatus:Status,
	val stanceGroup:TokenClass.StanceGroup
)



/**
 * Loads token classes as a service.
 * 
 * @version next
 */
object TokenClass
{
	/**
	 * Indicates token classes that are alternate stances of eachother
	 * @since a.6.1
	 */
	sealed trait StanceGroup
	/** 
	 * This tokenclass is not related to any other class via stance
	 * @since a.6.1
	 */
	object SingleStanceGroup extends StanceGroup
	/** 
	 * If two TokenClasses have the same MultipleStanceGroup, then it is possible to hotswap between the two
	 * @since a.6.1
	 */
	final class MultipleStanceGroup extends StanceGroup
	
	private[this] final class WeaponWeaknessMap(blade:Float, blunt:Float, spear:Float, whip:Float, powder:Float) extends Map[Weaponkind, Float] {
		override def apply(key:Weaponkind):Float = key match {
			case Weaponkinds.Bladekind => this.blade
			case Weaponkinds.Bluntkind => this.blunt
			case Weaponkinds.Spearkind => this.spear
			case Weaponkinds.Whipkind => this.whip
			case Weaponkinds.Powderkind => this.powder
		}
		def get(key:Weaponkind):Option[Float] = Option(this.apply(key))
		def iterator:Iterator[(Weaponkind, Float)] = Weaponkinds.values.map{x => ((x, this(x)))}.iterator
		def +[B1 >: Float](kv:(Weaponkind, B1)):Map[Weaponkind, B1] = this.iterator.toMap + kv
		def -(k:Weaponkind):Map[Weaponkind, Float] = this.iterator.toMap - k
	}
	
	private[this] def basicTokens:Seq[TokenClass] = {
		val elems:Seq[(Element, String, Status)] = Seq(
			(Elements.Light, "Shining", Statuses.Blind),
			(Elements.Electric, "Static", Statuses.Neuro),
			(Elements.Fire, "Flaming", Statuses.Burn),
			(Elements.Frost, "Frosty", Statuses.Sleep),
			(Elements.Sound, "Sonic", Statuses.Confuse)
		)
		val weapons:Seq[(Weaponkind, String, Status, WeaponWeaknessMap)] = Seq(
			(Weaponkinds.Bladekind, "Swordsman", Statuses.Sleep, new WeaponWeaknessMap(blade = .5f, blunt = .75f, spear = 1.5f, whip = 2f, powder = 1f)),
			(Weaponkinds.Bluntkind, "Clubsman", Statuses.Burn, new WeaponWeaknessMap(blade = 1.5f, blunt = .5f, spear = 2f, whip = 1f, powder = .75f)),
			(Weaponkinds.Spearkind, "Pikeman", Statuses.Blind, new WeaponWeaknessMap(blade = 2f, blunt = 1f, spear = .5f, whip = .75f, powder = 1.5f)),
			(Weaponkinds.Whipkind, "Whipman", Statuses.Confuse, new WeaponWeaknessMap(blade = 1f, blunt = 1.5f, spear = .75f, whip = .5f, powder = 2f)),
			(Weaponkinds.Powderkind, "Powderman", Statuses.Neuro, new WeaponWeaknessMap(blade = .75f, blunt = 2f, spear = 1f, whip = 1.5f, powder = .5f))
		)
		
		for (
			(atkElement, elemNamePart, atkStatus) <- elems;
			(atkWeapon, weaponNamePart, weakStatus, weakWeapon) <- weapons
		) yield {
			TokenClass(
				name = s"$elemNamePart $weaponNamePart",
				atkElement = atkElement,
				atkWeapon = atkWeapon,
				atkStatus = atkStatus,
				weakDirection = Directions.Left,
				weakWeapon = weakWeapon,
				weakStatus = weakStatus,
				
				body = BodyTypes.Humanoid,
				isSpy = false,
				range = 1,
				speed = 3,
				stanceGroup = TokenClass.SingleStanceGroup
			)
		}
	}
	private[this] def birdTokens:Seq[TokenClass] = Seq(
		TokenClass(
			name = "Eagle",
			atkElement = Elements.Fire,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Blind,
			range = 1,
			speed = 4,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 2f, spear = 1f, whip = 1.5f, powder = 0.5f),
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Falcon",
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Blind,
			range = 1,
			speed = 5,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 2f, spear = 0.75f, whip = 1.5f, powder = 0.5f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Penguin",
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Blind,
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			// It can't fly, but the game will act like it can?,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 1.5f, spear = 0.75f, whip = 2f, powder = 0.5f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Turkey",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Blind,
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 2f, spear = 0.75f, whip = 1.5f, powder = 0.5f),
			weakStatus = Statuses.Burn,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Bluebird",
			atkElement = Elements.Sound,
			// songbird, yes?,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Sleep,
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 1.5f, spear = 0.75f, whip = 2f, powder = 0.5f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Cardinal",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Confuse,
			// colors confuse predators trying to go for the eggs,
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 2f, spear = 0.75f, whip = 1.5f, powder = 0.5f),
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Crow",
			// and/or raven,
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Bladekind,
			atkStatus = Statuses.Confuse,
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f, blunt = 2f, spear = 1f, whip = 0.75f, powder = 0.5f),
			weakStatus = Statuses.Neuro,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Parrot",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Sleep,
			range = 2,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f, blunt = 2f, spear = 1f, whip = 1.5f, powder = 0.5f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Thunderbird",
			// Fictional: Native American mythology,
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Neuro,
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 1.5f, spear = 2f, whip = 1f, powder = 0.5f),
			weakStatus = Statuses.Blind,
			// Thunderbolts come from eyes, apparently,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Pigeon",
			atkElement = Elements.Fire,
			// According to Wikipedia, Passenger Pigeons were apparently a major cause of Forest Fires
			// Also, they commonly suffered from migrating too early
			// Also also, in this game, Fire << Frost
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Confuse,
			range = 1,
			speed = 4,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f, blunt = 0.5f, spear = 0.75f, whip = 1f, powder = 2f),
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Owl",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Sleep,
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 1.5f, spear = 0.5f, whip = 0.75f, powder = 2f),
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Phoenix",
			atkElement = Elements.Fire,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Blind,
			// Nope. No Harry Potter reference here.
			range = 1,
			speed = 3,
			body = BodyTypes.Avian,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f, blunt = 1f, spear = 0.75f, whip = 2f, powder = 0.5f),
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		)
	)
	private[this] def sportTokens:Seq[TokenClass] = Seq(
		TokenClass(
			name = "Bowler",
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Burn,
			range = 3,
			speed = 2,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 1f, spear = 2f, whip = 0.5f, powder = 1.5f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Baseball Pitcher",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Confuse,
			range = 2,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 2f, spear = 0.5f, whip = 1.5f, powder = 0.75f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Baseball Batter",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Neuro,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 0.5f, spear = 1.5f, whip = 1f, powder = 1.25f),
			weakStatus = Statuses.Burn,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			// not sure how important direction it - it seems like the first thing someone would find out, after attacking elements
			name = "Lefty Batter",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Neuro,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 0.5f, spear = 1.5f, whip = 1f, powder = 1.25f),
			weakStatus = Statuses.Burn,
			weakDirection = Directions.Right,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Soccer Striker",
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Snake,
			range = 2,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1.25f, blunt = 1.5f, spear = 2f, whip = 1f, powder = 0.5f),
			weakStatus = Statuses.Snake,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Pigskin Quarterback",
			atkElement = Elements.Fire,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Confuse,
			range = 3,
			speed = 2,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 2f, spear = 1.5f, whip = 0.5f, powder = 1f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Pigskin Lineback",
			atkElement = Elements.Fire,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Neuro,
			// flattening is close enough
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 0.5f, spear = 1.5f, whip = 1f, powder = 2f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Hockey Player",
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Bladekind,
			atkStatus = Statuses.Confuse,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f, blunt = 0.75f, spear = 2f, whip = 1f, powder = 0.5f),
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Lacrosse Sticksman",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Whipkind,
			atkStatus = Statuses.Burn,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 0.5f, spear = 1f, whip = 0.75f, powder = 1.5f),
			weakStatus = Statuses.Snake,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Tennis Racketman",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Whipkind,
			atkStatus = Statuses.Burn,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 1f, spear = 1.5f, whip = 0.5f, powder = 2f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Golfer",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Snake,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.5f, blunt = 1f, spear = 2f, whip = 1.5f, powder = 0.75f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Runner",
			// More details about the attack? tvtropes:Main/CrashIntoHello
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Sleep,
			range = 1,
			speed = 5,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1.25f, blunt = 2f, spear = 1f, whip = 0.5f, powder = 1.5f),
			weakStatus = Statuses.Snake,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Volleyball hitter",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Confuse,
			range = 2,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1.25f, blunt = 2f, spear = 1f, whip = 0.5f, powder = 1.5f),
			weakStatus = Statuses.Blind,
			// All lighting comes from the east side, so clearly the sun is that way
			weakDirection = Directions.Right,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		)
	)
	private[this] def miscTokens:Seq[TokenClass] = Seq(
		TokenClass(
			name = "Ninja",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Bladekind,
			atkStatus = Statuses.Confuse,
			range = 1,
			speed = 5,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 0.5f, spear = 1.75f, whip = 2f, powder = 1f),
			weakStatus = Statuses.Neuro,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Farmer",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Snake,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f ,blunt = 0.5f, spear = 0.75f ,whip = 1f, powder = 2f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Umbreallaswordsman",
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Bladekind,
			atkStatus = Statuses.Blind,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f, blunt = 1f, spear = 2f ,whip = 0.75f, powder = 0.5f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Down,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Umbreallagunsman",
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Confuse,
			range = 2,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1.5f, blunt = 1f, spear = 2f ,whip = 1f, powder = 0.75f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Down,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Pyro",
			atkElement = Elements.Fire,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Burn,
			range = 2,
			speed = 2,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 1.5f, spear = 0.5f ,whip = 0.5f, powder = 0.75f),
			weakStatus = Statuses.Neuro,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Torchbearer",
			atkElement = Elements.Fire,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Burn,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 0.5f, spear = 1.5f ,whip = 2f, powder = 1f),
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Yoyoer",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Whipkind,
			atkStatus = Statuses.Confuse,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 0.5f, spear = 1.5f, whip = 1f, powder = 0.75f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Flashlightman",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Blind,
			range = 2,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 1.5f, spear = 2f, whip = 0.75f, powder = 0.5f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Medic",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Bluntkind,
			atkStatus = Statuses.Heal,
			// It's not possible to attack your own units, so this is very counter productive
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.5f, blunt = 0.75f, spear = 2f, whip = 1f, powder = 1.5f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Haunted Hoodie",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Whipkind,
			// Silence shouldn't become an option, but if it did…
			atkStatus = Statuses.Confuse,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 0.5f, spear = 1.5f, whip = 0.75f, powder = 1f),
			// Silence shouldn't become an option, but if it did…
			weakStatus = Statuses.Sleep,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			// This one isn't decorated; a more shocking one later?
			name = "Tiny Evergreen Tree",
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Blind,
			range = 3,
			speed = 2,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 0.75f, spear = 1f, whip = 0.75f, powder = 0.5f),
			weakStatus = Statuses.Burn,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Snowman",
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Sleep,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 1.5f, spear = 0.75f, whip = 0.5f, powder = 2f),
			weakStatus = Statuses.Burn,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			// Holmes
			name = "Detective",
			atkElement = Elements.Light,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Sleep,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 2f, blunt = 1f, spear = 1.5f, whip = 0.5f, powder = 0.75f),
			weakStatus = Statuses.Snake,
			weakDirection = Directions.Left,
			isSpy = true,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			// Ms. Vitch, TF2 Spy
			// I'm debating whether spies should be allowed to attack for damage; if not this specific class should be able to stance change between spy and not-spy
			name = "Infiltrator",
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Bladekind,
			atkStatus = Statuses.Confuse,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 0.75f, blunt = 1.5f, spear = 1f, whip = 2f, powder = 0.5f),
			weakStatus = Statuses.Neuro,
			weakDirection = Directions.Left,
			isSpy = true,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Eavesdropper",
			atkElement = Elements.Sound,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Blind,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 1.5f, spear = 0.75f, whip = 0.5f, powder = 2f),
			weakStatus = Statuses.Confuse,
			weakDirection = Directions.Left,
			isSpy = true,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			// I actually think I'll make the format the other one, where both stances are in the same JsonObject
			// The intention is to make both Rangers alternate stances 
			name = "Ranger (sword)",
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Bladekind,
			atkStatus = Statuses.Blind,
			range = 1,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 0.75f, spear = 2f, whip = 1.5f, powder = 0.5f),
			weakStatus = Statuses.Neuro,
			weakDirection = Directions.Down,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Ranger (arrow)",
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Spearkind,
			atkStatus = Statuses.Blind,
			range = 2,
			speed = 3,
			body = BodyTypes.Humanoid,
			weakWeapon = new WeaponWeaknessMap(blade = 1f, blunt = 0.75f, spear = 2f, whip = 1.5f, powder = 0.5f),
			weakStatus = Statuses.Neuro,
			weakDirection = Directions.Down,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		
		TokenClass(
			name = "Magenta Lion",
			// Entei; Qilin; Magma Lion
			atkElement = Elements.Fire,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Burn,
			range = 1,
			speed = 5,
			body = BodyTypes.Gerbil,
			weakWeapon = new WeaponWeaknessMap(blade = 1.25f, blunt = 0.5f, spear = 1.5f, whip = 1f, powder = 2f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Yellow Tiger",
			// Raikou; Raiju; ???
			atkElement = Elements.Electric,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Neuro,
			range = 1,
			speed = 5,
			body = BodyTypes.Gerbil,
			weakWeapon = new WeaponWeaknessMap(blade = 1.25f, blunt = 0.5f, spear = 1.5f, whip = 1f, powder = 2f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		),
		TokenClass(
			name = "Cyan Leopard",
			// Suicune; ???; Snow Leopard
			atkElement = Elements.Frost,
			atkWeapon = Weaponkinds.Powderkind,
			atkStatus = Statuses.Sleep,
			range = 1,
			speed = 5,
			body = BodyTypes.Gerbil,
			weakWeapon = new WeaponWeaknessMap(blade = 1.25f, blunt = 0.5f, spear = 1.5f, whip = 1f, powder = 2f),
			weakStatus = Statuses.Blind,
			weakDirection = Directions.Left,
			isSpy = false,
			stanceGroup = SingleStanceGroup
		)
	)
	
	val allKnown:Seq[TokenClass] = {
		basicTokens ++
		birdTokens ++
		sportTokens ++
		miscTokens
	}
}
