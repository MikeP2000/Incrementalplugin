package com.idleadventurer.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A potion. Boost potions raise a combat level for a while ("+3 and 10%" style, as in OSRS);
 * the prayer potion restores prayer points. Prices and unlock levels are tuned for the idle pace.
 */
public class Potion
{
	public static final List<Potion> ALL = Collections.unmodifiableList(Arrays.asList(
		boost("Attack potion", Skill.ATTACK, 3, 10, 25, 1),
		boost("Strength potion", Skill.STRENGTH, 3, 10, 25, 1),
		boost("Defence potion", Skill.DEFENCE, 3, 10, 25, 1),
		boost("Super attack", Skill.ATTACK, 5, 15, 120, 30),
		boost("Super strength", Skill.STRENGTH, 5, 15, 120, 30),
		boost("Super defence", Skill.DEFENCE, 5, 15, 120, 30),
		new Potion("Prayer potion", null, 0, 0, 150, Skill.PRAYER, 10)
	));

	private final String name;
	private final Skill boostedSkill;
	private final int flatBoost;
	private final int percentBoost;
	private final int cost;
	private final Skill requiredSkill;
	private final int requiredLevel;

	private Potion(String name, Skill boostedSkill, int flatBoost, int percentBoost, int cost,
		Skill requiredSkill, int requiredLevel)
	{
		this.name = name;
		this.boostedSkill = boostedSkill;
		this.flatBoost = flatBoost;
		this.percentBoost = percentBoost;
		this.cost = cost;
		this.requiredSkill = requiredSkill;
		this.requiredLevel = requiredLevel;
	}

	private static Potion boost(String name, Skill skill, int flat, int percent, int cost, int requiredLevel)
	{
		return new Potion(name, skill, flat, percent, cost, skill, requiredLevel);
	}

	public String getName()
	{
		return name;
	}

	/** The skill raised, or null for the prayer potion. */
	public Skill getBoostedSkill()
	{
		return boostedSkill;
	}

	public boolean isPrayerRestore()
	{
		return boostedSkill == null;
	}

	/** Levels gained for a base level: flat plus a percentage, rounded down. */
	public int boostFor(int baseLevel)
	{
		return flatBoost + baseLevel * percentBoost / 100;
	}

	/** Prayer points restored for a Prayer level: 7 plus a quarter of the level, as in OSRS. */
	public int restoreFor(int prayerLevel)
	{
		return 7 + prayerLevel / 4;
	}

	public int getFlatBoost()
	{
		return flatBoost;
	}

	public int getPercentBoost()
	{
		return percentBoost;
	}

	public int getCost()
	{
		return cost;
	}

	public Skill getRequiredSkill()
	{
		return requiredSkill;
	}

	public int getRequiredLevel()
	{
		return requiredLevel;
	}
}
