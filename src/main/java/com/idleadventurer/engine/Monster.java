package com.idleadventurer.engine;

/**
 * Combat stats and loot for one kind of monster. Stats are simplified from the OSRS wiki.
 */
public class Monster
{
	private final String id;
	private final String name;
	private final int combatLevel;
	private final int hitpoints;
	private final int attackLevel;
	private final int defenceLevel;
	private final int maxHit;
	private final int attackSpeedTicks;
	private final int minCoins;
	private final int maxCoins;

	public Monster(String id, String name, int combatLevel, int hitpoints, int attackLevel, int defenceLevel,
		int maxHit, int attackSpeedTicks, int minCoins, int maxCoins)
	{
		this.id = id;
		this.name = name;
		this.combatLevel = combatLevel;
		this.hitpoints = hitpoints;
		this.attackLevel = attackLevel;
		this.defenceLevel = defenceLevel;
		this.maxHit = maxHit;
		this.attackSpeedTicks = attackSpeedTicks;
		this.minCoins = minCoins;
		this.maxCoins = maxCoins;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public int getCombatLevel()
	{
		return combatLevel;
	}

	public int getHitpoints()
	{
		return hitpoints;
	}

	public int getAttackLevel()
	{
		return attackLevel;
	}

	public int getDefenceLevel()
	{
		return defenceLevel;
	}

	public int getMaxHit()
	{
		return maxHit;
	}

	public int getAttackSpeedTicks()
	{
		return attackSpeedTicks;
	}

	public int getMinCoins()
	{
		return minCoins;
	}

	public int getMaxCoins()
	{
		return maxCoins;
	}
}
