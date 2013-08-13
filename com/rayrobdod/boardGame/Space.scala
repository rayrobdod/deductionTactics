package com.rayrobdod.boardGame

import javax.swing.JPanel
import scala.swing.Panel
import scala.collection.immutable.{Set, List, Map => IMap}
import scala.collection.mutable.{Map => MMap}
import scala.math.Ordering

/**
 * A spot on a board game board
 * 
 * @author Raymond Dodge
 * @version 6 May 2011
 * @version 30 July 2011 - #movementCost() => #movementCost(Token)
 * @version 29 Sept 2011 - moved methods not related to spacial relations between
 				spaces were moved to a new class SpaceClass
 * @version 20 Oct 2011 - adding #pathTo and #distanceTo
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 * @version 01 Mar 2012 - now, pathTo's algorythm only stores the precending Space in the path, rather than the entire path so far.
 * @version 01 Mar 2012 - adding #pathToEverywhere, which is basically data used by #pathTo and #distanceTo
 * @version 05 Apr 2012 - adding TypeOfCost parameter to cost-related functions, due to change in [[com.rayrobdod.boardGame.SpaceClass]] 
 * @param typeOfSpace the class that defines how this space interacts with Tokens.
 * @see [[com.rayrobdod.boardGame.SpaceClass]] defines the way this interacts with tokens
 */
abstract class Space(val typeOfSpace:SpaceClass)
{
	/**
	 * returns the type of space that this is
	 */
//	def typeOfSpace:SpaceClass
	
	/**
	 * A space that is treated as adjacent to this one; such as a tile that can be directly
	 * accessed from this tile without passing through other tiles
	 */
	def adjacentSpaces:Traversable[Space]
	
	/**
	 * Finds all the spaces within a certain movementCost of this one.
	 * 
	 * @param availableCost the amount of movementCost available
	 * @return a set of all spaces that can be reached from this by moving into an adjacentTile
			using movementCost or less
	 */
	def spacesWithin(availableCost:Int, token:Token, costType:TypeOfCost):Set[Space] =
	{
		if (availableCost < 0) Set.empty
		else if (availableCost == 0) Set(this)
		else
		{
			Set(this) ++ adjacentSpaces.flatMap((space:Space) => {
				space.spacesWithin(
					availableCost - space.typeOfSpace.cost(token, costType),
					token,
					costType
				)
			})
		}
	}
	
	/**
	 * Finds all the spaces that take exactly movementCost to get To.
	 * 
	 * @param availableCost the amount of movementCost available
	 * @return a set of all spaces that can be reached from this by moving into an adjacentTile
			using exactly movementCost
	 */
	def spacesAfter(availableCost:Int, token:Token, costType:TypeOfCost):Set[Space] =
	{
		if (availableCost < 0) Set.empty
		else if (availableCost == 0) Set(this)
		else
		{
			Set.empty ++ adjacentSpaces.flatMap((space:Space) => {
				space.spacesAfter(
					availableCost - space.typeOfSpace.cost(token, costType),
					token,
					costType
				)
			})
		}
	}
	
	/**
	 * Finds the distance between this Space and another Space.
	 * This is Dijkstra's algorithm, as the spaces aren't allowed to know where they are in relation
	 * they are to each other.
	 * 
	 * @param other the space to find the movementCost required to get to
	 * @return the movementCost required to get from this space to other
	 */
	def distanceTo(other:Space, token:Token, costType:TypeOfCost):Int =
	{
		val closed = MMap.empty[Space, Int]
		val open = MMap.empty[Space, Int]
		var checkingTile:(Space, Int) = ((this, 0))
		
		// (Space, Int) is Space and a distance to the tile from this
		
		while (checkingTile._1 != other)
		{
			open -= checkingTile._1
			closed += checkingTile
			
			val newTilesToCheck = checkingTile._1.adjacentSpaces
			val unclosedNewTilesToCheck = newTilesToCheck.filter{! closed.contains(_)}
			
			unclosedNewTilesToCheck.foreach{(s:Space) => {
				val newDistance = checkingTile._2 + s.typeOfSpace.cost(token, costType)
				val oldDistance = open.getOrElse(s, Integer.MAX_VALUE)
				
				if (newDistance < oldDistance) open += ((s, newDistance))
			}}
			
			val ord = new Ordering[(Space, Int)]
			{
				def compare(a:(Space, Int), b:(Space, Int)) = {
					Ordering.Int.compare(a._2, b._2)
				}
			}
			checkingTile = open.min{ord}
		}
		return checkingTile._2
	}
	
