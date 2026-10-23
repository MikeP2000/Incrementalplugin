package com.idleadventurer.ui;

import com.idleadventurer.engine.GameEngine;
import com.idleadventurer.engine.GameState;
import com.idleadventurer.engine.Monster;
import com.idleadventurer.engine.Monsters;
import com.idleadventurer.engine.Skill;
import com.idleadventurer.engine.XpTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * The sidebar panel. Only ever touched on the Swing event thread.
 */
public class IdleAdventurerPanel extends PluginPanel
{
	private final GameEngine engine;
	private final NumberFormat numbers = NumberFormat.getIntegerInstance();

	private final ScenePanel scene = new ScenePanel();
	private final JLabel statusLabel = new JLabel(" ");
	private final JLabel coinsLabel = new JLabel();
	private final JLabel bonesLabel = new JLabel();
	private final JLabel killsLabel = new JLabel();
	private final Map<Skill, JProgressBar> skillBars = new EnumMap<>(Skill.class);
	private final Map<Skill, JLabel> skillLabels = new EnumMap<>(Skill.class);

	public IdleAdventurerPanel(GameEngine engine)
	{
		this.engine = engine;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);

		statusLabel.setForeground(Color.WHITE);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));

		for (JComponent part : new JComponent[]{scene, statusLabel, buildTargetRow(), buildResourceRow(), buildSkillRows()})
		{
			part.setAlignmentX(LEFT_ALIGNMENT);
			content.add(part);
		}

		add(content, BorderLayout.NORTH);
		update();
	}

	private JPanel buildTargetRow()
	{
		JPanel row = new JPanel(new GridLayout(2, 2, 4, 4));
		row.setOpaque(false);
		row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		JComboBox<Monster> monsterBox = new JComboBox<>();
		for (Monster monster : Monsters.LUMBRIDGE)
		{
			monsterBox.addItem(monster);
		}
		monsterBox.setRenderer(IdleAdventurerPanel.<Monster>renderer(m -> m.getName() + " (lvl " + m.getCombatLevel() + ")"));
		monsterBox.setSelectedItem(engine.getMonster());
		monsterBox.addActionListener(e -> engine.setTargetMonster(((Monster) monsterBox.getSelectedItem()).getId()));

		JComboBox<Skill> styleBox = new JComboBox<>();
		for (Skill skill : Skill.values())
		{
			if (skill.isTrainable())
			{
				styleBox.addItem(skill);
			}
		}
		styleBox.setRenderer(IdleAdventurerPanel.<Skill>renderer(Skill::getDisplayName));
		styleBox.setSelectedItem(engine.getState().trainingSkill);
		styleBox.addActionListener(e -> engine.setTrainingSkill((Skill) styleBox.getSelectedItem()));

		row.add(label("Fight"));
		row.add(monsterBox);
		row.add(label("Train"));
		row.add(styleBox);
		return row;
	}

	private JPanel buildResourceRow()
	{
		JPanel row = new JPanel(new GridLayout(3, 1, 0, 2));
		row.setOpaque(false);
		row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		for (JLabel resource : new JLabel[]{coinsLabel, bonesLabel, killsLabel})
		{
			resource.setForeground(Color.WHITE);
			row.add(resource);
		}
		coinsLabel.setForeground(new Color(255, 215, 60));
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

		String monsterName = engine.getMonster().getName();
		switch (engine.getPhase())
		{
			case WALKING:
				statusLabel.setText("Walking to a " + monsterName + "...");
				break;
			case FIGHTING:
				statusLabel.setText("Fighting a " + monsterName);
				break;
			default:
				statusLabel.setText("Knocked out! Respawning...");
				break;
		}

		coinsLabel.setText("Coins: " + numbers.format(state.coins));
		bonesLabel.setText("Bones: " + numbers.format(state.bones));
		killsLabel.setText("Kills: " + numbers.format(state.kills) + "   Deaths: " + numbers.format(state.deaths));

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
