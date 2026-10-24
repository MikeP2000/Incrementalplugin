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
		assertEquals(state.kills, engine.totalBones());
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

	private static GameEngine strongPlayer(GameState state, long seed)
	{
		for (Skill skill : Skill.values())
		{
			state.skillXp[skill.ordinal()] = XpTable.xpForLevel(80);
		}
		state.coins = 10_000_000;
		GameEngine engine = new GameEngine(state, new Random(seed));
		for (Slot slot : Slot.values())
		{
			for (int tier = 0; tier < Gear.tierCount(); tier++)
			{
				engine.buyGear(Gear.get(slot, tier));
			}
		}
		state.foodCount[Food.ALL.size() - 1] = 1000;
		return engine;
	}

	private static void tickUntilKills(GameEngine engine, long kills)
	{
		int shark = Food.ALL.size() - 1;
		int guard = 0;
		while (engine.getState().kills < kills && guard++ < 50_000)
		{
			engine.tick();
			if (engine.getPlayerHp() * 2 <= engine.getPlayerMaxHp())
			{
				engine.eat(shark);
			}
		}
		assertTrue("fight never finished", engine.getState().kills >= kills);
	}

	@Test
	public void lockedZonesCannotBeEntered()
	{
		GameEngine engine = new GameEngine(new GameState(), new Random(1));
		engine.setTargetMonster("zombie");
		assertEquals("chicken", engine.getState().targetMonster);
		assertEquals("Chicken", engine.getMonster().getName());
	}

	@Test
	public void beatingABossUnlocksTheNextZoneOnce()
	{
		GameState state = new GameState();
		GameEngine engine = strongPlayer(state, 1);
		assertEquals(1, state.unlockedZones);
		engine.setTargetMonster("goblin_champion");
		tickUntilKills(engine, 1);
		assertEquals(2, state.unlockedZones);
		assertEquals("Varrock Sewers", engine.getUnlockNotice());

		engine.setTargetMonster("zombie");
		assertEquals("Zombie", engine.getMonster().getName());

		// Killing the same boss again does not skip ahead.
		engine.setTargetMonster("goblin_champion");
		tickUntilKills(engine, 2);
		assertEquals(2, state.unlockedZones);
	}

	@Test
	public void unlockNoticeFades()
	{
		GameState state = new GameState();
		GameEngine engine = strongPlayer(state, 1);
		engine.setTargetMonster("goblin_champion");
		tickUntilKills(engine, 1);
		tick(engine, GameEngine.UNLOCK_NOTICE_TICKS);
		assertEquals(null, engine.getUnlockNotice());
	}

	@Test
	public void savedTargetInALockedZoneFallsBack()
	{
		GameState state = new GameState();
		state.targetMonster = "abyssal_sire";
		GameEngine engine = new GameEngine(state, new Random(1));
		assertEquals("Chicken", engine.getMonster().getName());
	}

	@Test
	public void everyZoneEndsWithItsOnlyBoss()
	{
		for (Zone zone : Monsters.ZONES)
		{
			int bosses = 0;
			for (Monster monster : zone.getMonsters())
			{
				if (monster.isBoss())
				{
					bosses++;
				}
			}
			assertEquals(1, bosses);
			assertTrue(zone.getBoss().isBoss());
		}
	}

	private static void setLevel(GameState state, Skill skill, int level)
	{
		state.skillXp[skill.ordinal()] = XpTable.xpForLevel(level);
	}

	@Test
	public void buryingBonesGivesPrayerXpAndPoints()
	{
		GameState state = new GameState();
		state.boneCount[0] = 20;
		state.boneCount[2] = 1;
		GameEngine engine = new GameEngine(state, new Random(1));
		assertEquals(21, engine.buryBones());
		assertEquals(0, engine.totalBones());
		assertEquals(20 * 4.5 + 72, state.xp(Skill.PRAYER), 0.001);
		assertEquals(state.level(Skill.PRAYER), engine.getPrayerMaxPoints());
		// Each level gained also adds a point to the pool.
		assertEquals(engine.getPrayerMaxPoints(), engine.getPrayerPoints(), 0.001);
		assertEquals(0, engine.buryBones());
	}

	@Test
	public void killsDropTheMonstersBoneType()
	{
		GameState state = new GameState();
		GameEngine engine = strongPlayer(state, 1);
		state.unlockedZones = 5;
		engine.setTargetMonster("abyssal_demon");
		tickUntilKills(engine, 1);
		assertEquals(1, state.boneCount[2]);
		assertEquals(0, state.boneCount[0]);
	}

	@Test
	public void prayersNeedLevelAndPoints()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		int piety = Prayer.ALL.size() - 1;
		assertEquals("Needs Prayer 70", engine.prayerBlocker(piety));
		assertEquals(false, engine.togglePrayer(piety));
		assertEquals(null, engine.prayerBlocker(0));
		assertTrue(engine.togglePrayer(0));
		assertTrue(engine.isPrayerActive(0));
		assertTrue(engine.togglePrayer(0));
		assertEquals(false, engine.isPrayerActive(0));
	}

	@Test
	public void chivalryAndPietyAlsoNeedDefence()
	{
		GameState state = new GameState();
		setLevel(state, Skill.PRAYER, 70);
		state.prayerPoints = 70;
		GameEngine engine = new GameEngine(state, new Random(1));
		int piety = Prayer.ALL.size() - 1;
		assertEquals("Needs Defence 70", engine.prayerBlocker(piety));
		setLevel(state, Skill.DEFENCE, 70);
		assertEquals(null, engine.prayerBlocker(piety));
	}

	@Test
	public void prayersOnTheSameStatReplaceEachOther()
	{
		GameState state = new GameState();
		setLevel(state, Skill.PRAYER, 20);
		state.prayerPoints = 20;
		GameEngine engine = new GameEngine(state, new Random(1));
		engine.togglePrayer(1); // Burst of Strength
		engine.togglePrayer(0); // Thick Skin: different stat, both stay on
		assertTrue(engine.isPrayerActive(0) && engine.isPrayerActive(1));
		engine.togglePrayer(4); // Superhuman Strength replaces Burst
		assertEquals(false, engine.isPrayerActive(1));
		assertTrue(engine.isPrayerActive(4));
		assertTrue(engine.isPrayerActive(0));
	}

	@Test
	public void prayerBoostsEffectiveLevels()
	{
		GameState state = new GameState();
		setLevel(state, Skill.PRAYER, 70);
		setLevel(state, Skill.DEFENCE, 70);
		setLevel(state, Skill.STRENGTH, 100 - 1);
		state.prayerPoints = 70;
		GameEngine engine = new GameEngine(state, new Random(1));
		assertEquals(99, engine.effectiveLevel(Skill.STRENGTH));
		engine.togglePrayer(Prayer.ALL.size() - 1); // Piety
		assertEquals(99 * 123 / 100, engine.effectiveLevel(Skill.STRENGTH));
		assertEquals(70 * 125 / 100, engine.effectiveLevel(Skill.DEFENCE));
	}

	@Test
	public void prayerDrainsAndSwitchesOffAtZero()
	{
		GameState state = new GameState();
		GameEngine engine = new GameEngine(state, new Random(1));
		engine.togglePrayer(0); // Thick Skin, 1 point every 300 ticks
		tick(engine, 150);
		assertEquals(0.5, engine.getPrayerPoints(), 0.01);
		assertTrue(engine.isPrayerActive(0));
		tick(engine, 150);
		assertEquals(0, engine.getPrayerPoints(), 0.001);
		assertEquals(false, engine.isPrayerActive(0));
		assertEquals("No prayer points", engine.prayerBlocker(0));
	}

	@Test
	public void potionsNeedLevelAndCoins()
	{
		GameState state = new GameState();
		state.coins = 1000;
		GameEngine engine = new GameEngine(state, new Random(1));
		int superAttack = 3;
		assertEquals("Needs Attack 30", engine.potionBlocker(superAttack, 1));
		assertEquals(false, engine.buyPotion(superAttack, 1));
		setLevel(state, Skill.ATTACK, 30);
		assertTrue(engine.buyPotion(superAttack, 2));
		assertEquals(1000 - 240, state.coins);
		assertEquals(2, state.potionCount[superAttack]);
		assertEquals("Not enough coins", engine.potionBlocker(superAttack, 50));
	}

	@Test
	public void boostPotionsRaiseDecayAndAreNotWasted()
	{
		GameState state = new GameState();
		state.coins = 100;
		setLevel(state, Skill.STRENGTH, 40);
		GameEngine engine = new GameEngine(state, new Random(1));
		assertTrue(engine.buyPotion(1, 2)); // Strength potion: +3 and 10%
		assertTrue(engine.drink(1));
		assertEquals(3 + 4, engine.getBoost(Skill.STRENGTH));
		assertEquals(47, engine.effectiveLevel(Skill.STRENGTH));
		// Already at full boost: drinking again would waste it.
		assertEquals(false, engine.drink(1));
		assertEquals(1, state.potionCount[1]);
		tick(engine, GameEngine.BOOST_DECAY_TICKS);
		assertEquals(6, engine.getBoost(Skill.STRENGTH));
		tick(engine, GameEngine.BOOST_DECAY_TICKS * 10);
		assertEquals(0, engine.getBoost(Skill.STRENGTH));
	}

	@Test
	public void prayerPotionRestoresUpToTheMaximum()
	{
		GameState state = new GameState();
		state.coins = 1000;
		setLevel(state, Skill.PRAYER, 40);
		state.prayerPoints = 5;
		GameEngine engine = new GameEngine(state, new Random(1));
		int prayerPotion = Potion.ALL.size() - 1;
		assertTrue(engine.buyPotion(prayerPotion, 3));
		assertTrue(engine.drink(prayerPotion));
		assertEquals(5 + 7 + 10, engine.getPrayerPoints(), 0.001);
		assertTrue(engine.drink(prayerPotion));
		assertTrue(engine.drink(prayerPotion));
		assertEquals(40, engine.getPrayerPoints(), 0.001);
		assertEquals(false, engine.drink(prayerPotion));
	}

	@Test
	public void dyingSwitchesPrayersOffAndWearsOffBoosts()
	{
		GameState state = new GameState();
		state.coins = 100;
		GameEngine engine = new GameEngine(state, new Random(3));
		engine.buyPotion(0, 1);
		engine.drink(0);
		engine.togglePrayer(0);
		state.unlockedZones = 5;
		engine.setTargetMonster("bloodveld");
		int guard = 0;
		while (state.deaths == 0 && guard++ < 5000)
		{
			engine.tick();
		}
		assertEquals(1, state.deaths);
		assertEquals(false, engine.isPrayerActive(0));
		assertEquals(0, engine.getBoost(Skill.ATTACK));
		tick(engine, GameEngine.RESPAWN_TICKS);
		assertEquals(engine.getPrayerMaxPoints(), engine.getPrayerPoints(), 0.001);
	}

	@Test
	public void oldUntypedBonesMoveIntoPlainBones()
	{
		GameState state = new GameState();
		state.bones = 12;
		state.boneCount = null;
		state.activePrayers = null;
		state.skillBoost = null;
		state.potionCount = null;
		new GameEngine(state, new Random(1));
		assertEquals(12, state.boneCount[0]);
		assertEquals(0, state.bones);
		assertEquals(Prayer.ALL.size(), state.activePrayers.length);
	}

	@Test
	public void oldSaveWithoutPrayerSkillLoads()
	{
		GameState state = new GameState();
		state.skillXp = new double[]{100, 200, 300, 1500};
		new GameEngine(state, new Random(1));
		assertEquals(Skill.values().length, state.skillXp.length);
		assertEquals(1500, state.xp(Skill.HITPOINTS), 0);
		assertEquals(0, state.xp(Skill.PRAYER), 0);
	}
}
