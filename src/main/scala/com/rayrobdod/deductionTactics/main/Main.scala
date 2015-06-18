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

object Main {
	def main(args:Array[String]):Unit = {
		this.startNewGame()
	}
	
	def startNewGame():Unit = {
		val t = new swingView.host.Top()
		t.addNextListener(chooseTokens _)
		t.setVisible(true)
	}
	
	private def chooseTokens(ais:Seq[PlayerAI], map:String, startSpaces:Seq[Seq[(Int, Int)]]):Unit = {
		new Thread(new Runnable(){
			def run() = {
				val selectedClasses:Seq[Seq[TokenClass]] = ais.map{_.selectTokenClasses(5)}
				val narrowedClasses:Seq[Seq[TokenClass]] = ais.zipWithIndex.map{x => x._1.narrowTokenClasses(selectedClasses, 5, x._2)}
				
				System.out.println(map)
				System.out.println(startSpaces)
				System.out.println(narrowedClasses)
			}
		})
	}
}