	/**
	 * Finds the shortest path from this space to another space
	 * This is Dijkstra's algorithm, as the spaces aren't allowed to know where they are in relation
	 * they are to each other.
	 * 
	 * @param other the space to find the movementCost required to get to
	 * @return the a list of spaces such that the first space is this, the last space is other, and
	 			the movementcost between the two is minimal
	 */ /*
	def pathTo(other:Space, token:Token):List[Space] =
	{
		val closed = MMap.empty[Space, (Int, List[Space])]
		val open = MMap.empty[Space, (Int, List[Space])]
		var checkingTile:(Space, (Int, List[Space])) = ((this, ((0, List(this) )) ))
		
		// (Space, Int) is Space and a distance to the tile from this
		
		while (checkingTile._1 != other)
		{
			open -= checkingTile._1
			closed += checkingTile
			
			val newTilesToCheck = checkingTile._1.adjacentSpaces
			val unclosedNewTilesToCheck = newTilesToCheck.filter{! closed.contains(_)}
			
			unclosedNewTilesToCheck.foreach{(s:Space) => {
				val newDistance = checkingTile._2._1 + s.typeOfSpace.cost(token, costType)
				val oldDistance = open.getOrElse(s, ((Integer.MAX_VALUE, Nil)) )._1
				val newList:List[Space] = s :: checkingTile._2._2
				
				if (newDistance < oldDistance) open += ((s, ((newDistance, newList)) ))
			}}
			
			val ord = new Ordering[(Space, (Int, List[Space]))]
			{
				def compare(a:(Space, (Int, List[Space])), b:(Space, (Int, List[Space]))) = {
					Ordering.Int.compare(a._2._1, b._2._1)
				}
			}
			checkingTile = open.min{ord}
		}
		return (checkingTile._2._2).reverse
	} */
	
	/**
	 * Finds the shortest path from this space to another space
	 * This is Dijkstra's algorithm, as the spaces aren't allowed to know where they are in relation
	 * they are to each other.
	 * 
	 * This short-circuts when it finds the desired space, and so is more efficient than pathToEverywhere
	 * which searches the whole field
	 * 
	 * @param other the space to find the movementCost required to get to
	 * @return the a list of spaces such that the first space is this, the last space is other, and
	 			the movementcost between the two is minimal
	 */
	def pathTo(other:Space, token:Token, costType:TypeOfCost):List[Space] =
	{
		val closed = MMap.empty[Space, (Int, Space)]
		val open = MMap.empty[Space, (Int, Space)]
		var checkingTile:(Space, (Int, Space)) = ((this, ((0, null )) ))
		
		// (Space, Int) is Space and a distance to the tile from this
		
		while (checkingTile._1 != other)
		{
			open -= checkingTile._1
			closed += checkingTile
			
			val newTilesToCheck = checkingTile._1.adjacentSpaces
			val unclosedNewTilesToCheck = newTilesToCheck.filter{! closed.contains(_)}
			
			unclosedNewTilesToCheck.foreach{(s:Space) => {
				val newDistance = checkingTile._2._1 + s.typeOfSpace.cost(token, costType)
				val oldDistance = open.getOrElse(s, ((Integer.MAX_VALUE, None)) )._1
				
				if (newDistance < oldDistance) open += ((s, ((newDistance, checkingTile._1)) ))
			}}
			
			val ord = new Ordering[(Space, (Int, Space))]
			{
				def compare(a:(Space, (Int, Space)), b:(Space, (Int, Space))) = {
					Ordering.Int.compare(a._2._1, b._2._1)
				}
			}
			checkingTile = open.min{ord}
		}
		open -= checkingTile._1
		closed += checkingTile
		
		var currentTile:Space = other
		var returnValue:List[Space] = other :: Nil
		while ((closed(currentTile)._2) != null)
		{
			currentTile = closed(currentTile)._2
			returnValue = currentTile :: returnValue
		}
		return returnValue
	}
	
