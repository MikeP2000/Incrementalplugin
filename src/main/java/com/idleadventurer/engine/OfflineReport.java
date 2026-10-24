package com.idleadventurer.engine;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * What happened while the player was away, for the "while you were away" notice.
 */
public class OfflineReport
{
	/** The numbers worth comparing before and after a stretch of offline play. */
	static class Snapshot
	{
		long kills;
		long deaths;
		long coins;
		int food;
		int potions;
		double[] xp;
		int[] levels;
	}

	private final long ticks;
	private final long kills;
	private final long deaths;
	private final long coins;
	private final int foodUsed;
	private final int potionsUsed;
	private final double[] xpGained;
	private final int[] levelBefore;
	private final int[] levelAfter;

	private OfflineReport(Snapshot before, Snapshot after, long ticks)
	{
		this.ticks = ticks;
		kills = after.kills - before.kills;
		deaths = after.deaths - before.deaths;
		coins = after.coins - before.coins;
		foodUsed = before.food - after.food;
		potionsUsed = before.potions - after.potions;
		xpGained = new double[Skill.values().length];
		for (int i = 0; i < xpGained.length; i++)
		{
			xpGained[i] = after.xp[i] - before.xp[i];
		}
		levelBefore = before.levels;
		levelAfter = after.levels;
	}

	static Snapshot snapshot(GameState state)
	{
		Snapshot snap = new Snapshot();
		snap.kills = state.kills;
		snap.deaths = state.deaths;
		snap.coins = state.coins;
		for (int count : state.foodCount)
		{
			snap.food += count;
		}
		for (int count : state.potionCount)
		{
			snap.potions += count;
		}
		snap.xp = state.skillXp.clone();
		snap.levels = new int[Skill.values().length];
		for (Skill skill : Skill.values())
		{
			snap.levels[skill.ordinal()] = state.level(skill);
		}
		return snap;
	}

	static OfflineReport between(Snapshot before, GameState state, long ticks)
	{
		return new OfflineReport(before, snapshot(state), ticks);
	}

	/** Time covered, in game ticks. */
	public long getTicks()
	{
		return ticks;
	}

	public long getKills()
	{
		return kills;
	}

	public long getDeaths()
	{
		return deaths;
	}

	public long getCoins()
	{
		return coins;
	}

	public int getFoodUsed()
	{
		return foodUsed;
	}

	public int getPotionsUsed()
	{
		return potionsUsed;
	}

	public double getXpGained(Skill skill)
	{
		return xpGained[skill.ordinal()];
	}

	public int getLevelsGained(Skill skill)
	{
		return levelAfter[skill.ordinal()] - levelBefore[skill.ordinal()];
	}

	/** "3h 12m" style description of the time covered. */
	public String describeTime()
	{
		long minutes = ticks * GameEngine.TICK_MILLIS / 60000;
		return minutes >= 60 ? (minutes / 60) + "h " + (minutes % 60) + "m" : minutes + "m";
	}

	/** One line per fact, ready to show in the notice. */
	public List<String> lines()
	{
		NumberFormat numbers = NumberFormat.getIntegerInstance();
		List<String> lines = new ArrayList<>();
		lines.add(numbers.format(kills) + " kills, " + numbers.format(deaths) + (deaths == 1 ? " death" : " deaths"));
		lines.add((coins >= 0 ? "+" : "") + numbers.format(coins) + " coins");
		for (Skill skill : Skill.values())
		{
			double xp = getXpGained(skill);
			if (xp < 1)
			{
				continue;
			}
			int levels = getLevelsGained(skill);
			lines.add(skill.getDisplayName() + " +" + numbers.format(Math.round(xp)) + " xp"
				+ (levels > 0 ? ", lvl " + levelAfter[skill.ordinal()] : ""));
		}
		if (foodUsed > 0 || potionsUsed > 0)
		{
			lines.add("Used " + foodUsed + " food, " + potionsUsed + " potions");
		}
		return lines;
	}
}
