package com.idleadventurer.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CombatMathTest
{
	@Test
	public void maxHitGrowsWithStrength()
	{
		assertEquals(1, CombatMath.playerMaxHit(1));
		assertEquals(2, CombatMath.playerMaxHit(10));
		assertEquals(11, CombatMath.playerMaxHit(99));
	}

	@Test
	public void hitChanceRisesWithAttackLevel()
	{
		double low = CombatMath.playerHitChance(1, 10);
		double high = CombatMath.playerHitChance(50, 10);
		assertTrue(low > 0 && low < high && high < 1);
	}

	@Test
	public void equalLevelsStillMiss()
	{
		assertEquals(0.45, CombatMath.playerHitChance(1, 1), 0.01);
	}
}
