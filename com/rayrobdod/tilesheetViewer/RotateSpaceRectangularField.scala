package com.rayrobdod.jsonTilesheetViewer

import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.{SpaceClassConstructor, RectangularField, RectangularSpace}

/**
 * An immutable field that, when asked, produces a new field with a
 * new RectangularSpace in a spot
 * @author Raymond Dodge
 * @version 18 Aug 2011
 * @version 19 Aug 2011 - added single-param rotate function
 * @version 15 Apr 2012 - moved from {@code net.verizon.rayrobdod.boardGame.view} to {@code com.rayrobdod.boardGame.view}
 * @version 15 Apr 2012 - modifying such that it now takes SpaceClassConstructors rather than RectanguleSpaceConstructors
 */
class RotateSpaceRectangularField(
	val rotation:Seq[SpaceClassConstructor],
	val spaceIndexes:Seq[Seq[Int]]) extends RectangularField
{
	def this(rotation:Seq[SpaceClassConstructor], width:Int, height:Int)
	{
		this(rotation, Seq.fill(width, height){0})
	}
	
	override val spaces = spaceIndexes.zipWithIndex.map({(seq:Seq[Int], i:Int) =>
		
		seq.zipWithIndex.map({(index:Int, j:Int) =>
			new RectangularSpace(
				rotation(index)(),
				spaceFuture(i+1,j),
				spaceFuture(i,j-1),
				spaceFuture(i-1,j),
				spaceFuture(i,j+1)
			)
		}.tupled)
	}.tupled)
	
	def rotate(x:Int, y:Int):RotateSpaceRectangularField = 
	{
		val newSpaceIndexes = spaceIndexes.updated(
			x,
			spaceIndexes(x).updated(
				y,
				((spaceIndexes(x)(y) + 1) % rotation.size)
			)
		)
		
		new RotateSpaceRectangularField(rotation, newSpaceIndexes)
	}
	
	def rotate(i:Int):RotateSpaceRectangularField = 
	{
		val lengths:Seq[Int] = spaceIndexes.map{_.size}
		
		val coords:Either[Int,(Int,Int)] = lengths.zipWithIndex.foldLeft[Either[Int,(Int,Int)]](Left(i))
		{(indexesLeftResultRight:Either[Int,(Int,Int)], (lengthI:(Int, Int))) =>
			{
				val (length, i) = lengthI
				
				indexesLeftResultRight match
				{
					case right:Right[_,_] => right 
					case Left(x:Int) => if (x >= length) {
							Left(x - length)
						} else {
							Right((i, x))
						}
				}
			}
		}
		
		coords match
		{
			case x:Left[_,_] => throw new IndexOutOfBoundsException("param was greater than size of array")
			case Right(Tuple2(i:Int,j:Int)) => this.rotate(i,j)
		}
	}
}
