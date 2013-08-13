package com.rayrobdod.deductionTactics
package consoleView

import com.rayrobdod.deductionTactics.{AttackForDamage, AttackForStatus, Died}
import com.rayrobdod.boardGame.{Moved, Space}
import scala.swing.event.Event
import scala.swing.Reactions.Reaction
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
	val out:java.io.OutputStream = System.out;
	val tokensToLetters = consoleView.tokensToLetters(allTokens)
	val trackedTokenLetter = tokensToLetters(trackedToken)
	
	def apply(e:Event) = {
		val line = e match {
			case Moved(s:Space, _) => {
				trackedTokenLetter + " moved."
			}
			case AttackForDamage(tar:MirrorToken, ele:Element, kind:Weaponkind, _) => {
				val localTar = allTokens.aliveTokens.flatten.filter{_.currentSpace == tar.currentSpace}.head
				
				trackedTokenLetter + " attacked " + tokensToLetters(localTar) + " and dealt " + ele.name + ' ' + kind.name + " damage."
			}
			case AttackForStatus(tar:MirrorToken, sta:Status, from:Space) => {
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
