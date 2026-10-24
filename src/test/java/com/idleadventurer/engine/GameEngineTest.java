package com.idleadventurer.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Random;
import org.junit.Test;

public class GameEngineTest
{
	private static void tick(GameEngine engine, int times)
	{
		for (int i = 0; i < times; i++)
		{
			engine.tick();
		}
	}

	@Test
	public void tickAdvancesTotalTicks()
	{
		GameEngine engine = new GameEngine(new GameState(), new Random(1));
		tick(engine, 2);
		assertEquals(2, engine.getState().totalTicks);
	}

	@Test
	public void startsWalkingThenFights()
	{
		GameEngine engine = new GameEngine(new GameState(), new Random(1));
		assertEquals(Phase.WALKING, engine.getPhase());
		tick(engine, GameEngine.WALK_TICKS);
		assertEquals(Phase.FIGHTING, engine.getPhase());
		assertEquals(engine.getMonster().getHitpoints(), engine.getMonsterHp());
	}

	@Test
	public void killingChickensGivesLootAndXp()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		tick(engine, 600);
		assertTrue(state.kills > 0);
		assertEquals(state.kills, state.bones);
		assertTrue(state.coins >= state.kills * 2);
		assertTrue(state.xp(Skill.STRENGTH) > 0);
		assertTrue(state.xp(Skill.HITPOINTS) > GameState.STARTING_HITPOINTS_XP);
		assertEquals(0, state.xp(Skill.ATTACK), 0);
	}

	@Test
	public void trainingSkillReceivesTheXp()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		engine.setTrainingSkill(Skill.DEFENCE);
		tick(engine, 600);
		assertTrue(state.xp(Skill.DEFENCE) > 0);
		assertEquals(0, state.xp(Skill.STRENGTH), 0);
	}

	@Test
	public void dyingRespawnsAtFullHealth()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(3));
		engine.setTargetMonster("goblin");
		int guard = 0;
		while (state.deaths == 0 && guard++ < 5000)
		{
			engine.tick();
		}
		assertEquals(1, state.deaths);
		assertEquals(Phase.DEAD, engine.getPhase());
		tick(engine, GameEngine.RESPAWN_TICKS);
		assertEquals(Phase.WALKING, engine.getPhase());
		assertEquals(engine.getPlayerMaxHp(), engine.getPlayerHp());
	}

	@Test
	public void changingTargetRestartsTheWalk()
	{
		GameEngine engine = new GameEngine(new GameState(), new Random(1));
		tick(engine, GameEngine.WALK_TICKS);
		engine.setTargetMonster("cow");
		assertEquals(Phase.WALKING, engine.getPhase());
		assertEquals("Cow", engine.getMonster().getName());
	}

	@Test
	public void unknownMonsterAndBrokenSaveFallBackSafely()
	{
		GameState state = new GameState();
		state.targetMonster = "dragon";
		state.skillXp = new double[1];
		state.trainingSkill = Skill.HITPOINTS;
		GameEngine engine = new GameEngine(state, new Random(1));
		assertEquals("Chicken", engine.getMonster().getName());
		assertEquals(Skill.values().length, state.skillXp.length);
		assertEquals(Skill.STRENGTH, state.trainingSkill);
	}

	@Test
	public void buyingGearSpendsCoinsAndEquips()
	{
		GameState state = new GameState();
		state.coins = 100;
		GameEngine engine = new GameEngine(state, new Random(1));
		GearItem scimitar = Gear.get(Slot.WEAPON, 0);
		assertEquals(null, engine.gearBlocker(scimitar));
		assertTrue(engine.buyGear(scimitar));
		assertEquals(100 - scimitar.getCost(), state.coins);
		assertEquals(scimitar, engine.getEquipped(Slot.WEAPON));
		assertEquals(scimitar.getAttackBonus(), engine.attackBonus());
		assertEquals("Already owned", engine.gearBlocker(scimitar));
	}

	@Test
	public void gearNeedsCoinsAndLevel()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		GearItem steel = Gear.get(Slot.WEAPON, 2);
		assertEquals("Needs Attack 5", engine.gearBlocker(steel));
		state.skillXp[Skill.ATTACK.ordinal()] = XpTable.xpForLevel(5);
		assertEquals("Not enough coins", engine.gearBlocker(steel));
		assertEquals(false, engine.buyGear(steel));
		state.coins = steel.getCost();
		assertTrue(engine.buyGear(steel));
		assertEquals(0, state.coins);
	}

	@Test
	public void defenceBonusSumsAllArmour()
	{
		GameState state = new GameState();
		state.coins = 1000;
		GameEngine engine = new GameEngine(state, new Random(1));
		for (Slot slot : Slot.values())
		{
			engine.buyGear(Gear.get(slot, 0));
		}
		assertEquals(4 + 15 + 10 + 8, engine.defenceBonus());
	}

	@Test
	public void eatingHealsConsumesFoodAndDelaysTheNextSwing()
	{
		GameState state = new GameState();
		state.coins = 50;
		GameEngine engine = new GameEngine(state, new Random(1));
		assertEquals(false, engine.eat(0));
		assertTrue(engine.buyFood(0, 5));
		assertEquals(25, state.coins);
		// Full health: eating would be wasted, so nothing is consumed.
		assertEquals(false, engine.eat(0));
		assertEquals(5, state.foodCount[0]);

		engine.setTargetMonster("goblin");
		int guard = 0;
		while (engine.getPlayerHp() == engine.getPlayerMaxHp() && guard++ < 2000)
		{
			engine.tick();
		}
		int before = engine.getPlayerHp();
		assertTrue(engine.eat(0));
		assertEquals(Math.min(engine.getPlayerMaxHp(), before + 3), engine.getPlayerHp());
		assertEquals(4, state.foodCount[0]);
	}

	@Test
	public void cannotBuyFoodWithoutCoins()
	{
		GameEngine engine = new GameEngine(new GameState(), new Random(1));
		assertEquals(false, engine.buyFood(6, 1));
		assertEquals(0, engine.getState().foodCount[6]);
	}

	@Test
	public void olderSavesGetEmptyGearAndFood()
	{
		GameState state = new GameState();
		state.gearTier = null;
		state.foodCount = null;
		GameEngine engine = new GameEngine(state, new Random(1));
		assertEquals(null, engine.getEquipped(Slot.BODY));
		assertEquals(Food.ALL.size(), state.foodCount.length);
	}
}
