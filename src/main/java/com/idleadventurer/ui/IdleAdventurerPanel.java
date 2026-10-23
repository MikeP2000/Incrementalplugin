package com.idleadventurer.ui;

import com.idleadventurer.engine.GameEngine;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * The sidebar panel: a Fight tab and a Shop tab. Only ever touched on the Swing event thread.
 */
public class IdleAdventurerPanel extends PluginPanel
{
	private static final String FIGHT = "Fight";
	private static final String SHOP = "Shop";

	private final FightPanel fightPanel;
	private final ShopPanel shopPanel;
	private final CardLayout cards = new CardLayout();
	private final JPanel cardHolder = new JPanel(cards);

	public IdleAdventurerPanel(GameEngine engine)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		fightPanel = new FightPanel(engine);
		shopPanel = new ShopPanel(engine);

		cardHolder.setOpaque(false);
		cardHolder.add(fightPanel, FIGHT);
		cardHolder.add(shopPanel, SHOP);

		JPanel tabs = new JPanel(new GridLayout(1, 2, 4, 0));
		tabs.setOpaque(false);
		tabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		ButtonGroup group = new ButtonGroup();
		for (String name : new String[]{FIGHT, SHOP})
		{
			JToggleButton tab = new JToggleButton(name);
			tab.setFocusable(false);
			tab.addActionListener(e -> cards.show(cardHolder, name));
			group.add(tab);
			tabs.add(tab);
			if (name.equals(FIGHT))
			{
				tab.setSelected(true);
			}
		}

		add(tabs, BorderLayout.NORTH);
		add(cardHolder, BorderLayout.CENTER);
	}

	public void update()
	{
		fightPanel.update();
		shopPanel.update();
	}
}
