package com.idleadventurer.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Random;
import org.junit.Test;

public class AutomationTest
{
	private static void tick(GameEngine engine, int times)
	{
		for (int i = 0; i < times; i++)
		{
			engine.tick();
		}
	}

	private static GameState strongFighter()
	{
		GameState state = new GameState();
		state.skillXp[Skill.ATTACK.ordinal()] = XpTable.xpForLevel(60);
		state.skillXp[Skill.STRENGTH.ordinal()] = XpTable.xpForLevel(60);
		state.skillXp[Skill.DEFENCE.ordinal()] = XpTable.xpForLevel(60);
		return state;
	}

	@Test
	public void autoEatUsesFoodWhenHurt()
	{
		GameState state = new GameState();
		state.unlockedZones = 3;
		state.targetMonster = "moss_giant";
		state.foodCount[Food.ALL.size() - 1] = 500;
		GameEngine engine = new GameEngine(state, new Random(3));
		tick(engine, 1500);
		assertTrue(state.foodCount[Food.ALL.size() - 1] < 500);
	}

	@Test
	public void autoEatOffLeavesFoodAlone()
	{
		GameState state = new GameState();
		state.autoEat = false;
		state.unlockedZones = 3;
		state.targetMonster = "moss_giant";
		state.foodCount[0] = 50;
		GameEngine engine = new GameEngine(state, new Random(3));
		tick(engine, 1500);
		assertEquals(50, state.foodCount[0]);
	}

	@Test
	public void autoEatPicksFoodThatFitsTheMissingHealth()
	{
		GameState state = new GameState();
		state.unlockedZones = 3;
		state.targetMonster = "moss_giant";
		state.foodCount[0] = 100;
		state.foodCount[Food.ALL.size() - 1] = 100;
		state.autoEatPercent = 90;
		GameEngine engine = new GameEngine(state, new Random(5));
		tick(engine, 600);
		// A level 10 adventurer is never missing 20 hit points before dying, so Shark is left for later.
		assertTrue(state.foodCount[0] < 100);
	}

	@Test
	public void autoBuryTurnsBonesIntoPrayerXp()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		tick(engine, 600);
		assertTrue(state.kills > 0);
		assertEquals(0, engine.totalBones());
		assertTrue(state.xp(Skill.PRAYER) > 0);
	}

	@Test
	public void autoBuryOffKeepsBones()
	{
		GameState state = new GameState();
		state.autoBury = false;
		GameEngine engine = new GameEngine(state, new Random(1));
		tick(engine, 600);
		assertEquals(state.kills, engine.totalBones());
	}

	@Test
	public void autoBuyKeepsFoodStocked()
	{
		GameState state = new GameState();
		state.coins = 1000;
		state.autoBuyFood = 0;
		GameEngine engine = new GameEngine(state, new Random(1));
		tick(engine, 5);
		assertEquals(30, state.foodCount[0]);
		assertEquals(1000 - 30 * Food.ALL.get(0).getCost(), state.coins);
	}

	@Test
	public void autoBuyDoesNotOverspend()
	{
		GameState state = new GameState();
		state.coins = 12;
		state.autoBuyFood = 0;
		GameEngine engine = new GameEngine(state, new Random(1));
		tick(engine, 1);
		assertEquals(2, state.foodCount[0]);
		assertEquals(2, state.coins);
	}

	@Test
	public void autoAdvanceMovesToTheNewZone()
	{
		GameState state = strongFighter();
		state.autoAdvance = true;
		state.targetMonster = "goblin_champion";
		GameEngine engine = new GameEngine(state, new Random(2));
		tick(engine, 400);
		assertEquals(2, state.unlockedZones);
		assertEquals("giant_rat", state.targetMonster);
	}

	@Test
	public void withoutAutoAdvanceTheTargetStays()
	{
		GameState state = strongFighter();
		state.targetMonster = "goblin_champion";
		GameEngine engine = new GameEngine(state, new Random(2));
		tick(engine, 400);
		assertEquals(2, state.unlockedZones);
		assertEquals("goblin_champion", state.targetMonster);
	}

	@Test
	public void autoPotionDrinksWhenBoostIsGone()
	{
		GameState state = new GameState();
		state.potionCount[1] = 3; // Strength potion
		GameEngine engine = new GameEngine(state, new Random(1));
		tick(engine, 20);
		assertEquals(2, state.potionCount[1]);
		assertTrue(state.skillBoost[Skill.STRENGTH.ordinal()] > 0);
	}

	@Test
	public void autoPotionOffKeepsPotions()
	{
		GameState state = new GameState();
		state.autoPotion = false;
		state.potionCount[1] = 3;
		GameEngine engine = new GameEngine(state, new Random(1));
		tick(engine, 20);
		assertEquals(3, state.potionCount[1]);
	}

	@Test
	public void togglingPrayersTracksWhatThePlayerWants()
	{
		GameState state = new GameState();
		state.skillXp[Skill.PRAYER.ordinal()] = XpTable.xpForLevel(10);
		state.prayerPoints = 10;
		GameEngine engine = new GameEngine(state, new Random(1));
		assertTrue(engine.togglePrayer(0));
		assertTrue(state.preferredPrayers[0]);
		engine.togglePrayer(0);
		assertFalse(state.preferredPrayers[0]);
	}

	@Test
	public void preferredPrayersComeBackAfterThePointsDoAgain()
	{
		GameState state = new GameState();
		state.skillXp[Skill.PRAYER.ordinal()] = XpTable.xpForLevel(10);
		state.prayerPoints = 10;
		GameEngine engine = new GameEngine(state, new Random(1));
		engine.togglePrayer(0);
		state.activePrayers[0] = false; // as if the points ran out
		state.prayerPoints = 0;
		tick(engine, 2);
		assertFalse(state.activePrayers[0]);
		state.prayerPoints = 5;
		tick(engine, 1);
		assertTrue(state.activePrayers[0]);
	}

	@Test
	public void fastForwardIsCappedAtTwelveHours()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		OfflineReport report = engine.fastForward(Long.MAX_VALUE / 2);
		assertEquals(GameEngine.MAX_OFFLINE_TICKS, report.getTicks());
		assertEquals(GameEngine.MAX_OFFLINE_TICKS, state.totalTicks);
		assertEquals("12h 0m", report.describeTime());
	}

	@Test
	public void fastForwardReportsTheChanges()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		long coinsBefore = state.coins;
		OfflineReport report = engine.fastForward(3000);
		assertEquals(state.kills, report.getKills());
		assertEquals(state.coins - coinsBefore, report.getCoins());
		assertTrue(report.getXpGained(Skill.STRENGTH) > 0);
		assertTrue(report.getLevelsGained(Skill.STRENGTH) > 0);
		assertFalse(report.lines().isEmpty());
	}

	@Test
	public void oldSavesGetSensibleAutomationDefaults()
	{
		GameState state = new GameState();
		state.preferredPrayers = null;
		state.autoEatPercent = 500;
		state.autoBuyFood = 99;
		new GameEngine(state, new Random(1));
		assertEquals(Prayer.ALL.size(), state.preferredPrayers.length);
		assertEquals(90, state.autoEatPercent);
		assertEquals(Food.ALL.size() - 1, state.autoBuyFood);
	}
}
