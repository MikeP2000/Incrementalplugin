package com.idleadventurer.engine;

/**
 * Advances the game one tick at a time. Knows nothing about RuneLite or Swing so it can be
 * tested on its own.
 */
public class GameEngine
{
	/** Length of one game tick, matching OSRS. */
	public static final int TICK_MILLIS = 600;

	private final GameState state;

	public GameEngine(GameState state)
	{
		this.state = state;
	}

	public GameState getState()
	{
		return state;
	}

	public void tick()
	{
		state.totalTicks++;
	}
}
