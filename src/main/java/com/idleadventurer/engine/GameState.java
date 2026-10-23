package com.idleadventurer.engine;

/**
 * All persistent progress. Plain fields so Gson can round-trip it; no RuneLite dependencies.
 * New fields keep their initialisers when an older save lacks them, so adding fields is safe.
 */
public class GameState
{
	/** Bumped when the save layout changes, so old saves can be migrated. */
	public static final int CURRENT_VERSION = 2;

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

	/** Id of the monster being hunted. */
	public String targetMonster = "chicken";

	/** Which skill receives the 4 XP per damage. */
	public Skill trainingSkill = Skill.STRENGTH;

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
