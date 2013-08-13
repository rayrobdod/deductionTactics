#package com.rayrbodod.deductionTactics.ai

from com.rayrobdod.deductionTactics import Player, PlayerAI, TokenClass
from com.rayrobdod.boardGame import EndOfTurn, MoveTo
from scala.util import Random
from scala.collection import Seq

class MoveSouthAI(PlayerAI):
	@java
	def __init__(self): ""
	
	@java(Seq[TokenClass])
	def buildTeam(self):
		clazz = Class.forName("TokenClass$")
		module = clazz.getField("MODULE$")
		
		Random().shuffle(module.allKnown).take(teamSize)
	
	@java(Player, Void)
	def takeTurn(self, player):
		for token in player.tokens:
			token.forward(MoveTo(token.currentSpace.south))
		
		clazz = Class.forName("EndOfTurn$")
		module = clazz.getField("MODULE$")
		
		player.forward(module)
