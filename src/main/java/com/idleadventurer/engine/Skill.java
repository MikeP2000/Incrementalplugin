package com.idleadventurer.engine;

public enum Skill
{
	ATTACK("Attack"),
	STRENGTH("Strength"),
	DEFENCE("Defence"),
	HITPOINTS("Hitpoints"),
	PRAYER("Prayer");

	private final String displayName;

	Skill(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** Skills the player can pick to receive combat XP (Hitpoints always gets a share). */
	public boolean isTrainable()
	{
		return this == ATTACK || this == STRENGTH || this == DEFENCE;
	}
}
