package com.idleadventurer.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Something to eat. Healing amounts match OSRS; prices are tuned for the idle pace.
 */
public class Food
{
	public static final List<Food> ALL = Collections.unmodifiableList(Arrays.asList(
		new Food("Shrimps", 3, 5),
		new Food("Trout", 7, 15),
		new Food("Salmon", 9, 40),
		new Food("Lobster", 12, 100),
		new Food("Swordfish", 14, 180),
		new Food("Monkfish", 16, 300),
		new Food("Shark", 20, 600)
	));

	private final String name;
	private final int heal;
	private final int cost;

	private Food(String name, int heal, int cost)
	{
		this.name = name;
		this.heal = heal;
		this.cost = cost;
	}

	public String getName()
	{
		return name;
	}

	public int getHeal()
	{
		return heal;
	}

	public int getCost()
	{
		return cost;
	}
}
