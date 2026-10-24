package com.idleadventurer.ui;

import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.Food;
import com.idleadventurer.engine.GameState;
import com.idleadventurer.engine.Monster;
import com.idleadventurer.engine.Monsters;
import com.idleadventurer.engine.Phase;
import com.idleadventurer.engine.Skill;
import com.idleadventurer.engine.XpTable;
import com.idleadventurer.engine.Zone;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.runelite.client.ui.ColorScheme;

/**
 * The Fight tab: the scene, what to fight and train, resources, food and skill progress.
 * Only ever touched on the Swing event thread.
 */
public class FightPanel extends JPanel
{
	private final GameEngine engine;
	private final NumberFormat numbers = NumberFormat.getIntegerInstance();

	private final ScenePanel scene = new ScenePanel();
	private final JLabel statusLabel = new JLabel(" ");
	private final JLabel goalLabel = new JLabel(" ");
	private final JComboBox<Zone> zoneBox = new JComboBox<>();
	private final JComboBox<Monster> monsterBox = new JComboBox<>();
	private int shownZones;
	private boolean adjusting;
	private final JLabel coinsLabel = new JLabel();
	private final JLabel bonesLabel = new JLabel();
	private final JLabel killsLabel = new JLabel();
	private final JLabel bonusLabel = new JLabel();
	private final List<JButton> eatButtons = new ArrayList<>();
	private final Map<Skill, JProgressBar> skillBars = new EnumMap<>(Skill.class);
	private final Map<Skill, JLabel> skillLabels = new EnumMap<>(Skill.class);

	public FightPanel(GameEngine engine)
	{
		this.engine = engine;
		setLayout(new BorderLayout());
		setOpaque(false);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);

		statusLabel.setForeground(Color.WHITE);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
		goalLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		goalLabel.setFont(goalLabel.getFont().deriveFont(11f));
		goalLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

		for (JComponent part : new JComponent[]{scene, statusLabel, goalLabel, buildTargetRow(), buildResourceRow(), buildEatRow(), buildSkillRows()})
		{
			part.setAlignmentX(LEFT_ALIGNMENT);
			content.add(part);
		}

