package com.rayrobdod.deductionTactics
package test

import com.rayrobdod.deductionTactics.consoleView._
import org.scalatest.{FunSuite, BeforeAndAfter}
import scala.collection.immutable.Seq
import com.rayrobdod.boardGame.RectangularField


class CommandParserTest extends FunSuite with BeforeAndAfter
{
	val tokens = new CannonicalListOfTokens(
		Seq(
			Seq(
				new CannonicalToken( new CannonicalTokenClassImpl( "1A" ) ),
				new CannonicalToken( new CannonicalTokenClassImpl( "2A" ) )
			),
			Seq(
				new CannonicalToken( new CannonicalTokenClassImpl( "1B" ) ),
				new CannonicalToken( new CannonicalTokenClassImpl( "2B" ) )
			)
		)
	);
	val field = RectangularField.applySCC(
		Seq(
			Seq( PassibleSpaceClass, PassibleSpaceClass ),
			Seq( PassibleSpaceClass, PassibleSpaceClass )
		)
	);
	
	
	// is immutable, so no need to rebuild every time
	var parser:CommandParser = new CommandParser(tokens, field)
	
	
	
	test("Rejects a blank line")
	{
		val string = ""
		
		try {
			val value = parser.parseCommand(string);
			fail("didn't throw exception");
		} catch {
			case e:IllegalArgumentException => {}
		}
	}
	
	test("Ask for help")
	{
		val string = "HELP"
		
		val value = parser.parseCommand(string);
		assert(value == SupplyHelp)
	}
	
	test("Move token a to token b's space")
	{
		val string = "TOKEN a MOVE TO TOKEN b"
		
		val value = parser.parseCommand(string);
		value match {
			case RequestMove(mover, movee) => {
				assert (mover == tokens.tokens(0)(0) )
				assert (movee == tokens.tokens(0)(1).currentSpace )
			}
			case _ => fail("Did not produce a 'RequestMove' : created a " + value)
		}
	}
	
	test("Move token a to space 1A")
	{
		val string = "TOKEN a MOVE TO SPACE 1A"
		
		val value = parser.parseCommand(string);
		value match {
			case RequestMove(mover, movee) => {
				assert (mover == tokens.tokens(0)(0) )
				assert (movee == field.space(0, 0) )
			}
			case _ => fail("Did not produce a 'RequestMove' : created a " + value)
		}
	}
	
	test("Make Token a attack Token c FOR DAMAGE")
	{
		val string = "TOKEN a ATTACK TOKEN c FOR DAMAGE"
		
		val value = parser.parseCommand(string);
		value match {
			case RequestAttackForDamage(attacker, attackee) => {
				assert (attacker == tokens.tokens(0)(0) )
				assert (attackee == tokens.tokens(1)(0) )
			}
			case _ => fail("Did not produce a 'RequestAttackForDamage' : created a " + value)
		}
	}
	
	test("Make Token b attack Token d FOR STATUS")
	{
		val string = "TOKEN b ATTACK TOKEN d FOR STATUS"
		
		val value = parser.parseCommand(string);
		value match {
			case RequestAttackForStatus(attacker, attackee) => {
				assert (attacker == tokens.tokens(0)(1) )
				assert (attackee == tokens.tokens(1)(1) )
			}
			case _ => fail("Did not produce a 'RequestAttackForDamage' : created a " + value)
		}
	}
	
}






class CannonicalTokenClassImpl(val name:String) extends CannonicalTokenClass
{
	import Elements.Element
	import Weaponkinds.Weaponkind
	import Statuses.Status
	import BodyTypes.{Value => BodyType}
	import Directions.Direction
	import javax.swing.Icon

	def icon:Icon = null
	
	def body:Some[BodyType] = Some(BodyTypes.Humanoid)
	def atkElement:Some[Element] = Some(Elements.Fire)
	def atkWeapon:Some[Weaponkind] = Some(Weaponkinds.Bladekind)
	def atkStatus:Some[Status] = Some(Statuses.Sleep)
	def range:Some[Int] = Some(5)
	def speed:Some[Int] = Some(5)
	
	def weakDirection:Some[Direction] = Some(Directions.Up)
	def weakWeapon:Map[Weaponkind,Some[Float]] = Map.empty
	def weakStatus:Some[Status] = Some(Statuses.Sleep)
}
