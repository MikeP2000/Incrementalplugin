package com.idleadventurer.engine;

import java.util.Arrays;

/**
 * All persistent progress. Plain fields so Gson can round-trip it; no RuneLite dependencies.
 * New fields keep their initialisers when an older save lacks them, so adding fields is safe.
 */
public class GameState
{
	/** Bumped when the save layout changes, so old saves can be migrated. */
	public static final int CURRENT_VERSION = 5;

	/** Hitpoints start at level 10, as in OSRS. */
	public static final double STARTING_HITPOINTS_XP = 1154;

	public int version = CURRENT_VERSION;

	/** Game ticks (0.6s each) the adventurer has been alive. */
	public long totalTicks;

	/** Wall-clock time of the last save, used later for offline progress. */
	public long lastSavedMillis;

	/** XP per {@link Skill}, indexed by ordinal. */
	public double[] skillXp = newSkillXp();

	public long coins;

	/** Bones owned per entry of {@link BoneType#ALL}. */
	public long[] boneCount = new long[BoneType.ALL.size()];

	/** Only read from saves made before bones had types; moved into {@link #boneCount} on load. */
	public long bones;
	public long kills;
	public long deaths;

	/** Best tier owned and worn per {@link Slot}, indexed by ordinal; -1 means nothing. */
	public int[] gearTier = newGearTiers();

	/** Food owned per entry of {@link Food#ALL}. */
	public int[] foodCount = new int[Food.ALL.size()];

	/** Current prayer points; the maximum is the Prayer level. */
	public double prayerPoints = 1;

	/** Which prayers are switched on, per entry of {@link Prayer#ALL}. */
	public boolean[] activePrayers = new boolean[Prayer.ALL.size()];

	/** Temporary levels from potions per {@link Skill}, fading by one every minute. */
	public int[] skillBoost = new int[Skill.values().length];

	/** Potions owned per entry of {@link Potion#ALL}. */
	public int[] potionCount = new int[Potion.ALL.size()];

	/** How many zones can be entered; beating a zone's boss unlocks the next. */
	public int unlockedZones = 1;

	/** Id of the monster being hunted. */
	public String targetMonster = "chicken";

	/** Which skill receives the 4 XP per damage. */
	public Skill trainingSkill = Skill.STRENGTH;

	private static int[] newGearTiers()
	{
		int[] tiers = new int[Slot.values().length];
		Arrays.fill(tiers, -1);
		return tiers;
	}

	private static double[] newSkillXp()
	{
		double[] xp = new double[Skill.values().length];
		xp[Skill.HITPOINTS.ordinal()] = STARTING_HITPOINTS_XP;
		return xp;
	}

	/** Repairs fields that a hand-edited or older save may have left unusable. */
	void sanitise()
	{
		if (skillXp == null || skillXp.length != Skill.values().length)
		{
			double[] fixed = newSkillXp();
			if (skillXp != null)
			{
				System.arraycopy(skillXp, 0, fixed, 0, Math.min(skillXp.length, fixed.length));
			}
			skillXp = fixed;
		}
		if (gearTier == null || gearTier.length != Slot.values().length)
		{
			gearTier = newGearTiers();
		}
		for (int i = 0; i < gearTier.length; i++)
		{
			gearTier[i] = Math.max(-1, Math.min(gearTier[i], Gear.tierCount() - 1));
		}
		if (foodCount == null || foodCount.length != Food.ALL.size())
		{
			foodCount = new int[Food.ALL.size()];
		}
		if (trainingSkill == null || !trainingSkill.isTrainable())
		{
			trainingSkill = Skill.STRENGTH;
		}
		if (boneCount == null || boneCount.length != BoneType.ALL.size())
		{
			boneCount = new long[BoneType.ALL.size()];
		}
		boneCount[0] += bones;
		bones = 0;
		if (activePrayers == null || activePrayers.length != Prayer.ALL.size())
		{
			activePrayers = new boolean[Prayer.ALL.size()];
		}
		if (skillBoost == null || skillBoost.length != Skill.values().length)
		{
			skillBoost = new int[Skill.values().length];
		}
		if (potionCount == null || potionCount.length != Potion.ALL.size())
		{
			potionCount = new int[Potion.ALL.size()];
		}
		prayerPoints = Math.max(0, Math.min(prayerPoints, level(Skill.PRAYER)));
		if (prayerPoints <= 0)
		{
			Arrays.fill(activePrayers, false);
		}
		unlockedZones = Math.max(1, Math.min(unlockedZones, Monsters.ZONES.size()));
		if (targetMonster == null || Monsters.zoneIndex(Monsters.byId(targetMonster)) >= unlockedZones)
		{
			targetMonster = "chicken";
		}
		version = CURRENT_VERSION;
	}

	public int level(Skill skill)
	{
		return XpTable.levelForXp(skillXp[skill.ordinal()]);
	}

	public double xp(Skill skill)
	{
		return skillXp[skill.ordinal()];
	}
}