		add(content, BorderLayout.NORTH);
		update();
	}

	private JPanel buildTargetRow()
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setOpaque(false);
		row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		zoneBox.setRenderer(FightPanel.<Zone>renderer(Zone::getName));
		monsterBox.setRenderer(FightPanel.<Monster>renderer(m -> m.isBoss()
			? m.getName() + " (boss)"
			: m.getName() + " (lvl " + m.getCombatLevel() + ")"));
		populateZones();
		zoneBox.addActionListener(e ->
		{
			if (!adjusting && zoneBox.getSelectedItem() != null)
			{
				Zone zone = (Zone) zoneBox.getSelectedItem();
				populateMonsters(zone, zone.getMonsters().get(0));
				engine.setTargetMonster(zone.getMonsters().get(0).getId());
			}
		});
		monsterBox.addActionListener(e ->
		{
			if (!adjusting && monsterBox.getSelectedItem() != null)
			{
				engine.setTargetMonster(((Monster) monsterBox.getSelectedItem()).getId());
			}
		});

		JComboBox<Skill> styleBox = new JComboBox<>();
		for (Skill skill : Skill.values())
		{
			if (skill.isTrainable())
			{
				styleBox.addItem(skill);
			}
		}
		styleBox.setRenderer(FightPanel.<Skill>renderer(Skill::getDisplayName));
		styleBox.setSelectedItem(engine.getState().trainingSkill);
		styleBox.addActionListener(e -> engine.setTrainingSkill((Skill) styleBox.getSelectedItem()));

		row.add(labelled("Zone", zoneBox));
		row.add(Box.createVerticalStrut(4));
		row.add(labelled("Fight", monsterBox));
		row.add(Box.createVerticalStrut(4));
		row.add(labelled("Train", styleBox));
		return row;
	}

	/** Lists the unlocked zones and selects the one being fought in. */
	private void populateZones()
	{
		adjusting = true;
		zoneBox.removeAllItems();
		for (int i = 0; i < engine.getState().unlockedZones; i++)
		{
			zoneBox.addItem(Monsters.ZONES.get(i));
		}
		shownZones = engine.getState().unlockedZones;
		zoneBox.setSelectedItem(engine.getZone());
		populateMonsters(engine.getZone(), engine.getMonster());
		adjusting = false;
	}

	private void populateMonsters(Zone zone, Monster selected)
	{
		boolean wasAdjusting = adjusting;
		adjusting = true;
		monsterBox.removeAllItems();
		for (Monster monster : zone.getMonsters())
		{
			monsterBox.addItem(monster);
		}
		monsterBox.setSelectedItem(selected);
		adjusting = wasAdjusting;
	}

	private JPanel buildResourceRow()
	{
		JPanel row = new JPanel(new GridLayout(4, 1, 0, 2));
		row.setOpaque(false);
		row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		bonusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		for (JLabel resource : new JLabel[]{coinsLabel, bonesLabel, killsLabel, bonusLabel})
		{
			resource.setForeground(Color.WHITE);
			row.add(resource);
		}
		coinsLabel.setForeground(new Color(255, 215, 60));
		return row;
	}

	/** One Eat button per food, shown only while you own some. */
	private JPanel buildEatRow()
	{
		// BoxLayout so hidden buttons take no space.
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setOpaque(false);
		row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		for (int i = 0; i < Food.ALL.size(); i++)
		{
			int index = i;
			JButton button = new JButton();
			button.setFocusable(false);
			button.setVisible(false);
			button.setAlignmentX(LEFT_ALIGNMENT);
			button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height + 8));
			button.addActionListener(e ->
			{
				engine.eat(index);
				update();
			});
			eatButtons.add(button);
			row.add(button);
		}
		return row;
	}

	private JPanel buildSkillRows()
	{
		JPanel rows = new JPanel(new GridLayout(Skill.values().length, 1, 0, 6));
		rows.setOpaque(false);
		for (Skill skill : Skill.values())
		{
			JPanel row = new JPanel(new BorderLayout(0, 2));
			row.setOpaque(false);
			JLabel name = new JLabel();
			name.setForeground(Color.WHITE);
			JProgressBar bar = new JProgressBar(0, 1000);
			bar.setPreferredSize(new Dimension(0, 8));
			bar.setForeground(ColorScheme.BRAND_ORANGE);
			bar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			bar.setBorderPainted(false);
			row.add(name, BorderLayout.NORTH);
			row.add(bar, BorderLayout.CENTER);
			rows.add(row);
			skillLabels.put(skill, name);
			skillBars.put(skill, bar);
		}
		return rows;
	}

	private static <T> DefaultListCellRenderer renderer(Function<T, String> text)
	{
		return new DefaultListCellRenderer()
		{
			@Override
			@SuppressWarnings("unchecked")
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
				boolean selected, boolean focus)
			{
				super.getListCellRendererComponent(list, value, index, selected, focus);
				setText(text.apply((T) value));
				return this;
			}
		};
	}

	private JPanel labelled(String text, JComponent field)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setOpaque(false);
		JLabel label = label(text);
		label.setPreferredSize(new Dimension(36, 0));
		row.add(label, BorderLayout.WEST);
		row.add(field, BorderLayout.CENTER);
		row.setAlignmentX(LEFT_ALIGNMENT);
		return row;
	}

	private JLabel label(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}

	public void update()
	{
		GameState state = engine.getState();
		scene.update(engine);

		if (state.unlockedZones != shownZones)
		{
			populateZones();
		}

		Monster monster = engine.getMonster();
		String monsterName = monster.isBoss() ? "the " + monster.getName() : "a " + monster.getName();
		String unlocked = engine.getUnlockNotice();
		statusLabel.setForeground(unlocked != null ? ColorScheme.BRAND_ORANGE : Color.WHITE);
		if (unlocked != null)
		{
			statusLabel.setText("Zone unlocked: " + unlocked + "!");
		}
		else
		{
			switch (engine.getPhase())
			{
				case WALKING:
					statusLabel.setText("Walking to " + monsterName + "...");
					break;
				case FIGHTING:
					statusLabel.setText("Fighting " + monsterName);
					break;
				default:
					statusLabel.setText("Knocked out! Respawning...");
					break;
			}
		}
		if (state.unlockedZones < Monsters.ZONES.size())
		{
			Zone current = Monsters.ZONES.get(state.unlockedZones - 1);
			goalLabel.setText("<html><body style='width: 165px'>Beat the " + current.getBoss().getName()
				+ " to unlock " + Monsters.ZONES.get(state.unlockedZones).getName() + "</body></html>");
		}
		else
		{
			goalLabel.setText("Every zone unlocked");
		}

		coinsLabel.setText("Coins: " + numbers.format(state.coins));
		bonesLabel.setText("Bones: " + numbers.format(state.bones));
		killsLabel.setText("Kills: " + numbers.format(state.kills) + "   Deaths: " + numbers.format(state.deaths));

		bonusLabel.setText("Bonuses: +" + engine.attackBonus() + " acc, +" + engine.strengthBonus()
			+ " str, +" + engine.defenceBonus() + " def");

		for (int i = 0; i < eatButtons.size(); i++)
		{
			Food food = Food.ALL.get(i);
			JButton button = eatButtons.get(i);
			button.setVisible(state.foodCount[i] > 0);
			button.setText("Eat " + food.getName() + " x" + state.foodCount[i] + " (+" + food.getHeal() + ")");
			button.setEnabled(engine.getPlayerHp() < engine.getPlayerMaxHp()
				&& engine.getPhase() != Phase.DEAD);
		}

		for (Skill skill : Skill.values())
		{
			int level = state.level(skill);
			double xp = state.xp(skill);
			String text = skill.getDisplayName() + " " + level;
			if (skill == Skill.HITPOINTS)
			{
				text += "  (" + engine.getPlayerHp() + "/" + engine.getPlayerMaxHp() + ")";
			}
			skillLabels.get(skill).setText(text);

			double floor = XpTable.xpForLevel(level);
			double ceiling = XpTable.xpForLevel(level + 1);
			boolean maxed = level >= XpTable.MAX_LEVEL;
			skillBars.get(skill).setValue(maxed ? 1000 : (int) (1000 * (xp - floor) / (ceiling - floor)));
		}
	}
}
