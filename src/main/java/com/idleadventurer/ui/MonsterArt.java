package com.idleadventurer.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * How each monster is drawn: a body shape, two colours and a size. Monsters without an entry get a
 * plain grey humanoid, so adding a monster never breaks the scene.
 */
final class MonsterArt
{
	enum Shape
	{
		HUMANOID, BEAST, DRAGON
	}

	private static final MonsterArt FALLBACK = new MonsterArt(Shape.HUMANOID, new Color(150, 150, 150),
		new Color(90, 90, 90), 1.0);
	private static final Map<String, MonsterArt> ART = new HashMap<>();

	static
	{
		ART.put("giant_rat", new MonsterArt(Shape.BEAST, new Color(120, 100, 85), new Color(220, 170, 170), 0.7));
		ART.put("zombie", new MonsterArt(Shape.HUMANOID, new Color(110, 140, 100), new Color(90, 60, 50), 1.0));
		ART.put("skeleton", new MonsterArt(Shape.HUMANOID, new Color(228, 228, 212), new Color(70, 70, 70), 1.0));
		ART.put("hobgoblin", new MonsterArt(Shape.HUMANOID, new Color(180, 130, 70), new Color(110, 70, 40), 1.05));
		ART.put("hill_giant", new MonsterArt(Shape.HUMANOID, new Color(160, 135, 105), new Color(105, 75, 45), 1.5));
		ART.put("moss_giant", new MonsterArt(Shape.HUMANOID, new Color(100, 140, 80), new Color(60, 90, 50), 1.55));
		ART.put("ice_warrior", new MonsterArt(Shape.HUMANOID, new Color(175, 215, 240), new Color(100, 140, 200), 1.15));
		ART.put("lesser_demon", new MonsterArt(Shape.HUMANOID, new Color(190, 55, 45), new Color(90, 20, 20), 1.25));
		ART.put("blue_dragon", new MonsterArt(Shape.DRAGON, new Color(60, 100, 190), new Color(150, 190, 240), 1.0));
		ART.put("bloodveld", new MonsterArt(Shape.BEAST, new Color(170, 60, 70), new Color(240, 150, 150), 1.2));
		ART.put("gargoyle", new MonsterArt(Shape.HUMANOID, new Color(135, 135, 140), new Color(85, 85, 90), 1.3));
		ART.put("abyssal_demon", new MonsterArt(Shape.HUMANOID, new Color(115, 45, 105), new Color(190, 90, 170), 1.45));

		ART.put("goblin_champion", new MonsterArt(Shape.HUMANOID, new Color(70, 145, 60), new Color(150, 40, 30), 1.3));
		ART.put("zombie_champion", new MonsterArt(Shape.HUMANOID, new Color(70, 105, 65), new Color(110, 50, 130), 1.4));
		ART.put("giant_champion", new MonsterArt(Shape.HUMANOID, new Color(155, 105, 95), new Color(205, 175, 55), 1.75));
		ART.put("demon_champion", new MonsterArt(Shape.HUMANOID, new Color(125, 25, 35), new Color(205, 175, 55), 1.7));
		ART.put("abyssal_sire", new MonsterArt(Shape.DRAGON, new Color(85, 35, 95), new Color(205, 105, 205), 1.45));
	}

	final Shape shape;
	final Color body;
	final Color accent;
	final double scale;

	private MonsterArt(Shape shape, Color body, Color accent, double scale)
	{
		this.shape = shape;
		this.body = body;
		this.accent = accent;
		this.scale = scale;
	}

	static MonsterArt of(String monsterId)
	{
		return ART.getOrDefault(monsterId, FALLBACK);
	}
}
