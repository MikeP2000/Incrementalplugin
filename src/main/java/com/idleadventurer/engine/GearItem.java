package com.idleadventurer.engine;

/**
 * One piece of equipment. Weapons give accuracy and strength; armour gives defence.
 */
public class GearItem
{
	private final Slot slot;
	private final int tier;
	private final String name;
	private final Skill requiredSkill;
	private final int requiredLevel;
	private final int attackBonus;
	private final int strengthBonus;
	private final int defenceBonus;
	private final int cost;

	GearItem(Slot slot, int tier, String name, Skill requiredSkill, int requiredLevel,
		int attackBonus, int strengthBonus, int defenceBonus, int cost)
	{
		this.slot = slot;
		this.tier = tier;
		this.name = name;
		this.requiredSkill = requiredSkill;
		this.requiredLevel = requiredLevel;
		this.attackBonus = attackBonus;
		this.strengthBonus = strengthBonus;
		this.defenceBonus = defenceBonus;
		this.cost = cost;
	}

	public Slot getSlot()
	{
		return slot;
	}

	/** Position in the metal ladder, 0 for Bronze. */
	public int getTier()
	{
		return tier;
	}

	public String getName()
	{
		return name;
	}

	public Skill getRequiredSkill()
	{
		return requiredSkill;
	}

	public int getRequiredLevel()
	{
		return requiredLevel;
	}

	public int getAttackBonus()
	{
		return attackBonus;
	}

	public int getStrengthBonus()
	{
		return strengthBonus;
	}

	public int getDefenceBonus()
	{
		return defenceBonus;
	}

	public int getCost()
	{
		return cost;
	}
}
