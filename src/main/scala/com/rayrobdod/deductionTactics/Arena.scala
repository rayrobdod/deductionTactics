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

import scala.collection.immutable.{Seq, Set}
import com.rayrobdod.boardGame.RectangularField

/**
 * Represents a board upon which a battle will take place
 * @since a.6.0
 * @version next
 */
final case class Arena(
	val name:String,
	val field:RectangularField[SpaceClass],
	startSpacesSeq:Seq[Seq[Seq[(Int, Int)]]]
) {
	def possiblePlayers:Set[Int] = startSpacesSeq.map{_.size}.toSet
	
	def startSpacesWithPlayerCount(playerCount:Int):Seq[Seq[(Int, Int)]] = startSpacesSeq.filter{_.size == playerCount}.head
	
	override def toString:String = s"Arena[name = $name]" 
}

/**
 * 
 * @version next
 */
object Arena {
	val getAll:Seq[Arena] = Seq(
		Arena(name = "Empty Field",
			field = RectangularField(
				Seq.fill(10, 10)(UniPassageSpaceClass.apply)
			),
			startSpacesSeq = Seq(
				Seq(
					Seq((1 -> 5), (1 -> 3), (1 -> 7), (1 -> 1), (1 -> 9), (1 -> 4), (1 -> 6), (1 -> 2), (1 -> 8), (1 -> 0)),
					Seq((8 -> 5), (8 -> 3), (8 -> 7), (8 -> 1), (8 -> 9), (8 -> 4), (8 -> 6), (8 -> 2), (8 -> 8), (8 -> 0))
				),
				Seq(
					Seq((1 -> 5), (1 -> 3), (1 -> 7), (1 -> 4), (1 -> 6)),
					Seq((8 -> 5), (8 -> 3), (8 -> 7), (8 -> 4), (8 -> 6)),
					Seq((5 -> 1), (3 -> 1), (7 -> 1), (4 -> 1), (6 -> 1)),
					Seq((5 -> 8), (3 -> 8), (7 -> 8), (4 -> 8), (6 -> 8))
				)
			)
		),
		Arena(name = "Pit Arena",
			field = RectangularField(
				Seq(
					"  ||||||||||||||||  ",
					"  :..............:  ",
					"  |..............|  ",
					"  :..          ..:  ",
					"  |..          ..|  ",
					"  :..          ..:  ",
					"  |..          ..|  ",
					"                    ",
					"                    ",
					"  |..          ..|  ",
					"  :..          ..:  ",
					"  |..          ..|  ",
					"  :..          ..:  ",
					"  |..............|  ",
					"  :..............:  ",
					"  ||||||||||||||||  "
				).map{_.map{char => SpaceClassFactory("" + char)}}
			),
			startSpacesSeq = Seq(
				Seq(
					Seq((0 -> 1), (0 -> 2), (0 -> 3), (0 -> 4), (0 -> 5), (0 -> 10), (0 -> 11), (0 -> 12), (0 -> 13), (0 -> 14)),
					Seq((19 -> 14), (19 -> 13), (19 -> 12), (19 -> 11), (19 -> 10), (19 -> 5), (19 -> 4), (19 -> 3), (19 -> 2), (19 -> 1))
				)
			)
		),
		Arena(name = "Tournament Bracket",
			field = RectangularField(
				(for(
					(line, x) <- Seq(
						"|||||||||||||||||||||||||||||",
						"||||||||||||     ||||||||||||",
						"||||||||||||     ||||||||||||",
						"|||||||               |||||||",
						"||||||| ||||     |||| |||||||",
						"|||||     ||     ||     |||||",
						"|||||     |||||||||     |||||",
						"|||         |||||         |||",
						"||| |     | ||||| |     | |||",
						"||| ||||||| ||||| ||||||| |||",
						"|     |||     |     |||     |",
						"|     |||     |     |||     |",
						"|     |||     |     |||     |",
						"|     |||     |     |||     |",
						"|||||||||||||||||||||||||||||"
					).zipWithIndex;
					(char, y) <- line.zipWithIndex
				) yield {
					(x -> y) -> SpaceClassFactory("" + char)
				}).toMap
			),
			startSpacesSeq = Seq(
				Seq(
					Seq(( 2 -> 13), (10 -> 13), (16 -> 13), (24 -> 13)),
					Seq((26 -> 13), (18 -> 13), (12 -> 13), ( 4 -> 13))
				)
			)
		),
		Arena(name = "Tripath",
			field = RectangularField(
				(for(
					(line, x) <- Seq(
						"||||||||||||||||||||",
						"||||||||||||||||||||",
						"||ss |..    ..| ss||",
						"|ss              ss|",
						"|s   |..    ..|   s|",
						"|    ||||::||||    |",
						"|    |..    ..|    |",
						"|                  |",
						"|    |..    ..|    |",
						"|    ||||::||||    |",
						"|s   |..    ..|   s|",
						"|ss              ss|",
						"||ss |..    ..| ss||",
						"||||||||||||||||||||",
						"||||||||||||||||||||"
					).zipWithIndex;
					(char, y) <- line.zipWithIndex
				) yield {
					(x -> y) -> SpaceClassFactory("" + char)
				}).toMap
			),
			startSpacesSeq = Seq(
				Seq(
					Seq((2 -> 7), (2 -> 6), (2 -> 8), (1 -> 6), (1 -> 8)),
					Seq((17 -> 7), (17 -> 6), (17 -> 8), (18 -> 6), (18 -> 8))
				),
				Seq(
					Seq((8 -> 2), (9 -> 2), (10 -> 2), (11 -> 2)),
					Seq((6 -> 7), (7 -> 7), (12 -> 7), (13 -> 7)),
					Seq((8 -> 12), (9 -> 12), (10 -> 12), (11 -> 12))
				)
			)
		)
	)
}
