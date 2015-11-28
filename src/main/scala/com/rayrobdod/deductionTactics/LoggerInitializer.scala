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

import java.util.logging.{Logger, Level, ConsoleHandler}

private[deductionTactics] object LoggerInitializer
{
	val warningConsoleHander = new ConsoleHandler()
	warningConsoleHander.setLevel(Level.WARNING)
	
	val finerConsoleHander = new ConsoleHandler()
	finerConsoleHander.setLevel(Level.FINER)
	
	
	val turnCyclerLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.PlayerTurnCycler")
	turnCyclerLogger.addHandler(finerConsoleHander)
	turnCyclerLogger.setLevel(Level.WARNING)
	
	val elementsLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.Elements")
	elementsLogger.addHandler(finerConsoleHander)
	elementsLogger.setLevel(Level.WARNING)
	
	
	val mouseListenerLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.swingView.MoveTokenMouseListener")
	mouseListenerLogger.addHandler(finerConsoleHander)
	mouseListenerLogger.setLevel(Level.WARNING)
	
	val observeMovementLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.StandardObserveMovement")
	observeMovementLogger.addHandler(finerConsoleHander)
	observeMovementLogger.setLevel(Level.WARNING)
	
	val blindAttackAILogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.BlindAttackAI")
	blindAttackAILogger.addHandler(finerConsoleHander)
	blindAttackAILogger.setLevel(Level.WARNING)
	
	val sleepAbuserAILogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.SleepAbuserAI")
	sleepAbuserAILogger.addHandler(finerConsoleHander)
	sleepAbuserAILogger.setLevel(Level.FINER)
	
	val findWeaknessAILogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.FindWeaknessAI")
	findWeaknessAILogger.addHandler(finerConsoleHander)
	findWeaknessAILogger.setLevel(Level.WARNING)
	
	val networkServerLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.WithNetworkServer")
	networkServerLogger.addHandler(finerConsoleHander)
	networkServerLogger.setLevel(Level.WARNING)
	
	val networkClientLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.NetworkClient")
	networkClientLogger.addHandler(finerConsoleHander)
	networkClientLogger.setLevel(Level.WARNING)
	
	val fieldPotentialAiLogger = Logger.getLogger(
			"com.rayrobdod.deductionTactics.ai.FieldPotentialAI")
	fieldPotentialAiLogger.addHandler(finerConsoleHander)
	fieldPotentialAiLogger.setLevel(Level.FINER)
	
}
