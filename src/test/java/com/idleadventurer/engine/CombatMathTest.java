package com.idleadventurer.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CombatMathTest
{
	@Test
	public void maxHitGrowsWithStrength()
	{
		assertEquals(1, CombatMath.playerMaxHit(1, 0));
		assertEquals(2, CombatMath.playerMaxHit(10, 0));
		assertEquals(11, CombatMath.playerMaxHit(99, 0));
	}

	@Test
	public void hitChanceRisesWithAttackLevel()
	{
		double low = CombatMath.playerHitChance(1, 0, 10);
		double high = CombatMath.playerHitChance(50, 0, 10);
		assertTrue(low > 0 && low < high && high < 1);
	}

	@Test
	public void gearBonusesHelp()
	{
		assertTrue(CombatMath.playerMaxHit(40, 44) > CombatMath.playerMaxHit(40, 0));
		assertTrue(CombatMath.playerHitChance(40, 45, 30) > CombatMath.playerHitChance(40, 0, 30));
		assertTrue(CombatMath.monsterHitChance(10, 40, 200) < CombatMath.monsterHitChance(10, 40, 0));
	}

	@Test
	public void equalLevelsStillMiss()
	{
		assertEquals(0.45, CombatMath.playerHitChance(1, 0, 1), 0.01);
	}
}
