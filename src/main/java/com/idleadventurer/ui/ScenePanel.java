package com.idleadventurer.ui;

import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.Phase;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Draws the adventurer and the current monster. Plain Swing with no RuneLite classes, so it can
 * be rendered and checked outside the client. Only touched on the Swing event thread.
 */
public class ScenePanel extends JPanel
{
	private static final int WIDTH = 225;
	private static final int HEIGHT = 140;
	private static final int GROUND_Y = 112;
	private static final int PLAYER_X = 40;
	private static final int MONSTER_X = 165;
	private static final int MONSTER_START_X = 235;
	/** How many ticks a hit splat stays on screen. */
	private static final int SPLAT_TICKS = 2;

	private static final Color SKY = new Color(120, 170, 210);
	private static final Color GRASS = new Color(70, 120, 50);
	private static final Color SKIN = new Color(230, 190, 150);

	private Phase phase = Phase.WALKING;
	private double walkProgress;
	private String monsterId = "chicken";
	private String monsterName = "Chicken";
	private boolean monsterIsBoss;
	private int playerHp = 10;
	private int playerMaxHp = 10;
	private int monsterHp = 1;
	private int monsterMaxHp = 1;

	private int playerSplat = -1;
	private int playerSplatTicks;
	private int monsterSplat = -1;
	private int monsterSplatTicks;

	public ScenePanel()
	{
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
	}

