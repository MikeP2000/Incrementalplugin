package com.idleadventurer.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A melee prayer: a percentage boost to Attack, Strength and/or Defence while it is active, paid
 * for with prayer points. Levels and percentages match OSRS. Only one prayer may boost a given
 * stat at a time, so turning one on turns off any that share a stat.
 */
public class Prayer
{
	public static final List<Prayer> ALL = Collections.unmodifiableList(Arrays.asList(
		new Prayer("Thick Skin", 1, 0, 0, 0, 5, 1),
		new Prayer("Burst of Strength", 4, 0, 0, 5, 0, 1),
		new Prayer("Clarity of Thought", 7, 0, 5, 0, 0, 1),
		new Prayer("Rock Skin", 10, 0, 0, 0, 10, 6),
		new Prayer("Superhuman Strength", 13, 0, 0, 10, 0, 6),
		new Prayer("Improved Reflexes", 16, 0, 10, 0, 0, 6),
		new Prayer("Steel Skin", 28, 0, 0, 0, 15, 12),
		new Prayer("Ultimate Strength", 31, 0, 0, 15, 0, 12),
		new Prayer("Incredible Reflexes", 34, 0, 15, 0, 0, 12),
		new Prayer("Chivalry", 60, 65, 15, 18, 20, 24),
		new Prayer("Piety", 70, 70, 20, 23, 25, 24)
	));

	private final String name;
	private final int requiredPrayerLevel;
	private final int requiredDefenceLevel;
	private final int attackPercent;
	private final int strengthPercent;
	private final int defencePercent;
	private final int drainEffect;

	private Prayer(String name, int requiredPrayerLevel, int requiredDefenceLevel, int attackPercent,
		int strengthPercent, int defencePercent, int drainEffect)
	{
		this.name = name;
		this.requiredPrayerLevel = requiredPrayerLevel;
		this.requiredDefenceLevel = requiredDefenceLevel;
		this.attackPercent = attackPercent;
		this.strengthPercent = strengthPercent;
		this.defencePercent = defencePercent;
		this.drainEffect = drainEffect;
	}

	public String getName()
	{
		return name;
	}

	public int getRequiredPrayerLevel()
	{
		return requiredPrayerLevel;
	}

	/** Defence level also needed, or 0 for none (Chivalry and Piety). */
	public int getRequiredDefenceLevel()
	{
		return requiredDefenceLevel;
	}

	public int getAttackPercent()
	{
		return attackPercent;
	}

	public int getStrengthPercent()
	{
		return strengthPercent;
	}

	public int getDefencePercent()
	{
		return defencePercent;
	}

	/** Relative drain: 1 costs a prayer point every 3 minutes, 24 (Piety) one every 7.5 seconds. */
	public int getDrainEffect()
	{
		return drainEffect;
	}

	boolean conflictsWith(Prayer other)
	{
		return attackPercent > 0 && other.attackPercent > 0
			|| strengthPercent > 0 && other.strengthPercent > 0
			|| defencePercent > 0 && other.defencePercent > 0;
	}
}
