package com.idleadventurer.ui;

import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.OfflineReport;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * The sidebar panel: Fight, Prayer, Shop and Auto tabs, plus the "while you were away" notice. Only ever touched on the Swing event thread.
 */
public class IdleAdventurerPanel extends PluginPanel
{
	private static final String FIGHT = "Fight";
	private static final String PRAYER = "Prayer";
	private static final String SHOP = "Shop";
	private static final String AUTO = "Auto";

	private final FightPanel fightPanel;
	private final PrayerPanel prayerPanel;
	private final ShopPanel shopPanel;
	private final AutoPanel autoPanel;
	private final JPanel notice = new JPanel();
	private final JLabel noticeText = new JLabel();
	private final CardLayout cards = new CardLayout();
	private final JPanel cardHolder = new JPanel(cards);

	public IdleAdventurerPanel(GameEngine engine)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		fightPanel = new FightPanel(engine);
		prayerPanel = new PrayerPanel(engine);
		shopPanel = new ShopPanel(engine);
		autoPanel = new AutoPanel(engine);

		cardHolder.setOpaque(false);
		cardHolder.add(fightPanel, FIGHT);
		cardHolder.add(prayerPanel, PRAYER);
		cardHolder.add(shopPanel, SHOP);
		cardHolder.add(autoPanel, AUTO);

		JPanel tabs = new JPanel(new GridLayout(1, 4, 3, 0));
		tabs.setOpaque(false);
		tabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		ButtonGroup group = new ButtonGroup();
		for (String name : new String[]{FIGHT, PRAYER, SHOP, AUTO})
		{
			JToggleButton tab = new JToggleButton(name);
			tab.setFocusable(false);
			tab.setMargin(new Insets(2, 2, 2, 2));
			tab.addActionListener(e -> cards.show(cardHolder, name));
			group.add(tab);
			tabs.add(tab);
			if (name.equals(FIGHT))
			{
				tab.setSelected(true);
			}
		}

		notice.setLayout(new BoxLayout(notice, BoxLayout.Y_AXIS));
		notice.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		notice.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		notice.setVisible(false);
		noticeText.setForeground(Color.WHITE);
		noticeText.setAlignmentX(LEFT_ALIGNMENT);
		JButton dismiss = new JButton("Dismiss");
		dismiss.setFocusable(false);
		dismiss.setAlignmentX(LEFT_ALIGNMENT);
		dismiss.addActionListener(e -> notice.setVisible(false));
		notice.add(noticeText);
		notice.add(Box.createRigidArea(new Dimension(0, 4)));
		notice.add(dismiss);

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setOpaque(false);
		notice.setAlignmentX(LEFT_ALIGNMENT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		top.add(notice);
		top.add(tabs);

		add(top, BorderLayout.NORTH);
		add(cardHolder, BorderLayout.CENTER);
	}

	/** Shows what happened during time away; stays up until dismissed. */
	public void showOfflineReport(OfflineReport report)
	{
		StringBuilder text = new StringBuilder("<html><body><b>While you were away (")
			.append(report.describeTime()).append("):</b>");
		for (String line : report.lines())
		{
			text.append("<br>").append(line);
		}
		noticeText.setText(text.append("</body></html>").toString());
		notice.setVisible(true);
	}

	public void update()
	{
		fightPanel.update();
		prayerPanel.update();
		shopPanel.update();
	}
}
