package com.idleadventurer.engine;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GameEngineTest
{
	@Test
	public void tickAdvancesTotalTicks()
	{
		GameEngine engine = new GameEngine(new GameState());
		engine.tick();
		engine.tick();
		assertEquals(2, engine.getState().totalTicks);
	}
}
