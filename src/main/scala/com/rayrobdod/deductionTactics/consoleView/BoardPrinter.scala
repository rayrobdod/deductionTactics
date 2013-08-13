package com.rayrobdod.deductionTactics
package consoleView

import com.rayrobdod.boardGame.{RectangularField, Space, SpaceClass,
		SpaceClassConstructor, mapValuesFromObjectNameToSpaceClassConstructor}
import java.io.InputStreamReader
import com.rayrobdod.javaScriptObjectNotation.parser.listeners.ToScalaCollection
import com.rayrobdod.javaScriptObjectNotation.parser.JSONParser

/**
 * 
 * @author Raymond Dodge
 * @version 10 Aug 2012
 * @version 2013 Jun 23 - responding to changes in ToScalaCollection
 * @todo use the selected token and print out its range
 */
class BoardPrinter(tokens:ListOfTokens, val field:RectangularField)
{
	private val out:java.io.OutputStream = System.out;
	private val tokensToLetters = consoleView.tokensToLetters(tokens)
	
	val spaceClassConsToLetters:Map[SpaceClassConstructor, Char] = {
		val letterToNameMapReader = new InputStreamReader(this.getClass().getResourceAsStream("/com/rayrobdod/deductionTactics/letterMapping.json"))
		val letterToNameMap:Map[String,String] = {
			val listener = ToScalaCollection()
			JSONParser.parse(listener, letterToNameMapReader)
			listener.resultMap.mapValues{_.toString}
		}
		val letterToSpaceClass = mapValuesFromObjectNameToSpaceClassConstructor(letterToNameMap)
		
		letterToSpaceClass.map{_.swap}.mapValues{_(0)}
	}
	
	def printField(){
		val middles = field.spaces.map{_.map{(space:Space) =>
			val tokensOnSpace = tokens.tokens.flatten.filter{_.currentSpace == space}.headOption
			val spaceClassCons = spaceClassConsToLetters.map{_._1}.filter{_.unapply(space.typeOfSpace)}.headOption
			val lastResortChar = '‽'
			
			tokensOnSpace.map{tokensToLetters}.orElse{spaceClassCons.map{
						spaceClassConsToLetters}}.getOrElse{lastResortChar}
		}}
		
		// I'd prefer to use the top line, but the consoles are ASCII only
//		val (tl, tr, bl, br, horiz, vert) = ('┏', '┓', '┗', '┛', '━', '┃')
		val (tl, tr, bl, br, horiz, vert) = (',', '.', '`', '\'', '-', '|')
		
		val topBorder = tl +: Seq.fill(middles.head.length){horiz} :+ tr :+ '\n'
		val midsWithBorder = middles.map{vert +: _ :+ vert :+ '\n'}
		val botBorder = bl +: Seq.fill(middles.last.length){horiz} :+ br :+ '\n' :+ '\n'
		
		val whole = topBorder +: midsWithBorder :+ botBorder
		
		whole.flatten.foreach{out.write(_)}
	}
}
