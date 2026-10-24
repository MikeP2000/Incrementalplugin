package com.idleadventurer;

import com.google.gson.Gson;
import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.GameState;
import com.idleadventurer.engine.OfflineReport;
import com.idleadventurer.ui.IdleAdventurerPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "Idle Adventurer",
	description = "An idle game in the sidebar: your adventurer fights monsters on its own while you buy upgrades",
	tags = {"idle", "incremental", "game", "minigame"}
)
public class IdleAdventurerPlugin extends Plugin
{
	static final String CONFIG_GROUP = "idle-adventurer";
	static final String SAVE_KEY = "save";

	/** Save to the config every 50 ticks (30 seconds). */
	private static final int SAVE_INTERVAL_TICKS = 50;

	/** Away for less than this many ticks (one minute) is not worth a notice. */
	private static final long MIN_OFFLINE_TICKS = 100;

	/** A gap between ticks longer than this (30 seconds) means the computer slept or the client stalled. */
	private static final long STALL_MILLIS = 30_000;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	private SaveCodec saveCodec;
	private GameEngine engine;
	private IdleAdventurerPanel panel;
	private NavigationButton navButton;
	private Timer tickTimer;
	private long lastTickMillis;

	@Override
	protected void startUp()
	{
		saveCodec = new SaveCodec(gson);
		engine = new GameEngine(saveCodec.decode(configManager.getConfiguration(CONFIG_GROUP, SAVE_KEY)));

		OfflineReport report = catchUp(engine.getState().lastSavedMillis);

		panel = new IdleAdventurerPanel(engine);
		if (report != null)
		{
			panel.showOfflineReport(report);
		}

		navButton = NavigationButton.builder()
			.tooltip("Idle Adventurer")
			.icon(createIcon())
			.priority(10)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		// Swing timer fires on the event thread, so the engine and panel never race.
		tickTimer = new Timer(GameEngine.TICK_MILLIS, e -> onTick());
		lastTickMillis = System.currentTimeMillis();
		tickTimer.start();
		save();
		log.debug("Idle Adventurer started");
	}

	@Override
	protected void shutDown()
	{
		if (tickTimer != null)
		{
			tickTimer.stop();
			tickTimer = null;
		}
		clientToolbar.removeNavigation(navButton);
		save();
		panel = null;
		navButton = null;
		engine = null;
		log.debug("Idle Adventurer stopped");
	}

	/** Plays the game forward for time spent away since the given moment; null if there was none worth showing. */
	private OfflineReport catchUp(long sinceMillis)
	{
		long now = System.currentTimeMillis();
		if (sinceMillis <= 0 || now <= sinceMillis)
		{
			return null;
		}
		long ticks = (now - sinceMillis) / GameEngine.TICK_MILLIS;
		if (ticks < MIN_OFFLINE_TICKS)
		{
			return null;
		}
		return engine.fastForward(ticks);
	}

	private void onTick()
	{
		long now = System.currentTimeMillis();
		if (now - lastTickMillis > STALL_MILLIS)
		{
			// The computer slept or the client stalled; count the gap as time away.
			OfflineReport report = catchUp(lastTickMillis);
			if (report != null)
			{
				panel.showOfflineReport(report);
			}
		}
		lastTickMillis = now;
		engine.tick();
		panel.update();
		if (engine.getState().totalTicks % SAVE_INTERVAL_TICKS == 0)
		{
			save();
		}
	}

	private void save()
	{
		if (engine == null)
		{
			return;
		}
		GameState state = engine.getState();
		state.lastSavedMillis = System.currentTimeMillis();
		configManager.setConfiguration(CONFIG_GROUP, SAVE_KEY, saveCodec.encode(state));
	}

	/** Placeholder sidebar icon, a small sword; replaced by real art later. */
	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setColor(new Color(200, 200, 210));
		for (int i = 0; i < 10; i++)
		{
			g.fillRect(3 + i, 12 - i, 2, 2);
		}
		g.setColor(new Color(150, 100, 40));
		g.fillRect(2, 11, 4, 2);
		g.fillRect(2, 11, 2, 4);
		g.dispose();
		return image;
	}
}
