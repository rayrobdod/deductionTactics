package com.rayrobdod.deductionTactics

import java.util.logging.{Logger, Level, ConsoleHandler}

private[deductionTactics] object LoggerInitializer
{
	val warningConsoleHander = new ConsoleHandler()
	warningConsoleHander.setLevel(Level.WARNING)
	
	val finerConsoleHander = new ConsoleHandler()
	finerConsoleHander.setLevel(Level.FINER)
	
	val blindAttackAILogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.BlindAttackAI")
	blindAttackAILogger.addHandler(finerConsoleHander)
	blindAttackAILogger.setLevel(Level.WARNING)
	
	val cannonicalTokenLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.CannonicalToken")
	cannonicalTokenLogger.addHandler(finerConsoleHander)
	cannonicalTokenLogger.setLevel(Level.WARNING)
	
	val cannonicalTokenMovementLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.CannonicalToken~RequestMove")
	cannonicalTokenMovementLogger.addHandler(finerConsoleHander)
	cannonicalTokenMovementLogger.setLevel(Level.WARNING)
	
	val sleepAbuserAILogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.SleepAbuserAI")
	sleepAbuserAILogger.addHandler(finerConsoleHander)
	sleepAbuserAILogger.setLevel(Level.WARNING)
	
	val findWeaknessAILogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.FindWeaknessAI")
	findWeaknessAILogger.addHandler(finerConsoleHander)
	findWeaknessAILogger.setLevel(Level.WARNING)
	
	val elementsLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.Elements")
	elementsLogger.addHandler(finerConsoleHander)
	elementsLogger.setLevel(Level.WARNING)
	
	val networkServerLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.WithNetworkServer")
	networkServerLogger.addHandler(finerConsoleHander)
	networkServerLogger.setLevel(Level.FINER)
	
	val networkClientLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.NetworkClient")
	networkClientLogger.addHandler(finerConsoleHander)
	networkClientLogger.setLevel(Level.FINER)
	
	val observeMovementLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.StandardObserveMovement")
	networkClientLogger.addHandler(finerConsoleHander)
	networkClientLogger.setLevel(Level.FINER)
	
}
