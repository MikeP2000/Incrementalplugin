package com.idleadventurer.engine;

/**
 * The gear catalogue: Bronze to Rune in five slots. Bonuses are approximated from the OSRS wiki;
 * prices are tuned for the idle pace rather than taken from the Grand Exchange.
 */
public final class Gear
{
	public static final String[] METALS = {"Bronze", "Iron", "Steel", "Black", "Mithril", "Adamant", "Rune"};

	private static final int[] LEVEL_REQUIRED = {1, 1, 5, 10, 20, 30, 40};
	/** Base price per tier; each slot multiplies it. */
	private static final int[] TIER_COST = {25, 75, 250, 800, 2500, 8000, 25000};

	private static final int[] WEAPON_ATTACK = {7, 10, 15, 19, 21, 29, 45};
	private static final int[] WEAPON_STRENGTH = {6, 9, 14, 16, 20, 28, 44};
	private static final int[] HELM_DEFENCE = {4, 7, 11, 13, 18, 23, 30};
	private static final int[] BODY_DEFENCE = {15, 21, 32, 38, 50, 65, 82};
	private static final int[] LEGS_DEFENCE = {10, 14, 21, 26, 33, 43, 51};
	private static final int[] SHIELD_DEFENCE = {8, 12, 18, 22, 29, 38, 45};

	private static final GearItem[][] ITEMS = new GearItem[Slot.values().length][METALS.length];

	static
	{
		for (int tier = 0; tier < METALS.length; tier++)
		{
			String metal = METALS[tier];
			int level = LEVEL_REQUIRED[tier];
			int cost = TIER_COST[tier];
			add(new GearItem(Slot.WEAPON, tier, metal + " scimitar", Skill.ATTACK, level,
				WEAPON_ATTACK[tier], WEAPON_STRENGTH[tier], 0, (int) (cost * 1.2)));
			add(new GearItem(Slot.HELM, tier, metal + " full helm", Skill.DEFENCE, level,
				0, 0, HELM_DEFENCE[tier], (int) (cost * 0.8)));
			add(new GearItem(Slot.BODY, tier, metal + " platebody", Skill.DEFENCE, level,
				0, 0, BODY_DEFENCE[tier], (int) (cost * 2.4)));
			add(new GearItem(Slot.LEGS, tier, metal + " platelegs", Skill.DEFENCE, level,
				0, 0, LEGS_DEFENCE[tier], (int) (cost * 1.6)));
			add(new GearItem(Slot.SHIELD, tier, metal + " kiteshield", Skill.DEFENCE, level,
				0, 0, SHIELD_DEFENCE[tier], (int) (cost * 1.2)));
		}
	}

	private Gear()
	{
	}

	private static void add(GearItem item)
	{
		ITEMS[item.getSlot().ordinal()][item.getTier()] = item;
	}

	public static int tierCount()
	{
		return METALS.length;
	}

	public static GearItem get(Slot slot, int tier)
	{
		return ITEMS[slot.ordinal()][tier];
	}
}
