package com.idleadventurer.ui;

import com.idleadventurer.engine.Food;
import com.idleadventurer.engine.Gear;
import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.GearItem;
import com.idleadventurer.engine.Slot;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;

/**
 * The Shop tab: the next gear upgrade for each slot, and food. Only touched on the Swing event thread.
 */
public class ShopPanel extends JPanel
{
	/** Swatch colour per metal tier, Bronze to Rune. */
	private static final Color[] METAL_COLOURS = {
		new Color(180, 110, 50), new Color(140, 140, 145), new Color(190, 195, 205), new Color(55, 55, 65),
		new Color(60, 90, 170), new Color(60, 140, 70), new Color(60, 170, 200)
	};
	private static final int BULK_FOOD = 5;

	private final GameEngine engine;
	private final NumberFormat numbers = NumberFormat.getIntegerInstance();
	private final JLabel coinsLabel = new JLabel();
	private final List<GearRow> gearRows = new ArrayList<>();
	private final List<FoodRow> foodRows = new ArrayList<>();

	public ShopPanel(GameEngine engine)
	{
		this.engine = engine;
		setLayout(new BorderLayout());
		setOpaque(false);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);

		coinsLabel.setForeground(new Color(255, 215, 60));
		addPart(content, coinsLabel);
		addPart(content, heading("Gear"));
		for (Slot slot : Slot.values())
		{
			GearRow row = new GearRow(slot);
			gearRows.add(row);
			addPart(content, row.panel);
		}
		addPart(content, heading("Food"));
		for (int i = 0; i < Food.ALL.size(); i++)
		{
			FoodRow row = new FoodRow(i);
			foodRows.add(row);
			addPart(content, row.panel);
		}

		add(content, BorderLayout.NORTH);
		update();
	}

	private static void addPart(JPanel content, JComponent part)
	{
		part.setAlignmentX(LEFT_ALIGNMENT);
		content.add(part);
	}

	private static JLabel heading(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.BRAND_ORANGE);
		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
		return label;
	}

	public void update()
	{
		coinsLabel.setText("Coins: " + numbers.format(engine.getState().coins));
		for (GearRow row : gearRows)
		{
			row.refresh();
		}
		for (FoodRow row : foodRows)
		{
			row.refresh();
		}
	}

	private static JLabel small(Color colour)
	{
		JLabel label = new JLabel(" ");
		label.setForeground(colour);
		label.setFont(label.getFont().deriveFont(11f));
		return label;
	}

	private static JPanel row(JComponent west, JComponent text, JComponent east)
	{
		JPanel panel = new JPanel(new BorderLayout(6, 0));
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		panel.add(west, BorderLayout.WEST);
		panel.add(text, BorderLayout.CENTER);
		panel.add(east, BorderLayout.EAST);
		return panel;
	}

	private static JPanel textBlock(JLabel top, JLabel bottom)
	{
		JPanel block = new JPanel();
		block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
		block.setOpaque(false);
		block.add(top);
		block.add(bottom);
		return block;
	}

	/** A small square in the item's metal colour, standing in for an item icon. */
	private static class Swatch extends JComponent
	{
		private Color colour = Color.GRAY;

		Swatch()
		{
			setPreferredSize(new Dimension(16, 16));
		}

		void setColour(Color colour)
		{
			this.colour = colour;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			g.setColor(colour);
			g.fillRect(0, 6, 16, 16);
			g.setColor(Color.BLACK);
			g.drawRect(0, 6, 15, 15);
		}
	}

	private class GearRow
	{
		private final Slot slot;
		private final Swatch swatch = new Swatch();
		private final JLabel nameLabel = new JLabel(" ");
		private final JLabel detailLabel = small(ColorScheme.LIGHT_GRAY_COLOR);
		private final JButton buyButton = new JButton("Buy");
		private final JPanel panel;
		private GearItem offered;

		GearRow(Slot slot)
		{
			this.slot = slot;
			nameLabel.setForeground(Color.WHITE);
			buyButton.setFocusable(false);
			buyButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
			buyButton.addActionListener(e ->
			{
				if (offered != null)
				{
					engine.buyGear(offered);
					update();
				}
			});
			panel = row(swatch, textBlock(nameLabel, detailLabel), buyButton);
		}

		void refresh()
		{
			int owned = engine.getState().gearTier[slot.ordinal()];
			int next = owned + 1;
			if (next >= Gear.tierCount())
			{
				offered = null;
				GearItem best = engine.getEquipped(slot);
				swatch.setColour(METAL_COLOURS[best.getTier()]);
				nameLabel.setText(best.getName());
				detailLabel.setText("Best available");
				detailLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				buyButton.setVisible(false);
				return;
			}
			offered = Gear.get(slot, next);
			swatch.setColour(METAL_COLOURS[offered.getTier()]);
			nameLabel.setText(offered.getName());
			String blocker = engine.gearBlocker(offered);
			boolean levelBlocked = blocker != null && blocker.startsWith("Needs");
			detailLabel.setText(stats(offered) + " · " + numbers.format(offered.getCost()) + "c"
				+ (levelBlocked ? " · " + blocker : ""));
			detailLabel.setForeground(levelBlocked ? new Color(230, 110, 90) : ColorScheme.LIGHT_GRAY_COLOR);
			buyButton.setVisible(true);
			buyButton.setEnabled(blocker == null);
		}

		private String stats(GearItem item)
		{
			return slot == Slot.WEAPON
				? "+" + item.getAttackBonus() + " acc +" + item.getStrengthBonus() + " str"
				: "+" + item.getDefenceBonus() + " def";
		}
	}

	private class FoodRow
	{
		private final int index;
		private final JLabel nameLabel = new JLabel(" ");
		private final JLabel detailLabel = small(ColorScheme.LIGHT_GRAY_COLOR);
		private final JButton buyButton = new JButton("Buy " + BULK_FOOD);
		private final JPanel panel;

		FoodRow(int index)
		{
			this.index = index;
			nameLabel.setForeground(Color.WHITE);
			buyButton.setFocusable(false);
			buyButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
			buyButton.addActionListener(e ->
			{
				engine.buyFood(index, BULK_FOOD);
				update();
			});
			panel = row((JComponent) Box.createHorizontalStrut(16), textBlock(nameLabel, detailLabel), buyButton);
		}

		void refresh()
		{
			Food food = Food.ALL.get(index);
			nameLabel.setText(food.getName() + " (heals " + food.getHeal() + ")");
			detailLabel.setText(numbers.format(food.getCost()) + "c each · have " + engine.getState().foodCount[index]);
			buyButton.setEnabled(engine.getState().coins >= (long) food.getCost() * BULK_FOOD);
		}
	}
}
