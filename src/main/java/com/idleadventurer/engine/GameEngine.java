package com.idleadventurer.engine;

import java.util.Random;

/**
 * Advances the game one tick at a time. Knows nothing about RuneLite or Swing so it can be
 * tested on its own. Persistent progress lives in {@link GameState}; the fight in progress
 * (hit points, cooldowns) lives here and is rebuilt on startup.
 */
public class GameEngine
{
	/** Length of one game tick, matching OSRS. */
	public static final int TICK_MILLIS = 600;

	static final int WALK_TICKS = 3;
	static final int RESPAWN_TICKS = 5;
	static final int PLAYER_ATTACK_TICKS = 4;
	/** One hit point is restored this often, so a long fight is not a coin flip. */
	static final int REGEN_TICKS = 10;
	/** Multiplier on OSRS XP rates; 1 matches OSRS (4 XP per damage, 1.33 to Hitpoints). */
	static final double XP_RATE = 1;
	static final double XP_PER_DAMAGE = 4 * XP_RATE;
	static final double HITPOINTS_XP_PER_DAMAGE = 1.33 * XP_RATE;

	private final GameState state;
	private final Random random;

	private Phase phase;
	private Monster monster;
	private int playerHp;
	private int monsterHp;
	private int phaseTicks;
	private int playerCooldown;
	private int monsterCooldown;
	private int lastPlayerHit;
	private int lastMonsterHit;

	public GameEngine(GameState state)
	{
		this(state, new Random());
	}

	public GameEngine(GameState state, Random random)
	{
		state.sanitise();
		this.state = state;
		this.random = random;
		this.playerHp = getPlayerMaxHp();
		startWalking();
	}

	public GameState getState()
	{
		return state;
	}

	public Phase getPhase()
	{
		return phase;
	}

	public Monster getMonster()
	{
		return monster;
	}

	public int getPlayerHp()
	{
		return playerHp;
	}

	public int getPlayerMaxHp()
	{
		return state.level(Skill.HITPOINTS);
	}

	public int getMonsterHp()
	{
		return monsterHp;
	}

	/** How far the adventurer has walked towards the monster, from 0 to 1. */
	public double getWalkProgress()
	{
		return phase == Phase.WALKING ? Math.min(1.0, phaseTicks / (double) WALK_TICKS) : 1.0;
	}

	/** Damage the player dealt this tick, 0 for a miss, or -1 if the player did not attack. */
	public int getLastPlayerHit()
	{
		return lastPlayerHit;
	}

	/** Damage the monster dealt this tick, 0 for a miss, or -1 if the monster did not attack. */
	public int getLastMonsterHit()
	{
		return lastMonsterHit;
	}

	public void setTargetMonster(String monsterId)
	{
		if (!monsterId.equals(state.targetMonster))
		{
			state.targetMonster = monsterId;
			startWalking();
		}
	}

	public void setTrainingSkill(Skill skill)
	{
		if (skill.isTrainable())
		{
			state.trainingSkill = skill;
		}
	}

	public void tick()
	{
		state.totalTicks++;
		lastPlayerHit = -1;
		lastMonsterHit = -1;

		if (phase != Phase.DEAD && playerHp < getPlayerMaxHp() && state.totalTicks % REGEN_TICKS == 0)
		{
			playerHp++;
		}

		phaseTicks++;
		switch (phase)
		{
			case WALKING:
				if (phaseTicks >= WALK_TICKS)
				{
					startFight();
				}
				break;
			case FIGHTING:
				fightTick();
				break;
			case DEAD:
				if (phaseTicks >= RESPAWN_TICKS)
				{
					playerHp = getPlayerMaxHp();
					startWalking();
				}
				break;
		}
	}

	private void startWalking()
	{
		monster = Monsters.byId(state.targetMonster);
		phase = Phase.WALKING;
		phaseTicks = 0;
	}

	private void startFight()
	{
		phase = Phase.FIGHTING;
		phaseTicks = 0;
		monsterHp = monster.getHitpoints();
		playerCooldown = 1;
		monsterCooldown = monster.getAttackSpeedTicks();
	}

	private void fightTick()
	{
		if (--playerCooldown <= 0)
		{
			playerCooldown = PLAYER_ATTACK_TICKS;
			playerAttack();
			if (monsterHp <= 0)
			{
				kill();
				return;
			}
		}
		if (--monsterCooldown <= 0)
		{
			monsterCooldown = monster.getAttackSpeedTicks();
			monsterAttack();
			if (playerHp <= 0)
			{
				die();
			}
		}
	}

	private void playerAttack()
	{
		int damage = 0;
		double chance = CombatMath.playerHitChance(state.level(Skill.ATTACK), monster.getDefenceLevel());
		if (random.nextDouble() < chance)
		{
			damage = 1 + random.nextInt(CombatMath.playerMaxHit(state.level(Skill.STRENGTH)));
			damage = Math.min(damage, monsterHp);
		}
		monsterHp -= damage;
		lastPlayerHit = damage;
		gainXp(damage);
	}

	private void monsterAttack()
	{
		int damage = 0;
		double chance = CombatMath.monsterHitChance(monster.getAttackLevel(), state.level(Skill.DEFENCE));
		if (random.nextDouble() < chance)
		{
			damage = Math.min(1 + random.nextInt(monster.getMaxHit()), playerHp);
		}
		playerHp -= damage;
		lastMonsterHit = damage;
	}

	private void gainXp(int damage)
	{
		if (damage == 0)
		{
			return;
		}
		int maxHpBefore = getPlayerMaxHp();
		state.skillXp[state.trainingSkill.ordinal()] += damage * XP_PER_DAMAGE;
		state.skillXp[Skill.HITPOINTS.ordinal()] += damage * HITPOINTS_XP_PER_DAMAGE;
		// A Hitpoints level-up adds the new point to current health, as in OSRS.
		playerHp += getPlayerMaxHp() - maxHpBefore;
	}

	private void kill()
	{
		state.kills++;
		state.bones++;
		state.coins += monster.getMinCoins() + random.nextInt(monster.getMaxCoins() - monster.getMinCoins() + 1);
		startWalking();
	}

	private void die()
	{
		state.deaths++;
		phase = Phase.DEAD;
		phaseTicks = 0;
	}
}
