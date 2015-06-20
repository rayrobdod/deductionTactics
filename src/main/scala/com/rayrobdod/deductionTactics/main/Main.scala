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
package main

import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{Space, RectangularField}

object Main {
	def main(args:Array[String]):Unit = {
		this.startNewGame()
	}
	
	def startNewGame():Unit = {
		val t = new swingView.host.Top()
		t.addNextListener(chooseTokens _)
		t.setVisible(true)
	}
	
	private def chooseTokens(ais:Seq[PlayerAI], map:Arena, startSpaces:Seq[Seq[(Int, Int)]]):Unit = {
		val t = new Thread(new Runnable(){
			def run() = {
				val startCounts = startSpaces.map{_.size}
				val selectedClasses:Seq[Seq[TokenClass]] = ais.zip(startCounts).map{x => x._1.selectTokenClasses(x._2 * 2)}
				// TODO: short circuit and stop if any AI returns an empty list
				val narrowedClasses:Seq[Seq[TokenClass]] = ais.zip(startCounts).zipWithIndex.map{x => x._1._1.narrowTokenClasses(selectedClasses, x._1._2, x._2)}
				// TODO: short circuit and stop if any AI returns an empty list
				
				val field = RectangularField(map.layout)
				val TokenClassToSpaceIndex:Seq[Seq[(TokenClass, (Int, Int))]] = narrowedClasses.zip(startSpaces).map({(x:Seq[TokenClass],y:Seq[(Int, Int)]) => x.zip(y)}.tupled)
				val tokenClassToSpace:Seq[Seq[(Option[TokenClass], Space[SpaceClass])]] = TokenClassToSpaceIndex.map{_.map{(x) => ((Option(x._1), field(x._2._1, x._2._2) ))}}
				
				val tokens = new ListOfTokens( tokenClassToSpace.map{_.map{(x) => new Token(x._2, tokenClass = x._1)}} )
				val initialState = GameState(field, tokens)
				
				// run, meaning this returns when PlayerTurnCycler returns
				new PlayerTurnCycler(ais, initialState).run()
			}
		}, "main.Main.chooseTokens")
		t.start()
	}
}
