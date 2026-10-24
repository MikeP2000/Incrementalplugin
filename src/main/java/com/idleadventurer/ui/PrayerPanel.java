package com.idleadventurer.ui;

import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.GameState;
import com.idleadventurer.engine.Potion;
import com.idleadventurer.engine.Prayer;
import com.idleadventurer.engine.Skill;
import com.idleadventurer.engine.XpTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import net.runelite.client.ui.ColorScheme;

/**
 * The Prayer tab: prayer points, burying bones, the prayer potion and the prayer list.
 * Only touched on the Swing event thread.
 */
public class PrayerPanel extends JPanel
{
	private static final Color BLOCKED = new Color(230, 110, 90);

	private final GameEngine engine;
	private final NumberFormat numbers = NumberFormat.getIntegerInstance();
	private final JLabel pointsLabel = new JLabel();
	private final JProgressBar pointsBar = new JProgressBar(0, 1000);
	private final JProgressBar levelBar = new JProgressBar(0, 1000);
	private final JLabel levelLabel = new JLabel();
	private final JButton buryButton = new JButton();
	private final JButton potionButton = new JButton();
	private final int prayerPotionIndex = Potion.ALL.size() - 1;
	private final List<PrayerRow> rows = new ArrayList<>();

	public PrayerPanel(GameEngine engine)
	{
		this.engine = engine;
		setLayout(new BorderLayout());
		setOpaque(false);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);

		pointsLabel.setForeground(Color.WHITE);
		pointsBar.setPreferredSize(new Dimension(0, 8));
		pointsBar.setForeground(new Color(80, 170, 220));
		pointsBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		pointsBar.setBorderPainted(false);
		levelLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		levelBar.setPreferredSize(new Dimension(0, 6));
		levelBar.setForeground(ColorScheme.BRAND_ORANGE);
		levelBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		levelBar.setBorderPainted(false);

		buryButton.setFocusable(false);
		buryButton.addActionListener(e ->
		{
			engine.buryBones();
			update();
		});
		potionButton.setFocusable(false);
		potionButton.addActionListener(e ->
		{
			engine.drink(prayerPotionIndex);
			update();
		});

		addPart(content, pointsLabel);
		addPart(content, pointsBar);
		addPart(content, gap(4));
		addPart(content, levelLabel);
		addPart(content, levelBar);
		addPart(content, gap(8));
		addPart(content, buryButton);
		addPart(content, gap(4));
		addPart(content, potionButton);
		addPart(content, gap(8));

		for (int i = 0; i < Prayer.ALL.size(); i++)
		{
			PrayerRow row = new PrayerRow(i);
			rows.add(row);
			addPart(content, row.panel);
		}

		add(content, BorderLayout.NORTH);
		update();
	}

	private static void addPart(JPanel content, JComponent part)
	{
		part.setAlignmentX(LEFT_ALIGNMENT);
		if (part instanceof JButton)
		{
			part.setMaximumSize(new Dimension(Integer.MAX_VALUE, part.getPreferredSize().height + 8));
		}
		content.add(part);
	}

	private static JComponent gap(int height)
	{
		JPanel gap = new JPanel();
		gap.setOpaque(false);
		gap.setPreferredSize(new Dimension(0, height));
		gap.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		return gap;
	}

	public void update()
	{
		GameState state = engine.getState();
		int max = engine.getPrayerMaxPoints();
		pointsLabel.setText("Prayer points: " + (int) Math.ceil(engine.getPrayerPoints()) + " / " + max);
		pointsBar.setValue(max <= 0 ? 0 : (int) (1000 * engine.getPrayerPoints() / max));

		int level = state.level(Skill.PRAYER);
		boolean maxed = level >= XpTable.MAX_LEVEL;
		levelLabel.setText("Prayer level " + level + (maxed ? " (max)"
			: ", " + numbers.format(XpTable.xpForLevel(level + 1) - (long) state.xp(Skill.PRAYER)) + " xp to next"));
		double floor = XpTable.xpForLevel(level);
		double ceiling = XpTable.xpForLevel(level + 1);
		levelBar.setValue(maxed ? 1000 : (int) (1000 * (state.xp(Skill.PRAYER) - floor) / (ceiling - floor)));

		long bones = engine.totalBones();
		buryButton.setText("Bury all bones (" + numbers.format(bones) + ")");
		buryButton.setEnabled(bones > 0);
		potionButton.setText("Drink Prayer potion x" + state.potionCount[prayerPotionIndex]);
		potionButton.setEnabled(engine.canDrink(prayerPotionIndex));

		for (PrayerRow row : rows)
		{
			row.refresh();
		}
	}

	private static String drainWord(int drainEffect)
	{
		return drainEffect <= 1 ? "slow drain" : drainEffect <= 6 ? "medium drain"
			: drainEffect <= 12 ? "fast drain" : "very fast drain";
	}

	private static String effects(Prayer prayer)
	{
		List<String> parts = new ArrayList<>();
		if (prayer.getAttackPercent() > 0)
		{
			parts.add("+" + prayer.getAttackPercent() + "% atk");
		}
		if (prayer.getStrengthPercent() > 0)
		{
			parts.add("+" + prayer.getStrengthPercent() + "% str");
		}
		if (prayer.getDefencePercent() > 0)
		{
			parts.add("+" + prayer.getDefencePercent() + "% def");
		}
		return String.join(", ", parts);
	}

	private class PrayerRow
	{
		private final int index;
		private final JToggleButton toggle = new JToggleButton();
		private final JLabel detail = new JLabel(" ");
		private final JPanel panel = new JPanel();

		PrayerRow(int index)
		{
			this.index = index;
			Prayer prayer = Prayer.ALL.get(index);
			toggle.setText(prayer.getName());
			toggle.setFocusable(false);
			toggle.setAlignmentX(LEFT_ALIGNMENT);
			toggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggle.getPreferredSize().height + 6));
			toggle.addActionListener(e ->
			{
				engine.togglePrayer(index);
				PrayerPanel.this.update();
			});
			detail.setFont(detail.getFont().deriveFont(11f));
			detail.setAlignmentX(LEFT_ALIGNMENT);
			detail.setBorder(BorderFactory.createEmptyBorder(1, 2, 6, 0));

			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setOpaque(false);
			panel.setAlignmentX(LEFT_ALIGNMENT);
			panel.add(toggle);
			panel.add(detail);
		}

		void refresh()
		{
			Prayer prayer = Prayer.ALL.get(index);
			boolean active = engine.isPrayerActive(index);
			String blocker = engine.prayerBlocker(index);
			toggle.setSelected(active);
			// A prayer that is on can always be turned off, whatever else is true.
			toggle.setEnabled(active || blocker == null);
			boolean levelBlocked = blocker != null && blocker.startsWith("Needs");
			detail.setText(levelBlocked ? blocker : effects(prayer) + " · " + drainWord(prayer.getDrainEffect()));
			detail.setForeground(levelBlocked ? BLOCKED : ColorScheme.LIGHT_GRAY_COLOR);
		}
	}
}
