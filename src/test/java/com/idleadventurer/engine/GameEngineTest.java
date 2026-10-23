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
}
