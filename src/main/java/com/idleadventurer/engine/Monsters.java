package com.idleadventurer.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The monster roster, grouped into five zones. Levels follow a gentle OSRS-flavoured curve and
 * the other stats are derived from the level, so the whole ladder can be tuned from one place.
 */
public final class Monsters
{
	public static final List<Zone> ZONES = Collections.unmodifiableList(Arrays.asList(
		new Zone("Lumbridge", Arrays.asList(
			new Monster("chicken", "Chicken", 1, 3, 1, 1, 1, 4, 2, 6, false),
			new Monster("cow", "Cow", 2, 8, 1, 1, 1, 4, 5, 15, false),
			new Monster("goblin", "Goblin", 5, 13, 1, 5, 2, 4, 10, 30, false),
			boss("goblin_champion", "Goblin Champion", 12))),
		new Zone("Varrock Sewers", Arrays.asList(
			normal("giant_rat", "Giant rat", 9),
			normal("zombie", "Zombie", 14),
			normal("skeleton", "Skeleton", 20),
			boss("zombie_champion", "Zombie Champion", 28))),
		new Zone("Edgeville Dungeon", Arrays.asList(
			normal("hobgoblin", "Hobgoblin", 26),
			normal("hill_giant", "Hill giant", 33),
			normal("moss_giant", "Moss giant", 41),
			boss("giant_champion", "Giant Champion", 55))),
		new Zone("Taverley Dungeon", Arrays.asList(
			normal("ice_warrior", "Ice warrior", 50),
			normal("lesser_demon", "Lesser demon", 60),
			normal("blue_dragon", "Blue dragon", 70),
			boss("demon_champion", "Demon Champion", 92))),
		new Zone("Slayer Tower", Arrays.asList(
			normal("bloodveld", "Bloodveld", 80),
			normal("gargoyle", "Gargoyle", 90),
			normal("abyssal_demon", "Abyssal demon", 100),
			boss("abyssal_sire", "Abyssal Sire", 130)))
	));

	private static final List<Monster> ALL;

	static
	{
		List<Monster> all = new ArrayList<>();
		for (Zone zone : ZONES)
		{
			all.addAll(zone.getMonsters());
		}
		ALL = Collections.unmodifiableList(all);
	}

	private Monsters()
	{
	}

	private static Monster normal(String id, String name, int level)
	{
		int hitpoints = (int) Math.round(3 + 1.3 * level);
		int defence = Math.max(1, (int) Math.round(0.9 * level));
		int maxHit = Math.max(1, (int) Math.round(1 + level / 9.0));
		int coins = (int) Math.round(2.5 * Math.pow(level, 1.5));
		return new Monster(id, name, level, hitpoints, level, defence, maxHit, 4,
			(int) Math.round(coins * 0.6), (int) Math.round(coins * 1.4), false);
	}

	private static Monster boss(String id, String name, int level)
	{
		int hitpoints = (int) Math.round((3 + 1.3 * level) * 2.5);
		int maxHit = (int) Math.round(2 + level / 6.0);
		int coins = (int) Math.round(8 * 2.5 * Math.pow(level, 1.5));
		return new Monster(id, name, level, hitpoints, level, level, maxHit, 4,
			(int) Math.round(coins * 0.6), (int) Math.round(coins * 1.4), true);
	}

	/** Falls back to the first monster so an unknown id in a save never breaks the game. */
	public static Monster byId(String id)
	{
		for (Monster monster : ALL)
		{
			if (monster.getId().equals(id))
			{
				return monster;
			}
		}
		return ZONES.get(0).getMonsters().get(0);
	}

	/** Index into {@link #ZONES} of the zone holding the monster. */
	public static int zoneIndex(Monster monster)
	{
		for (int i = 0; i < ZONES.size(); i++)
		{
			if (ZONES.get(i).getMonsters().contains(monster))
			{
				return i;
			}
		}
		return 0;
	}
}
