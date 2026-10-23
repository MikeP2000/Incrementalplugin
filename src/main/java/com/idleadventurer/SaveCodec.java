package com.idleadventurer;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.idleadventurer.engine.GameState;

/**
 * Converts {@link GameState} to and from the JSON string stored in the RuneLite config.
 */
class SaveCodec
{
	private final Gson gson;

	SaveCodec(Gson gson)
	{
		this.gson = gson;
	}

	String encode(GameState state)
	{
		return gson.toJson(state);
	}

	/** Returns a fresh state if there is no save or it cannot be read. */
	GameState decode(String json)
	{
		if (json == null || json.isEmpty())
		{
			return new GameState();
		}
		try
		{
			GameState state = gson.fromJson(json, GameState.class);
			return state != null ? state : new GameState();
		}
		catch (JsonParseException e)
		{
			return new GameState();
		}
	}
}
