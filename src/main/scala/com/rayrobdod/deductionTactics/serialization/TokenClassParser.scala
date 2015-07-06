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
package serialization

import com.rayrobdod.json.builder.Builder

class TokenClassParser[A](builder:Builder[A]) {
	
	def parse(tclass:TokenClass, icon:Option[String]):A = {
		val a:A = builder.init
		val b:A = builder.apply(a, "name", tclass.name)
		val c:A = builder.apply(b, "element", tclass.atkElement.name)
		val d:A = builder.apply(c, "atkWeapon", tclass.atkWeapon.name.dropRight(4))
		val e:A = builder.apply(d, "atkStatus", tclass.atkStatus.name)
		val f:A = builder.apply(e, "body", tclass.body.name)
		val g:A = builder.apply(f, "range", tclass.range)
		val h:A = builder.apply(g, "speed", tclass.speed)
		val i:A = builder.apply(h, "weakStatus", tclass.weakStatus.name)
		val j:A = builder.apply(i, "weakDirection", tclass.weakDirection.name)
		val k:A = builder.apply(j, "weakWeapon", tclass.weakWeapon.map{x => ((x._1.name.dropRight(4), x._2))})
		val l:A = icon.map{x => builder.apply(k, "icon", x)}.getOrElse(k)
		l
	}
}
