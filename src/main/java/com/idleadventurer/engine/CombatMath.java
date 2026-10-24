package com.idleadventurer.engine;

/**
 * Simplified OSRS melee formulas. Each bonus is added to the base 64, as in OSRS.
 */
public final class CombatMath
{
	private static final int BASE_BONUS = 64;

	private CombatMath()
	{
	}

	public static int playerMaxHit(int strengthLevel, int strengthBonus)
	{
		int effective = strengthLevel + 8;
		return (int) Math.floor(0.5 + effective * (strengthBonus + BASE_BONUS) / 640.0);
	}

	public static double playerHitChance(int attackLevel, int attackBonus, int monsterDefenceLevel)
	{
		return hitChance((attackLevel + 8) * (attackBonus + BASE_BONUS), (monsterDefenceLevel + 9) * BASE_BONUS);
	}

	public static double monsterHitChance(int monsterAttackLevel, int monsterAttackBonus, int defenceLevel,
		int defenceBonus)
	{
		return hitChance((monsterAttackLevel + 9) * (monsterAttackBonus + BASE_BONUS),
			(defenceLevel + 8) * (defenceBonus + BASE_BONUS));
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