	public void update(GameEngine engine)
	{
		phase = engine.getPhase();
		walkProgress = engine.getWalkProgress();
		monsterId = engine.getMonster().getId();
		monsterName = engine.getMonster().getName();
		monsterIsBoss = engine.getMonster().isBoss();
		playerHp = engine.getPlayerHp();
		playerMaxHp = engine.getPlayerMaxHp();
		monsterHp = engine.getMonsterHp();
		monsterMaxHp = engine.getMonster().getHitpoints();

		// Damage dealt to the monster shows over the monster, and vice versa.
		playerSplatTicks = Math.max(0, playerSplatTicks - 1);
		monsterSplatTicks = Math.max(0, monsterSplatTicks - 1);
		if (engine.getLastPlayerHit() >= 0)
		{
			monsterSplat = engine.getLastPlayerHit();
			monsterSplatTicks = SPLAT_TICKS;
		}
		if (engine.getLastMonsterHit() >= 0)
		{
			playerSplat = engine.getLastMonsterHit();
			playerSplatTicks = SPLAT_TICKS;
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int width = getWidth();
		g.setColor(SKY);
		g.fillRect(0, 0, width, GROUND_Y);
		g.setColor(GRASS);
		g.fillRect(0, GROUND_Y, width, getHeight() - GROUND_Y);

		boolean dead = phase == Phase.DEAD;
		drawPlayer(g, dead);
		if (!dead)
		{
			int monsterX = phase == Phase.WALKING
				? (int) Math.round(MONSTER_START_X + (MONSTER_X - MONSTER_START_X) * walkProgress)
				: MONSTER_X;
			drawMonster(g, monsterX);
			if (phase == Phase.FIGHTING)
			{
				drawName(g, MONSTER_X, 14, monsterName);
				drawBar(g, MONSTER_X, 18, monsterHp, monsterMaxHp);
				drawSplat(g, MONSTER_X, 70, monsterSplat, monsterSplatTicks);
			}
		}
		drawBar(g, PLAYER_X, 18, playerHp, playerMaxHp);
		drawSplat(g, PLAYER_X, 70, playerSplat, playerSplatTicks);
		g.dispose();
	}

	private void drawPlayer(Graphics2D g, boolean dead)
	{
		int x = PLAYER_X;
		if (dead)
		{
			// Lying down.
			g.setColor(new Color(90, 90, 130));
			g.fillRect(x - 14, GROUND_Y - 10, 28, 10);
			g.setColor(SKIN);
			g.fillRect(x + 14, GROUND_Y - 12, 10, 10);
			return;
		}
		g.setColor(new Color(40, 40, 70));
		g.fillRect(x - 7, GROUND_Y - 16, 6, 16);
		g.fillRect(x + 1, GROUND_Y - 16, 6, 16);
		g.setColor(new Color(60, 90, 160));
		g.fillRect(x - 9, GROUND_Y - 38, 18, 24);
		g.setColor(SKIN);
		g.fillRect(x - 6, GROUND_Y - 52, 12, 13);
		// Bronze-coloured sword held out towards the monster.
		g.setColor(new Color(190, 120, 50));
		g.fillRect(x + 9, GROUND_Y - 34, 16, 3);
		g.setColor(new Color(110, 70, 30));
		g.fillRect(x + 9, GROUND_Y - 36, 3, 7);
	}

	private void drawMonster(Graphics2D g, int x)
	{
		switch (monsterId)
		{
			case "cow":
				g.setColor(new Color(245, 245, 240));
				g.fillRect(x - 16, GROUND_Y - 30, 32, 18);
				g.setColor(new Color(50, 50, 50));
				g.fillRect(x - 8, GROUND_Y - 28, 8, 7);
				g.fillRect(x + 4, GROUND_Y - 22, 7, 6);
				g.setColor(new Color(245, 245, 240));
				g.fillRect(x - 24, GROUND_Y - 34, 10, 12);
				g.setColor(new Color(235, 170, 170));
				g.fillRect(x - 24, GROUND_Y - 25, 10, 3);
				g.setColor(new Color(60, 40, 20));
				g.fillRect(x - 14, GROUND_Y - 12, 4, 12);
				g.fillRect(x + 10, GROUND_Y - 12, 4, 12);
				break;
			case "goblin":
				g.setColor(new Color(60, 40, 20));
				g.fillRect(x - 5, GROUND_Y - 12, 4, 12);
				g.fillRect(x + 1, GROUND_Y - 12, 4, 12);
				g.setColor(new Color(120, 90, 50));
				g.fillRect(x - 8, GROUND_Y - 32, 16, 21);
				g.setColor(new Color(90, 150, 70));
				g.fillRect(x - 7, GROUND_Y - 44, 14, 12);
				g.fillRect(x - 11, GROUND_Y - 42, 4, 5);
				g.fillRect(x + 7, GROUND_Y - 42, 4, 5);
				g.setColor(Color.RED);
				g.fillRect(x - 4, GROUND_Y - 40, 2, 2);
				g.fillRect(x + 2, GROUND_Y - 40, 2, 2);
				break;
			case "chicken":
				g.setColor(new Color(250, 250, 245));
				g.fillRect(x - 9, GROUND_Y - 18, 18, 12);
				g.fillRect(x - 13, GROUND_Y - 26, 8, 10);
				g.setColor(new Color(235, 150, 30));
				g.fillRect(x - 17, GROUND_Y - 22, 4, 3);
				g.setColor(new Color(220, 40, 40));
				g.fillRect(x - 12, GROUND_Y - 29, 5, 3);
				g.setColor(new Color(235, 150, 30));
				g.fillRect(x - 4, GROUND_Y - 6, 2, 6);
				g.fillRect(x + 2, GROUND_Y - 6, 2, 6);
				break;
			default:
				drawCreature(g, x, MonsterArt.of(monsterId));
				break;
		}
	}

	/** Draws a creature at ground level around x, from a shape, two colours and a scale. */
	private void drawCreature(Graphics2D g, int x, MonsterArt art)
	{
		Graphics2D c = (Graphics2D) g.create();
		c.translate(x, GROUND_Y);
		c.scale(art.scale, art.scale);
		switch (art.shape)
		{
			case BEAST:
				c.setColor(art.accent);
				c.fillRect(-10, -6, 3, 6);
				c.fillRect(7, -6, 3, 6);
				c.fillPolygon(new int[]{14, 24, 14}, new int[]{-16, -22, -10}, 3);
				c.setColor(art.body);
				c.fillRect(-14, -18, 28, 12);
				c.fillRect(-23, -24, 11, 11);
				c.setColor(art.accent);
				c.fillRect(-21, -22, 2, 2);
				break;
			case DRAGON:
				c.setColor(art.accent);
				c.fillPolygon(new int[]{-6, 10, 20}, new int[]{-26, -56, -26}, 3);
				c.fillRect(-12, -8, 5, 8);
				c.fillRect(8, -8, 5, 8);
				c.setColor(art.body);
				c.fillPolygon(new int[]{18, 40, 18}, new int[]{-22, -8, -14}, 3);
				c.fillRect(-18, -26, 36, 18);
				c.fillRect(-24, -42, 9, 18);
				c.fillRect(-34, -46, 15, 10);
				c.setColor(Color.YELLOW);
				c.fillRect(-28, -44, 3, 3);
				break;
			default:
				c.setColor(art.accent);
				c.fillRect(-6, -12, 5, 12);
				c.fillRect(1, -12, 5, 12);
				c.fillRect(-9, -32, 18, 21);
				c.setColor(art.body);
				c.fillRect(-11, -32, 4, 18);
				c.fillRect(7, -32, 4, 18);
				c.fillRect(-6, -46, 12, 13);
				c.setColor(Color.RED);
				c.fillRect(-4, -42, 2, 2);
				c.fillRect(2, -42, 2, 2);
				break;
		}
		c.dispose();
	}

	private void drawName(Graphics2D g, int centerX, int y, String name)
	{
		g.setFont(g.getFont().deriveFont(Font.BOLD, 10f));
		FontMetrics metrics = g.getFontMetrics();
		int left = Math.max(2, Math.min(centerX - metrics.stringWidth(name) / 2,
			getWidth() - metrics.stringWidth(name) - 2));
		g.setColor(Color.BLACK);
		g.drawString(name, left + 1, y + 1);
		g.setColor(monsterIsBoss ? new Color(255, 200, 60) : Color.WHITE);
		g.drawString(name, left, y);
	}

	/** OSRS-style hit point bar: green remaining, red lost. */
	private void drawBar(Graphics2D g, int centerX, int y, int hp, int maxHp)
	{
		int width = 30;
		int left = centerX - width / 2;
		g.setColor(new Color(190, 30, 30));
		g.fillRect(left, y, width, 5);
		int green = maxHp <= 0 ? 0 : (int) Math.round(width * Math.max(0, Math.min(hp, maxHp)) / (double) maxHp);
		g.setColor(new Color(40, 190, 40));
		g.fillRect(left, y, green, 5);
		g.setColor(Color.BLACK);
		g.drawRect(left, y, width, 5);
	}

	private void drawSplat(Graphics2D g, int centerX, int y, int damage, int ticksLeft)
	{
		if (ticksLeft <= 0 || damage < 0)
		{
			return;
		}
		g.setColor(damage > 0 ? new Color(200, 30, 30) : new Color(60, 110, 200));
		g.fillOval(centerX - 9, y - 9, 18, 18);
		g.setColor(Color.WHITE);
		g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
		FontMetrics metrics = g.getFontMetrics();
		String text = String.valueOf(damage);
		g.drawString(text, centerX - metrics.stringWidth(text) / 2, y + metrics.getAscent() / 2 - 1);
	}
}
