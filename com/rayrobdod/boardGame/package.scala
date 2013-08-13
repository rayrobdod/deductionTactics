package com.rayrobdod

package object boardGame
{
	/**
	 * Finds the object with the given name and returns that object. 
	 * @version 04 Oct 2011
	 */
	def getSpaceClassConstructorFromObjectName(objectName:String):SpaceClassConstructor =
	{
		val clazz = Class.forName(objectName + "$")
		val field = clazz.getField("MODULE$")
		
		field.get(null).asInstanceOf[SpaceClassConstructor]
	}
	
	/**
	 * @version 04 Oct 2011
	 */
	def mapValuesFromObjectNameToSpaceClassConstructor(strStrMap:Map[String,String]):Map[String, SpaceClassConstructor] =
	{
		strStrMap.mapValues{getSpaceClassConstructorFromObjectName(_)}
	}
}
