package com.idleadventurer.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A kind of bone, buried for Prayer XP. XP per bone matches OSRS.
 */
public class BoneType
{
	public static final List<BoneType> ALL = Collections.unmodifiableList(Arrays.asList(
		new BoneType("Bones", 4.5),
		new BoneType("Big bones", 15),
		new BoneType("Dragon bones", 72)
	));

	private final String name;
	private final double xp;

	private BoneType(String name, double xp)
	{
		this.name = name;
		this.xp = xp;
	}

	public String getName()
	{
		return name;
	}

	public double getXp()
	{
		return xp;
	}
}