	/**
	 * Returns the raw Dijkstra's algorithm data
	 * 
	 * @param token the token that is moving from this space to everywhere
	 * @return A map where the key is a space, and the value is the cost from here to the key, and how to get there.
	 */
	def pathToEverywhere(token:Token, costType:TypeOfCost):IMap[Space, (Int, Space)] =
	{
		val closed = MMap.empty[Space, (Int, Space)]
		val open = MMap.empty[Space, (Int, Space)]
		var checkingTile:(Space, (Int, Space)) = ((this, ((0, null )) ))
		
		// (Space, Int) is Space and a distance to the tile from this
		
		while (! open.isEmpty || checkingTile._1 == this)
		{
			closed += checkingTile
			
			val newTilesToCheck = checkingTile._1.adjacentSpaces
			val unclosedNewTilesToCheck = newTilesToCheck.filter{! closed.contains(_)}
			
			unclosedNewTilesToCheck.foreach{(s:Space) => {
				val newDistance = checkingTile._2._1 + s.typeOfSpace.cost(token, costType)
				val oldDistance = open.getOrElse(s, ((Integer.MAX_VALUE, None)) )._1
				
				if (newDistance < oldDistance) open += ((s, ((newDistance, checkingTile._1)) ))
			}}
			
			val ord = new Ordering[(Space, (Int, Space))]
			{
				def compare(a:(Space, (Int, Space)), b:(Space, (Int, Space))) = {
					Ordering.Int.compare(a._2._1, b._2._1)
				}
			}
			checkingTile = open.min{ord}
			open -= checkingTile._1
		}
		closed += checkingTile
		
		return IMap.empty ++ closed
	}
}

/**
 * A [[com.rayrobdod.boardGame.Space]] in which a player can continue in only one direction.
 * 
 * @author Raymond Dodge
 * @version 6 May 2011
 * @version 29 Sept 2011 - modified with super Space; also, no longer abstract
 * @version 15 Dec 2011 - moved from {@code net.verizon.rayrobdod.boardGame} to {@code com.rayrobdod.boardGame}
 * @param typeOfSpace the class that defines how this space interacts with Tokens.
 * @param nextSpace The space a player will continue to after this one 
 */
class UnaryMovement(typeOfSpace:SpaceClass, val nextSpace:Option[Space]) extends Space(typeOfSpace)
{
	/**
	 * Returns a singleton set containing {@link #nextSpace} iff nextSpace is not None; else returns an empty set.
	 */
	override def adjacentSpaces:Set[Space] = nextSpace.toList.toSet
	
	/**
	 * Returns the space a player will reach when using a certain cost.
	 * @param availiableCost the available for movement
	 * @return an Option containing a space if there are nextSpaces until the cost is used up.
	 * @throws ClassCastException if one of the next spaces is not an instance of UnaryMovement, which presumably means
				there are multiple available adjacentSpaces.
	 */
	def spaceAfter(availiableCost:Int, token:Token, costType:TypeOfCost):Option[UnaryMovement] =
	{
		if (availiableCost == 0) Option(this)
		else
		{
			nextSpace match
			{
				case None => None
				case Some(x:UnaryMovement) =>
				{
					if (availiableCost >= x.typeOfSpace.cost(token, costType)) x.spaceAfter(availiableCost - x.typeOfSpace.cost(token, costType), token, costType)
					else None
				}
				case Some(_) => throw new ClassCastException("Encountered something that is not a UnarySpace; ")
			}
		}
	}
}
