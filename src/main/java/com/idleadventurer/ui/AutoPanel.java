package com.idleadventurer.ui;

import com.idleadventurer.engine.Food;
import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.GameState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;

/**
 * The Auto tab: switches for what the adventurer does without being asked.
 * Only touched on the Swing event thread.
 */
public class AutoPanel extends JPanel
{
	private static final Integer[] EAT_PERCENTS = {20, 30, 40, 50, 60, 70, 80};

	private final GameState state;
	private final JCheckBox autoEat = new JCheckBox("Eat food when hurt");
	private final JComboBox<Integer> eatPercent = new JComboBox<>(EAT_PERCENTS);
	private final JCheckBox autoPotion = new JCheckBox("Drink potions");
	private final JCheckBox autoBury = new JCheckBox("Bury bones");
	private final JCheckBox autoPrayer = new JCheckBox("Keep chosen prayers on");
	private final JCheckBox autoAdvance = new JCheckBox("Move to newly unlocked zones");
	private final JComboBox<String> autoBuy = new JComboBox<>();

	public AutoPanel(GameEngine engine)
	{
		this.state = engine.getState();
		setLayout(new BorderLayout());
		setOpaque(false);

		autoBuy.addItem("Off");
		for (Food food : Food.ALL)
		{
			autoBuy.addItem(food.getName() + " (" + food.getCost() + "c)");
		}

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);

		add(content, autoEat);
		JLabel eatLabel = note("Eat at or below this health %:");
		add(content, eatLabel);
		add(content, eatPercent);
		add(content, gap(8));
		add(content, autoPotion);
		add(content, note("Boost potions when a boost has<br>worn off; Prayer potions when<br>points run low."));
		add(content, gap(6));
		add(content, autoBury);
		add(content, note("Buries every bone straight<br>away for Prayer XP."));
		add(content, gap(6));
		add(content, autoPrayer);
		add(content, note("Turns your chosen prayers back<br>on after a respawn or a<br>Prayer potion."));
		add(content, gap(6));
		add(content, autoAdvance);
		add(content, note("After a boss, moves you to the<br>first monster of the next zone."));
		add(content, gap(8));
		JLabel stockLabel = new JLabel("Keep food stocked:");
		stockLabel.setForeground(Color.WHITE);
		add(content, stockLabel);
		add(content, autoBuy);
		add(content, note("Buys up to 30 when fewer than<br>10 are left and you can<br>afford it."));

		bind(autoEat, v -> state.autoEat = v);
		bind(autoPotion, v -> state.autoPotion = v);
		bind(autoBury, v -> state.autoBury = v);
		bind(autoPrayer, v -> state.autoPrayer = v);
		bind(autoAdvance, v -> state.autoAdvance = v);
		eatPercent.addActionListener(e -> state.autoEatPercent = (Integer) eatPercent.getSelectedItem());
		autoBuy.addActionListener(e -> state.autoBuyFood = autoBuy.getSelectedIndex() - 1);

		autoEat.setSelected(state.autoEat);
		eatPercent.setSelectedItem(nearestPercent(state.autoEatPercent));
		autoPotion.setSelected(state.autoPotion);
		autoBury.setSelected(state.autoBury);
		autoPrayer.setSelected(state.autoPrayer);
		autoAdvance.setSelected(state.autoAdvance);
		autoBuy.setSelectedIndex(state.autoBuyFood + 1);

		add(content, BorderLayout.NORTH);
	}

	private interface Setter
	{
		void set(boolean value);
	}

	private static void bind(JCheckBox box, Setter setter)
	{
		box.addActionListener(e -> setter.set(box.isSelected()));
	}

	private static int nearestPercent(int percent)
	{
		int best = EAT_PERCENTS[0];
		for (int option : EAT_PERCENTS)
		{
			if (Math.abs(option - percent) < Math.abs(best - percent))
			{
				best = option;
			}
		}
		return best;
	}

	private static JLabel note(String text)
	{
		JLabel label = new JLabel("<html><body>" + text + "</body></html>");
		label.setFont(label.getFont().deriveFont(11f));
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 0));
		return label;
	}

	private static void add(JPanel content, JComponent part)
	{
		part.setAlignmentX(LEFT_ALIGNMENT);
		if (part instanceof JComboBox)
		{
			part.setMaximumSize(new Dimension(Integer.MAX_VALUE, part.getPreferredSize().height));
		}
		if (part instanceof JCheckBox)
		{
			part.setOpaque(false);
			part.setFocusable(false);
			part.setForeground(Color.WHITE);
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
}
