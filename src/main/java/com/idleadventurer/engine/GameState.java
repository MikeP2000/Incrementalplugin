package com.idleadventurer.engine;

/**
 * All persistent progress. Plain fields so Gson can round-trip it; no RuneLite dependencies.
 */
public class GameState
{
	/** Bumped when the save layout changes, so old saves can be migrated. */
	public static final int CURRENT_VERSION = 1;

	public int version = CURRENT_VERSION;

	/** Game ticks (0.6s each) the adventurer has been alive; placeholder counter for the skeleton. */
	public long totalTicks;

	/** Wall-clock time of the last save, used later for offline progress. */
	public long lastSavedMillis;
}
