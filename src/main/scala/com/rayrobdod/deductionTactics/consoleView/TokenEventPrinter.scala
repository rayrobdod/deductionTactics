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
package com.rayrobdod.deductionTactics
package consoleView

import Elements.Element
import Weaponkinds.Weaponkind
import Directions.Direction
import Statuses.Status

/**
 * 
 * @author Raymond Dodge
 * @version 10 Aug 2012
 */
class TokenEventPrinter(trackedToken:Token, allTokens:ListOfTokens) extends Reaction
{
	private val out:java.io.OutputStream = System.out;
	private val tokensToLetters = consoleView.tokensToLetters(allTokens)
	private val trackedTokenLetter = tokensToLetters(trackedToken)
	
	def apply(e:Event) = {
		val line = e match {
			case Moved(s:Space, _) => {
				trackedTokenLetter + " moved."
			}
			case AttackForDamage(tar:MirrorToken, ele:Element, kind:Weaponkind, _) => {
				val localTar = allTokens.aliveTokens.flatten.filter{_.currentSpace == tar.currentSpace}.head
				
				trackedTokenLetter + " attacked " + tokensToLetters(localTar) + " and dealt " + ele.name + ' ' + kind.name + " damage."
			}
			case AttackForStatus(tar:MirrorToken, sta:Status, _) => {
				val localTar = allTokens.aliveTokens.flatten.filter{_.currentSpace == tar.currentSpace}.head
				
				trackedTokenLetter + " attacked " + tokensToLetters(localTar) + " and inflicted " + sta.name + '.'
			}
			case Died() => {
				trackedTokenLetter + " died!"
			}
		}
		out.write(line.getBytes)
		out.write('\n')
	}
	
	def isDefinedAt(e:Event) = e match {
		case Moved(_,_) => true
		case AttackForDamage(_, _, _, _) => true
		case AttackForStatus(_, _, _) => true
		case Died() => true
		case _ => false
	}
}
