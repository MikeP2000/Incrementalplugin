package com.idleadventurer.engine;

/**
 * The OSRS experience table: level 99 needs 13,034,431 XP.
 */
public final class XpTable
{
	public static final int MAX_LEVEL = 99;

	private static final int[] XP_FOR_LEVEL = new int[MAX_LEVEL + 2];

	static
	{
		double points = 0;
		for (int level = 1; level <= MAX_LEVEL; level++)
		{
			XP_FOR_LEVEL[level] = (int) Math.floor(points / 4);
			points += Math.floor(level + 300 * Math.pow(2, level / 7.0));
		}
		XP_FOR_LEVEL[MAX_LEVEL + 1] = Integer.MAX_VALUE;
	}

	private XpTable()
	{
	}

	/** Total XP needed to reach the given level (1 to 99). */
	public static int xpForLevel(int level)
	{
		return XP_FOR_LEVEL[Math.max(1, Math.min(level, MAX_LEVEL + 1))];
	}

	public static int levelForXp(double xp)
	{
		int level = 1;
		while (level < MAX_LEVEL && xp >= XP_FOR_LEVEL[level + 1])
		{
			level++;
		}
		return level;
	}
}
