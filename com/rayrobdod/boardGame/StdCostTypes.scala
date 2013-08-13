package com.rayrobdod.boardGame

// to make Ant stop recompiling this file
trait StdCostTypes

/*
 * Types of costs that I can concieve of on short notice
 */
 
/**
 * superclass of types of costs. These are used so that
 * a space can allow different types of actions through at
 * different rates.
 * @author Raymond Dodge
 * @version 05 Apr 2012
 */
trait TypeOfCost

/**
 * The cost of a token moving
 * @author Raymond Dodge
 * @version 01 Apr 2012
 * @version 05 Apr 2012 - extends TypeOfCost now
 */
case object TokenMovementCost extends TypeOfCost

/**
 * The cost of performing a physical attack across an area
 * @author Raymond Dodge
 * @version 01 Apr 2012
 * @version 05 Apr 2012 - extends TypeOfCost now
 */
case object PhysicalStrikeCost extends TypeOfCost

/**
 * The cost of performing a magical attack across an area
 * @author Raymond Dodge
 * @version 01 Apr 2012
 * @version 05 Apr 2012 - extends TypeOfCost now
 */
case object MagicalStrikeCost extends TypeOfCost

/**
 * The cost of sound moving through an area
 * @author Raymond Dodge
 * @version 01 Apr 2012
 * @version 05 Apr 2012 - extends TypeOfCost now
 */
case object SoundPenetrationCost extends TypeOfCost

/**
 * The cost of water moving through an area
 * @author Raymond Dodge
 * @version 01 Apr 2012
 * @version 05 Apr 2012 - extends TypeOfCost now
 */
case object LiquidPenetrationCost extends TypeOfCost
