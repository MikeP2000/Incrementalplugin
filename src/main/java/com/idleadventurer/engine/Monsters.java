package com.idleadventurer.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The monster roster. Only the Lumbridge zone exists so far; later steps add more zones and bosses.
 */
public final class Monsters
{
	public static final List<Monster> LUMBRIDGE = Collections.unmodifiableList(Arrays.asList(
		new Monster("chicken", "Chicken", 1, 3, 1, 1, 1, 4, 2, 6),
		new Monster("cow", "Cow", 2, 8, 1, 1, 1, 4, 5, 15),
		new Monster("goblin", "Goblin", 5, 13, 1, 5, 2, 4, 10, 30)
	));

	private Monsters()
	{
	}

	/** Falls back to the first monster so an unknown id in a save never breaks the game. */
	public static Monster byId(String id)
	{
		for (Monster monster : LUMBRIDGE)
		{
			if (monster.getId().equals(id))
			{
				return monster;
			}
		}
		return LUMBRIDGE.get(0);
	}
}
