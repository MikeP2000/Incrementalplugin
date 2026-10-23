package com.idleadventurer.engine;

public enum Slot
{
	WEAPON("Weapon"),
	HELM("Helm"),
	BODY("Body"),
	LEGS("Legs"),
	SHIELD("Shield");

	private final String displayName;

	Slot(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}
}
