package com.idleadventurer.engine;

import java.util.Collections;
import java.util.List;

/**
 * An area with a few monsters to hunt and a boss at the end. Beating the boss unlocks the next zone.
 */
public class Zone
{
	private final String name;
	private final List<Monster> monsters;

	Zone(String name, List<Monster> monsters)
	{
		this.name = name;
		this.monsters = Collections.unmodifiableList(monsters);
	}

	public String getName()
	{
		return name;
	}

	/** Normal monsters first, the boss last. */
	public List<Monster> getMonsters()
	{
		return monsters;
	}

	public Monster getBoss()
	{
		return monsters.get(monsters.size() - 1);
	}
}
