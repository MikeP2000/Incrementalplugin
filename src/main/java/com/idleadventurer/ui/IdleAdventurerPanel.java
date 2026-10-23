package com.idleadventurer.ui;

import com.idleadventurer.engine.GameState;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * The sidebar panel. Only ever touched on the Swing event thread.
 */
public class IdleAdventurerPanel extends PluginPanel
{
	private final JLabel ticksLabel = new JLabel("", SwingConstants.CENTER);

	public IdleAdventurerPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel title = new JLabel("Idle Adventurer", SwingConstants.CENTER);
		title.setForeground(Color.WHITE);
		add(title, BorderLayout.NORTH);

		ticksLabel.setForeground(ColorScheme.BRAND_ORANGE);
		add(ticksLabel, BorderLayout.CENTER);
	}

	public void update(GameState state)
	{
		ticksLabel.setText("Ticks survived: " + state.totalTicks);
	}
}
