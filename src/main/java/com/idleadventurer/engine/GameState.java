package com.idleadventurer.engine;

/**
 * All persistent progress. Plain fields so Gson can round-trip it; no RuneLite dependencies.
 * New fields keep their initialisers when an older save lacks them, so adding fields is safe.
 */
public class GameState
{
	/** Bumped when the save layout changes, so old saves can be migrated. */
	public static final int CURRENT_VERSION = 3;

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
	public long bones;
	public long kills;
	public long deaths;

	/** Best tier owned and worn per {@link Slot}, indexed by ordinal; -1 means nothing. */
	public int[] gearTier = newGearTiers();

	/** Food owned per entry of {@link Food#ALL}. */
	public int[] foodCount = new int[Food.ALL.size()];

	/** Id of the monster being hunted. */
	public String targetMonster = "chicken";

	/** Which skill receives the 4 XP per damage. */
	public Skill trainingSkill = Skill.STRENGTH;

	private static int[] newGearTiers()
	{
		int[] tiers = new int[Slot.values().length];
		java.util.Arrays.fill(tiers, -1);
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
		if (targetMonster == null)
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
