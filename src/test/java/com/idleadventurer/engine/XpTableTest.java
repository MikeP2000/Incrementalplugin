package com.idleadventurer.engine;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class XpTableTest
{
	@Test
	public void matchesOsrsTable()
	{
		assertEquals(0, XpTable.xpForLevel(1));
		assertEquals(83, XpTable.xpForLevel(2));
		assertEquals(1154, XpTable.xpForLevel(10));
		assertEquals(13034431, XpTable.xpForLevel(99));
	}

	@Test
	public void levelForXpUsesThresholds()
	{
		assertEquals(1, XpTable.levelForXp(82.9));
		assertEquals(2, XpTable.levelForXp(83));
		assertEquals(10, XpTable.levelForXp(GameState.STARTING_HITPOINTS_XP));
		assertEquals(99, XpTable.levelForXp(200_000_000));
	}
}
