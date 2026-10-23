package com.idleadventurer.engine;

/**
 * Simplified OSRS combat formulas. No equipment bonuses yet, so every bonus term is the base 64.
 */
public final class CombatMath
{
	private static final int BASE_BONUS = 64;

	private CombatMath()
	{
	}

	public static int playerMaxHit(int strengthLevel)
	{
		int effective = strengthLevel + 8;
		return (int) Math.floor(0.5 + effective * BASE_BONUS / 640.0);
	}

	public static double playerHitChance(int attackLevel, int monsterDefenceLevel)
	{
		return hitChance((attackLevel + 8) * BASE_BONUS, (monsterDefenceLevel + 9) * BASE_BONUS);
	}

	public static double monsterHitChance(int monsterAttackLevel, int defenceLevel)
	{
		return hitChance((monsterAttackLevel + 9) * BASE_BONUS, (defenceLevel + 8) * BASE_BONUS);
	}

	private static double hitChance(int attackRoll, int defenceRoll)
	{
		if (attackRoll > defenceRoll)
		{
			return 1 - (defenceRoll + 2.0) / (2.0 * (attackRoll + 1));
		}
		return attackRoll / (2.0 * (defenceRoll + 1));
	}
}
